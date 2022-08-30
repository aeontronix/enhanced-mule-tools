/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint;

import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.exception.UnexpectedException;
import com.aeontronix.enhancedmule.tools.anypoint.alert.Alert;
import com.aeontronix.enhancedmule.tools.anypoint.alert.AlertUpdate;
import com.aeontronix.enhancedmule.tools.anypoint.api.API;
import com.aeontronix.enhancedmule.tools.anypoint.api.APIAsset;
import com.aeontronix.enhancedmule.tools.anypoint.api.APIList;
import com.aeontronix.enhancedmule.tools.anypoint.api.APISpec;
import com.aeontronix.enhancedmule.tools.cloudhub.CHMuleVersion;
import com.aeontronix.enhancedmule.tools.cloudhub.CHRegion;
import com.aeontronix.enhancedmule.tools.cloudhub.CHWorkerType;
import com.aeontronix.enhancedmule.tools.runtime.CHApplication;
import com.aeontronix.enhancedmule.tools.runtime.Server;
import com.aeontronix.enhancedmule.tools.runtime.ServerGroup;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.HttpRequestBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.aeontronix.commons.StringUtils.isBlank;

public class Environment extends AnypointObject<Organization> {
    private static final Logger logger = LoggerFactory.getLogger(Environment.class);
    private String id;
    private String name;
    private boolean production;
    private Type type;
    private String clientId;
    private String group;

    public Environment() {
    }

    public Environment(Organization organization) {
        super(organization);
    }

    public Environment(Organization organization, String id) {
        super(organization);
        this.id = id;
    }

