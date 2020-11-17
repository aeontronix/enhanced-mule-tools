/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint;

import com.aeontronix.enhancedmule.tools.alert.AlertUpdate;
import com.aeontronix.enhancedmule.tools.util.AnypointAccessToken;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProvider;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProviderUsernamePasswordImpl;
import com.aeontronix.enhancedmule.tools.legacy.deploy.DeploymentService;
import com.aeontronix.enhancedmule.tools.ocli.OCliClient;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.aeontronix.commons.FileUtils;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.UnexpectedException;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@SuppressWarnings("SameParameterValue")
public class AnypointClient implements Closeable, Serializable {
    private static Pattern idRegex = Pattern.compile("[a-zA-Z0-9\\-]+");
    private static final Logger logger = LoggerFactory.getLogger(AnypointClient.class);
    protected JsonHelper jsonHelper = new JsonHelper();
    protected HttpHelper httpHelper;
    private int maxParallelDeployments = 5;
    private transient ExecutorService deploymentThreadPool;
    private DeploymentService deploymentService;
    private OCliClient cliClient;
    private ModelMapper modelMapper;

    /**
     * Contructor used for serialization only
     **/
    public AnypointClient() {
        if (!loadAnypointCliConfig()) {
            throw new IllegalStateException("Unable to find/load configurations");
        }
        init();
    }

    public AnypointClient(AuthenticationProvider authenticationProvider) {
        this(authenticationProvider, 3);
    }

    public AnypointClient(AuthenticationProvider authenticationProvider, int maxParallelDeployments) {
        this.maxParallelDeployments = maxParallelDeployments;
        httpHelper = new HttpHelper(jsonHelper, authenticationProvider);
        init();
    }

    private void init() {
        jsonHelper.setClient(this);
        try {
            cliClient = new OCliClient();
        } catch (IllegalStateException e) {
            logger.debug("Anypoint cli not available", e);
        }
        deploymentService = loadService(DeploymentService.class);
        deploymentThreadPool = Executors.newFixedThreadPool(maxParallelDeployments);
        modelMapper = new ModelMapper();
        modelMapper.validate();
    }

