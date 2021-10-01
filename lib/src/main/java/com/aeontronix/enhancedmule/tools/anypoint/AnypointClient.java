/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint;

import com.aeontronix.commons.FileUtils;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.UnexpectedException;
import com.aeontronix.enhancedmule.tools.anypoint.alert.AlertUpdate;
import com.aeontronix.enhancedmule.tools.anypoint.api.ClientApplication;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.DeploymentOperation;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.RuntimeDeploymentRequest;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProvider;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProviderUsernamePasswordImpl;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ApplicationProvisioningService;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ExchangeManagementClient;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.APIProvisioningResult;
import com.aeontronix.enhancedmule.tools.application.api.ClientApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.exchange.ExchangeAssetDescriptor;
import com.aeontronix.enhancedmule.tools.util.*;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.RESTClientHost;
import com.aeontronix.restclient.RESTException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@SuppressWarnings("SameParameterValue")
public class AnypointClient implements Closeable, Serializable {
    private static Pattern idRegex = Pattern.compile("[a-zA-Z0-9\\-]+");
    private static final Logger logger = LoggerFactory.getLogger(AnypointClient.class);
    private static final EMTLogger elogger = new EMTLogger(logger);
    protected JsonHelper jsonHelper = new JsonHelper();
    protected HttpHelper httpHelper;
    private int maxParallelDeployments = 5;
    private transient ExecutorService deploymentThreadPool;
    private ModelMapper modelMapper;
    private RESTClient restClient;
    private RESTClientHost anypointRestClient;

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
        anypointRestClient.setCredential(authenticationProvider);
    }

    private void init() {
        jsonHelper.setClient(this);
        deploymentThreadPool = Executors.newFixedThreadPool(maxParallelDeployments);
        modelMapper = new ModelMapper();
        modelMapper.validate();
        restClient = RESTClient.builder().build();
        anypointRestClient = restClient.host(URI.create("https://anypoint.mulesoft.com"));
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
        try {
            return anypointRestClient.get("/accounts/api/me").execute(UserInfo.class).getUser();
        } catch (RESTException e) {
            if (e.getStatusCode() != null) {
                throw new HttpException(e.getStatusCode());
            } else {
                throw new HttpException(e);
            }
        }
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

    public void setProxy(String scheme, String host, int port, String username, String password) {
        httpHelper.setProxy(scheme, host, port, username, password);
    }

    public void unsetProxy() {
        httpHelper.unsetProxy();
    }

    public AnypointAccessToken getBearerToken() throws HttpException {
        return getHttpHelper().getAuthToken();
    }

    public RESTClient getRestClient() {
        return restClient;
    }

    public RESTClientHost getAnypointRestClient() {
        return anypointRestClient;
    }

    public String deployApplicationToCH(boolean existingApp, Environment environment, boolean autoStart, Map<String, Object> appInfo,
                                        String filename, FileInputStream fileInputStream, String domain) throws IOException {
        HttpHelper.MultiPartRequest req;
        if (existingApp) {
            req = httpHelper.createAnypointMultiPartPutRequest("/cloudhub/api/v2/applications/" + domain, environment);
        } else {
            req = httpHelper.createAnypointMultiPartPostRequest("/cloudhub/api/v2/applications", environment);
        }
        req = req.addText("autoStart", Boolean.toString(autoStart));
        String appInfoJson = new String(environment.getClient().getJsonHelper().toJson(appInfo));
        req = req.addText("appInfoJson", appInfoJson);
        logger.debug("Deployment JSON: {}", appInfoJson);
        req = req.addBinary("file", new StreamSource() {
            @Override
            public String getFileName() {
                return filename;
            }

            @Override
            public InputStream createInputStream() throws IOException {
                return fileInputStream;
            }
        });
        elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Uploading application archive to Cloudhub");
        String deploymentJson = req.execute();
        elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Application starting: " + domain);
        return deploymentJson;
    }

    public void provisionApplication(ApplicationDescriptor applicationDescriptor, RuntimeDeploymentRequest deploymentRequest) throws ProvisioningException, HttpException {
        APIProvisioningResult provisioningResult;
        final Environment environment = deploymentRequest.getEnvironment();
        final Organization organization = environment.getOrganization();
        final ApplicationProvisioningService applicationProvisioningService = new ApplicationProvisioningService(this);
        boolean assetPublished = false;
        final ExchangeManagementClient exchangeManagementClient = new ExchangeManagementClient();
        if (applicationDescriptor.isAssetPublish()) {
            final ExchangeAssetDescriptor asset = applicationDescriptor.getApi().getAsset();
            assetPublished = exchangeManagementClient.publish(asset, organization, deploymentRequest.getApplicationSource(), deploymentRequest);
        }
        provisioningResult = applicationProvisioningService.provision(applicationDescriptor, environment, deploymentRequest);
        final APIDescriptor apiDescriptor = applicationDescriptor.getApi();
        if (provisioningResult.getApi() != null && apiDescriptor.isInjectApiId()) {
            final String apiIdProperty = apiDescriptor.getApiIdProperty();
            if (apiIdProperty == null) {
                throw new IllegalArgumentException("apiIdProperty musn't be null");
            }
            deploymentRequest.setOverrideProperty(apiIdProperty, provisioningResult.getApi().getId());
            deploymentRequest.setOverrideProperty(DeploymentOperation.ANYPOINT_PLATFORM_CLIENT_ID, environment.getClientId());
            try {
                final String clientSecret = environment.getClientSecret();
                if (clientSecret != null) {
                    deploymentRequest.setOverrideProperty(DeploymentOperation.ANYPOINT_PLATFORM_CLIENT_SECRET, clientSecret);
                }
            } catch (HttpException e) {
                if (e.getStatusCode() != 401) {
                    throw e;
                }
            }
        }
        if (assetPublished && deploymentRequest.isDeleteSnapshots()) {
            exchangeManagementClient.deleteSnapshots(organization, applicationDescriptor.getApi().getAsset());
        }
        final ClientApplicationDescriptor clientDescriptor = applicationDescriptor.getClient();
        ClientApplication clientApp = provisioningResult.getClientApplication();
        if (clientApp != null && clientDescriptor != null && clientDescriptor.isInjectClientIdSec()) {
            final String clientIdProperty = clientDescriptor.getClientIdProperty();
            if (clientIdProperty == null) {
                throw new IllegalStateException("client descriptor id property musn't be null");
            }
            deploymentRequest.setOverrideProperty(clientIdProperty, clientApp.getClientId());
            final String clientSecretProperty = clientDescriptor.getClientSecretProperty();
            if (clientSecretProperty == null) {
                throw new IllegalStateException("client descriptor id property musn't be null");
            }
            deploymentRequest.setOverrideProperty(clientSecretProperty, clientApp.getClientSecret());
        }
    }
}