    @JsonIgnore
    public Organization getOrganization() {
        return parent;
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @JsonProperty("isProduction")
    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    @JsonProperty
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @JsonProperty
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() throws HttpException {
        String json = httpHelper.httpGet("/accounts/api/organizations/" + parent.getId() + "/clients/" + clientId);
        try {
            return jsonHelper.getJsonMapper().readTree(json).at("/client_secret").textValue();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    @NotNull
    public String getServerRegistrationKey() throws HttpException {
        String json = httpHelper.anypointHttpGet("/hybrid/api/v1/servers/registrationToken", this);
        return (String) jsonHelper.toJsonMap(json).get("data");
    }

    public List<Server> findAllServers() throws HttpException {
        String json = client.getHttpHelper().anypointHttpGet("/armui/api/v1/servers", this);
        ArrayList<Server> list = new ArrayList<>();
        for (JsonNode node : jsonHelper.readJsonTree(json).at("/data")) {
            JsonNode type = node.get("type");
            Server s;
            if (type.asText().equals("SERVER_GROUP")) {
                s = jsonHelper.readJson(new ServerGroup(this), node);
            } else {
                s = jsonHelper.readJson(new Server(this), node);
            }
            list.add(s);
        }
        return list;
    }

    public ServerGroup createServerGroup(String name, String... serverIds) throws HttpException {
        if (serverIds == null) {
            serverIds = new String[0];
        }
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("serverIds", serverIds);
        String json = httpHelper.anypointHttpPost("/hybrid/api/v1/serverGroups", request, this);
        return jsonHelper.readJson(new ServerGroup(this), json, "/data");
    }

    @NotNull
    public Server findServerByName(@NotNull String name) throws NotFoundException, HttpException {
        for (Server server : findAllServers()) {
            if (name.equals(server.getName())) {
                return server;
            }
        }
        throw new NotFoundException("Cannot find server : " + name);
    }

    public void addHeaders(HttpRequestBase method) {
        method.setHeader("X-ANYPNT-ORG-ID", parent.getId());
        method.setHeader("X-ANYPNT-ENV-ID", id);
    }

    public void delete() throws HttpException {
        for (Server server : findAllServers()) {
            server.delete();
        }
        httpHelper.httpDelete("/accounts/api/organizations/" + parent.getId() + "/environments/" + id);
        logger.info("Deleted environment " + id + " : " + name);
    }

    public Environment rename(String newName) throws HttpException {
        HashMap<String, String> req = new HashMap<>();
        req.put("id", id);
        req.put("name", newName);
        req.put("organizationId", parent.getId());
        String json = httpHelper.httpPut("/accounts/api/organizations/" + parent.getId() + "/environments/" + id, req);
        return jsonHelper.readJson(new Environment(parent), json);
    }

    @Override
    public String toString() {
        return "Environment{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", production=" + production +
                ", type='" + type + '\'' +
                ", clientId='" + clientId + '\'' +
                "} " + super.toString();
    }

    public APIList findAllAPIs() throws HttpException {
        return findAPIs(null);
    }

    public APIList findAPIs(String filter) throws HttpException {
        return new APIList(this, filter);
    }

    public API findAPIByExchangeAssetNameAndVersion(@NotNull String name, @NotNull String version) throws HttpException, NotFoundException {
        return findAPIByExchangeAssetNameAndVersion(name, version, null);
    }

    public API findAPIByExchangeAssetIdOrNameAndVersion(@NotNull String idOrName, @NotNull String version, @Nullable String label) throws HttpException, NotFoundException {
        for (APIAsset asset : findAllAPIs()) {
            if (asset.getAssetId().equalsIgnoreCase(idOrName)) {
                for (API api : asset.getApis()) {
                    if (api.getAssetVersion().equalsIgnoreCase(version) && (label == null || label.equalsIgnoreCase(api.getInstanceLabel()))) {
                        return api;
                    }
                }
            }
        }
        return findAPIByExchangeAssetNameAndVersion(idOrName, version, label);
    }

    public API findAPIByExchangeAssetNameAndVersion(@NotNull String name, @NotNull String version, @Nullable String label) throws HttpException, NotFoundException {
        for (APIAsset asset : findAPIs(name)) {
            if (asset.getExchangeAssetName().equalsIgnoreCase(name)) {
                for (API api : asset.getApis()) {
                    if (api.getAssetVersion().equalsIgnoreCase(version) && (label == null || label.equalsIgnoreCase(api.getInstanceLabel()))) {
                        return api;
                    }
                }
            }
        }
        throw new NotFoundException("API " + name + " " + version + " not found");
    }

    public API findAPIByExchangeAssetIdOrNameAndProductAPIVersion(@NotNull String name, @NotNull String productAPIVersion, @Nullable String label) throws HttpException, NotFoundException {
        for (APIAsset asset : findAllAPIs()) {
            if (asset.getAssetId().equalsIgnoreCase(name)) {
                for (API api : asset.getApis()) {
                    if (api.getProductVersion().equalsIgnoreCase(productAPIVersion) && (label == null || label.equalsIgnoreCase(api.getInstanceLabel()))) {
                        return api;
                    }
                }
            }
        }
        return findAPIByExchangeAssetNameAndProductAPIVersion(name, productAPIVersion, label);
    }

    public API findAPIByExchangeAssetNameAndProductAPIVersion(@NotNull String name, @NotNull String productAPIVersion, @Nullable String label) throws HttpException, NotFoundException {
        for (APIAsset asset : findAllAPIs()) {
            if (asset.getExchangeAssetName().equalsIgnoreCase(name)) {
                for (API api : asset.getApis()) {
                    if (api.getProductVersion().equalsIgnoreCase(productAPIVersion) && (label == null || label.equalsIgnoreCase(api.getInstanceLabel()))) {
                        return api;
                    }
                }
            }
        }
        throw new NotFoundException("API " + name + " with product version " + productAPIVersion + " not found");
    }

    public List<API> findAPIsByExchangeAsset(@NotNull String groupId, @NotNull String assetId) throws HttpException, NotFoundException {
        ArrayList<API> apis = new ArrayList<>();
        for (APIAsset api : findAllAPIs()) {
            if (api.getGroupId().equalsIgnoreCase(groupId) && api.getAssetId().equalsIgnoreCase(assetId)) {
                apis.addAll(api.getApis());
            }
        }
        return apis;
    }

    public API findAPIByExchangeAsset(@NotNull String groupId, @NotNull String assetId, @NotNull String assetVersion) throws HttpException, NotFoundException {
        return findAPIByExchangeAsset(groupId, assetId, assetVersion, null);
    }

    public API findAPIByExchangeAsset(@NotNull String groupId, @NotNull String assetId, @NotNull String assetVersion, @Nullable String label) throws HttpException, NotFoundException {
        if (isBlank(groupId)) {
            throw new IllegalArgumentException("groupId missing (null or blank)");
        }
        if (isBlank(assetId)) {
            throw new IllegalArgumentException("assetId missing (null or blank)");
        }
        if (isBlank(assetVersion)) {
            throw new IllegalArgumentException("assetVersion missing (null or blank)");
        }
        for (APIAsset asset : findAllAPIs()) {
            if (asset.getGroupId().equalsIgnoreCase(groupId) && asset.getAssetId().equalsIgnoreCase(assetId)) {
                for (API api : asset.getApis()) {
                    if (api.getAssetVersion().equalsIgnoreCase(assetVersion) && (label == null || label.equalsIgnoreCase(api.getInstanceLabel()))) {
                        return api;
                    }
                }
            }
        }
        throw new NotFoundException("API based on exchange asset not found: groupId=" + groupId + ", assetId=" + assetId + ", assetVersion=" + assetVersion + ", label=" + label);
    }

    public API findAPIById(String id) throws HttpException {
        final String json = httpHelper.httpGet(new URLBuilder("/apimanager/api/v1/organizations/" + getParent().getId() + "/environments/" + getId() + "/apis/").path(id).toString());
        return jsonHelper.readJson(new API(this), json);
    }

    public CHApplication findCHApplicationByDomain(String domain) throws HttpException, NotFoundException {
        return CHApplication.find(this, domain);
    }

    /**
     * Refresh environment data (this will retrieve extra data compared to an environment object obtained from an organization)
     *
     * @return Refreshed environment
     * @throws NotFoundException If the environment isn't found
     * @throws HttpException     If an error occurs communicating with anypoint platform
     */
    public Environment refresh() throws NotFoundException, HttpException {
        return Environment.findEnvironmentById(id, client, parent);
    }

    @JsonIgnore
    public String getLName() {
        return getName().replace(" ", "_").toLowerCase();
    }

    @JsonIgnore
    public String getSuffix() {
        return "-" + getLName();
    }

    public API createAPI(@NotNull APISpec apiSpec, @Nullable String label,
                         Map<String, Object> implementationUrlJson, String consumerUrl) throws HttpException {
        return API.create(this, apiSpec, label, implementationUrlJson, consumerUrl);
    }

    public API createAPI(@NotNull APISpec apiSpec, boolean mule4, @Nullable String implementationUrl, String consumerUrl, @Nullable String label,
                         @NotNull API.Type type) throws HttpException {
        return API.create(this, apiSpec, mule4, implementationUrl, consumerUrl, label, type);
    }

    @SuppressWarnings("unchecked")
    public static List<Environment> findEnvironmentsByOrg(@NotNull AnypointClient client, @NotNull Organization organization) throws HttpException {
        String json = client.getHttpHelper().httpGet("/accounts/api/organizations/" + organization.getId() + "/environments");
        return client.getJsonHelper().readJsonList((Class<Environment>) organization.getEnvironmentClass(), json, organization, "/data");
    }

    @NotNull
    public static Environment findEnvironmentByName(@NotNull String name, @NotNull AnypointClient client, @NotNull Organization organization) throws HttpException, EnvironmentNotFoundException {
        logger.debug("Searching for environment named {}", name);
        for (Environment environment : findEnvironmentsByOrg(client, organization)) {
            logger.debug("Checking if " + environment.getName() + " is equals to " + name);
            if (name.equals(environment.getName())) {
                logger.debug("Match found, returning env " + environment.getId());
                return environment;
            }
        }
        try {
            final List<String> envNames = findEnvironmentsByOrg(client, organization).stream().map(Environment::getName).collect(Collectors.toList());
            throw new EnvironmentNotFoundException("Environment not found in org  " + organization.getName() + " : " + name + " must be one of: " + envNames);
        } catch (Exception e) {
            if (e instanceof EnvironmentNotFoundException) {
                throw e;
            } else {
                throw new EnvironmentNotFoundException("Environment not found: " + name);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static Environment findEnvironmentById(@NotNull String id, @NotNull AnypointClient client, @NotNull Organization organization) throws HttpException, EnvironmentNotFoundException {
        logger.debug("finding environment by id: {}", id);
        String json = null;
        final String organizationId = organization.getId();
        try {
            final HttpHelper httpHelper = client.getHttpHelper();
            json = httpHelper.httpGet("/accounts/api/organizations/" + organizationId + "/environments/" + id);
            return client.getJsonHelper().readJson(organization.createEnvironmentObject(), json, organization);
        } catch (HttpException e) {
            final int statusCode = e.getStatusCode();
            if (statusCode == 401 || statusCode == 403) {
                for (Environment environment : findEnvironmentsByOrg(client, organization)) {
                    if( environment.getId().equalsIgnoreCase(id) ) {
                        return environment;
                    }
                }
                throw new EnvironmentNotFoundException("Environment not found: " + id);
            }
            if (statusCode == 404) {
                throw new EnvironmentNotFoundException("Environment with id " + id + " not found within org " + organizationId);
            } else {
                throw e;
            }
        }
    }

    public String getNameOrId() {
        return name != null ? "(name) " + name : "(id) " + id;
    }

    public List<CHMuleVersion> findCHMuleVersions() throws HttpException {
        String json = client.getHttpHelper().anypointHttpGet("/cloudhub/api/mule-versions", this);
        return client.getJsonHelper().readJsonList(CHMuleVersion.class, json, this, "/data");
    }

    public CHMuleVersion findDefaultCHMuleVersion() throws HttpException {
        for (CHMuleVersion version : findCHMuleVersions()) {
            if (version.isDefaultVersion()) {
                return version;
            }
        }
        throw new UnexpectedException("No default mule version found");
    }

    public CHMuleVersion findCHMuleVersion(String muleVersion) throws NotFoundException, HttpException {
        for (CHMuleVersion version : findCHMuleVersions()) {
            if (version.getVersion().equalsIgnoreCase(muleVersion)) {
                return version;
            }
        }
        throw new NotFoundException("Unable to find mule version " + muleVersion);
    }

    public List<CHRegion> findAllCHRegions() throws HttpException {
        String json = client.getHttpHelper().anypointHttpGet("/cloudhub/api/regions", this);
        return client.getJsonHelper().readJsonList(CHRegion.class, json, this);
    }

    public CHRegion findDefaultCHRegion() throws HttpException {
        for (CHRegion region : findAllCHRegions()) {
            if (region.isDefaultRegion()) {
                return region;
            }
        }
        throw new UnexpectedException("No default mule version found");
    }

    public List<CHWorkerType> findAllWorkerTypes() throws HttpException {
        String json = client.getHttpHelper().anypointHttpGet("/cloudhub/api/organization", this);
        return client.getJsonHelper().readJsonList(CHWorkerType.class, json, this, "/plan/workerTypes");
    }

    public CHWorkerType findWorkerTypeByName(String name) throws HttpException, NotFoundException {
        for (CHWorkerType workerType : findAllWorkerTypes()) {
            if (workerType.getName().equalsIgnoreCase(name)) {
                return workerType;
            }
        }
        throw new NotFoundException("Unable to find worker type in plan: " + name);
    }

    public CHWorkerType findSmallestWorkerType() throws HttpException {
        CHWorkerType smallest = null;
        for (CHWorkerType workerType : findAllWorkerTypes()) {
            if (smallest == null || smallest.getWorkerVal().compareTo(workerType.getWorkerVal()) > 0) {
                smallest = workerType;
            }
        }
        return smallest;
    }

    public List<Alert> findAlerts() throws HttpException {
        String json = httpHelper.anypointHttpGet("https://anypoint.mulesoft.com/armui/api/v1/alerts", this);
        return jsonHelper.readJsonList(Alert.class, json, this, "/data");
    }

    public Alert findAlertByName(String name) throws HttpException, NotFoundException {
        for (Alert alert : findAlerts()) {
            if (alert.getName().equals(name)) {
                return alert;
            }
        }
        throw new NotFoundException("Unable to find alert named: " + name);
    }

    public void applyAlert(AlertUpdate alert) throws HttpException {
        URLBuilder url = new URLBuilder("/armui/api/v1/alerts");
        if (alert.getCondition().getResourceType().startsWith("cloudhub-")) {
            url.path("cloudhub");
        } else {
            url.path("hybrid");
        }
        try {
            Alert existingAlert = findAlertByName(alert.getName());
            url.path(existingAlert.getId());
            logger.info("Updating existing alert " + existingAlert.getId() + " in env " + getNameOrId());
            httpHelper.anypointHttpPut(url.toString(), alert, this);
        } catch (NotFoundException e) {
            httpHelper.anypointHttpPost(url.toString(), alert, this);
        }
    }

    public static Map<String, Environment> toMapIdxByName(Collection<Environment> envs) {
        HashMap<String, Environment> map = new HashMap<>();
        for (Environment env : envs) {
            map.put(env.getName(), env);
        }
        return map;
    }

    public static Map<String, Environment> toMapIdxById(Collection<Environment> envs) {
        HashMap<String, Environment> map = new HashMap<>();
        for (Environment env : envs) {
            map.put(env.getId(), env);
        }
        return map;
    }

    public enum Type {
        DESIGN, SANDBOX, PRODUCTION
    }
}