    private boolean loadAnypointCliConfig() {
        try {
            File cfg = new File(System.getProperty("user.home") + File.separator + ".anypoint" + File.separator + "credentials");
            if (cfg.exists()) {
                logger.debug("Loading anypoint cli config file " + cfg.getPath());
                JsonNode config = new ObjectMapper().readTree(cfg);
                JsonNode def = config.get("default");
                if (def != null && !def.isNull()) {
                    JsonNode usernameNode = def.get("username");
                    JsonNode passwordNode = def.get("password");
                    if (usernameNode != null && passwordNode != null && !usernameNode.isNull() && !passwordNode.isNull()) {
                        httpHelper = new HttpHelper(jsonHelper, new AuthenticationProviderUsernamePasswordImpl(usernameNode.asText(), passwordNode.asText()));
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Unable to load anypoint cli configuration", e);
        }
        return false;
    }

    public OCliClient getCliClient() {
        return cliClient;
    }

    private <X> X loadService(Class<X> serviceClass) {
        X service;
        ServiceLoader<X> serviceLoader = ServiceLoader.load(serviceClass);
        Iterator<X> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            service = iterator.next();
            if (iterator.hasNext()) {
                throw new IllegalStateException("Found multiple implementations of ProvisioningService");
            }
        } else {
            try {
                if (serviceClass.isInterface()) {
                    service = Class.forName(serviceClass.getName() + "Impl").asSubclass(serviceClass).newInstance();
                } else {
                    service = serviceClass.newInstance();
                }
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new UnexpectedException(e);
            }
        }
        if (service instanceof Service) {
            ((Service) service).setClient(this);
        }
        return service;
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        deploymentThreadPool = Executors.newFixedThreadPool(maxParallelDeployments);
    }

    public int getMaxParallelDeployments() {
        return maxParallelDeployments;
    }

    public synchronized void setMaxParallelDeployments(int maxParallelDeployments) {
        if (maxParallelDeployments <= 0) {
            throw new IllegalArgumentException("maxParallelDeployments " + maxParallelDeployments + " is invalid (must be greater than 0)");
        }
        this.maxParallelDeployments = maxParallelDeployments;
        if (deploymentThreadPool != null) {
            deploymentThreadPool.shutdown();
        }
        deploymentThreadPool = Executors.newFixedThreadPool(maxParallelDeployments);
    }

    @Override
    public void close() throws IOException {
        httpHelper.close();
        if (deploymentThreadPool != null) {
            deploymentThreadPool.shutdown();
        }
    }

    public List<Organization> findOrganizations() throws HttpException {
        String json = httpHelper.httpGet("/accounts/api/me");
        ArrayList<Organization> list = new ArrayList<>();
        for (JsonNode node : jsonHelper.readJsonTree(json).at("/user/memberOfOrganizations")) {
            list.add(jsonHelper.readJson(new Organization(this), node));
        }
        return list;
    }

    @NotNull
    public Organization findOrganizationByNameOrId(String nameOrId) throws NotFoundException, HttpException {
        for (Organization organization : findOrganizations()) {
            if (organization.getId().equals(nameOrId) || organization.getName().equals(nameOrId)) {
                return organization;
            }
        }
        throw new NotFoundException("Organization not found: " + nameOrId + " (if using client credentials you must use org id instead of nameOrId)");
    }

    public Organization findOrganizationById(String id) throws HttpException, NotFoundException {
        Organization organization = new Organization(this);
        organization.setId(id);
        try {
            String json = httpHelper.httpGet(organization.getUriPath());
            jsonHelper.readJson(organization, json);
            return organization;
        } catch (HttpException e) {
            if (e.getStatusCode() == 404) {
                throw new NotFoundException("Enable to find organization " + id, e);
            } else if (e.getStatusCode() == 403) {
                logger.debug("Access to organization data denied: " + id);
                return organization;
            } else {
                throw e;
            }
        }
    }

    /**
     * Return details on the account used to administer anypoint
     *
     * @return User details
     * @throws HttpException if an http exception occurs
     */
    public User getUser() throws HttpException {
        String json = httpHelper.httpGet("/accounts/api/me");
        return jsonHelper.readJson(new User(), json, "/user");
    }

    public Organization createOrganization(String name) throws HttpException {
        User user = getUser();
        return user.getOrganization().createSubOrganization(name, user.getId(), true, true);
    }

    public Organization createOrganization(String name, String ownerId, boolean createSubOrgs, boolean createEnvironments) throws HttpException {
        return getUser().getOrganization().createSubOrganization(name, ownerId, createSubOrgs, createEnvironments);
    }

    public Organization createOrganization(String name, String ownerId, boolean createSubOrgs, boolean createEnvironments,
                                           boolean globalDeployment, int vCoresProduction, int vCoresSandbox, int vCoresDesign,
                                           int staticIps, int vpcs, int loadBalancer) throws HttpException {
        return getUser().getOrganization().createSubOrganization(name, ownerId, createSubOrgs,
                createEnvironments, globalDeployment, vCoresProduction, vCoresSandbox, vCoresDesign, staticIps, vpcs, loadBalancer);
    }

    public JsonHelper getJsonHelper() {
        return jsonHelper;
    }

    public HttpHelper getHttpHelper() {
        return httpHelper;
    }

    public void setHttpHelper(HttpHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    public Environment findEnvironment(String organizationName, String environmentName, boolean createOrganization, boolean createEnvironment, Environment.Type createEnvironmentType) throws NotFoundException, HttpException {
        Organization organization;
        try {
            organization = findOrganizationByNameOrId(organizationName);
        } catch (NotFoundException e) {
            if (createOrganization) {
                organization = createOrganization(organizationName);
            } else {
                throw e;
            }
        }
        try {
            return organization.findEnvironmentByName(environmentName);
        } catch (NotFoundException e) {
            if (createEnvironment) {
                return organization.createEnvironment(environmentName, createEnvironmentType);
            } else {
                throw e;
            }
        }
    }

    public Set<Environment> findEnvironmentsRegexSearch(JsonNode targetJsonNode) throws HttpException {
        List<Organization> orgs = findOrganizations();
        if (targetJsonNode instanceof ArrayNode) {
            HashSet<Environment> combined = new HashSet<>();
            for (JsonNode jsonNode : targetJsonNode) {
                combined.addAll(findEnvironmentsRegexSearch(jsonNode, orgs));
            }
            return combined;
        } else {
            return findEnvironmentsRegexSearch(targetJsonNode, orgs);
        }
    }

    private Set<Environment> findEnvironmentsRegexSearch(String orgRegex, String envRegex) throws HttpException {
        return findEnvironmentsRegexSearch(orgRegex, envRegex, findOrganizations());
    }

    private Set<Environment> findEnvironmentsRegexSearch(JsonNode targetJsonNode, List<Organization> orgs) throws HttpException {
        JsonNode orgNode = targetJsonNode.get("org");
        if (orgNode == null) {
            throw new InvalidJsonException("Invalid target json, missing 'org' field");
        }
        JsonNode envNode = targetJsonNode.get("env");
        if (envNode == null) {
            throw new InvalidJsonException("Invalid target json, missing 'env' field");
        }
        return findEnvironmentsRegexSearch(orgNode.asText(), envNode.asText(), orgs);
    }

    private Set<Environment> findEnvironmentsRegexSearch(String orgRegex, String envRegex, List<Organization> orgs) throws HttpException {
        HashSet<Environment> envs = new HashSet<>();
        Pattern orgPattern = Pattern.compile(orgRegex);
        Pattern envPattern = Pattern.compile(envRegex);
        for (Organization org : orgs) {
            if (orgPattern.matcher(org.getName()).find()) {
                for (Environment environment : org.findAllEnvironments()) {
                    if (envPattern.matcher(environment.getName()).find()) {
                        envs.add(environment);
                    }
                }
            }
        }
        return envs;
    }

    public void applyAlerts(File alertsDescriptor) throws IOException, HttpException {
        if (!alertsDescriptor.exists()) {
            throw new IllegalArgumentException("Alerts data file not found: " + alertsDescriptor.getPath());
        }
        applyAlerts(FileUtils.toString(alertsDescriptor));
    }

    public void applyAlert(String name, JsonNode jsonNode) throws HttpException {
        JsonNode target = jsonNode.get("target");
        if (target == null) {
            throw new InvalidJsonException("Invalid alert json missing target: " + name);
        }
        Set<Environment> envs = findEnvironmentsRegexSearch(target);
        AlertUpdate alert = jsonHelper.readJson(AlertUpdate.class, jsonNode, this);
        alert.setName(name);
        applyAlert(envs, alert);
    }

    public void applyAlert(Set<Environment> environments, AlertUpdate alert) throws HttpException {
        for (Environment environment : environments) {
            environment.applyAlert(alert);
        }
    }

    private void applyAlerts(String json) throws HttpException {
        JsonNode jsonNode = jsonHelper.readJsonTree(json);
        if (!jsonNode.isObject()) {
            throw new InvalidJsonException("Invalid alert data file, root element should be an object");
        }
        JsonNode alertsNode = jsonNode.get("alerts");
        if (alertsNode != null) {
            for (Iterator<Map.Entry<String, JsonNode>> it = alertsNode.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> field = it.next();
                String alertName = field.getKey();
                if (StringUtils.isBlank(alertName)) {
                    throw new InvalidJsonException("Invalid alert, name must not be blank");
                }
                JsonNode alertNode = field.getValue();
                applyAlert(alertName, alertNode);
            }
        }
    }

    public String getUserId() throws HttpException {
        // TODO cache this
        return getUser().getId();
    }

    public ModelMapper getModelMapper() {
        return modelMapper;
    }

    public DeploymentService getDeploymentService() {
        return deploymentService;
    }

    public void setProxy(String scheme, String host, int port, String username, String password) {
        httpHelper.setProxy(scheme, host, port, username, password);
    }

    public void unsetProxy() {
        httpHelper.unsetProxy();
    }

    public AnypointAccessToken getBearerToken() throws HttpException {
        return getHttpHelper().getAuthToken();
    }
}
