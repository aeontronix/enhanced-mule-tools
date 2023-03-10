/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.anypoint.api;

import com.aeontronix.commons.URLBuilder;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointObject;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetInstance;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class ClientApplication extends AnypointObject<Organization> {
    private static final Logger logger = getLogger(ClientApplication.class);
    private Integer id;
    private String name;
    private String description;
    private String url;
    private String clientId;
    private String clientSecret;

    public ClientApplication(LegacyAnypointClient client) {
        super(client);
    }

    public ClientApplication(Organization parent) {
        super(parent);
    }

    public ClientApplication() {
    }

    @JsonProperty
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @JsonProperty
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @JsonIgnore
    public String getUriPath() {
        return parent.getUriPath() + "/applications/" + id;
    }

    public static ClientApplication create(@NotNull Organization organization, @NotNull String name, String url,
                                           String description, List<String> redirectUri, String apiEndpoints,
                                           String accessedAPIInstanceId) throws HttpException {
        LegacyAnypointClient client = organization.getClient();
        Map<String, Object> req = client.getJsonHelper().buildJsonMap().set("name", name.trim()).set("url", url)
                .set("description", description != null ? description : "")
                .set("redirectUri", redirectUri).set("apiEndpoints", apiEndpoints)
                .toMap();
        URLBuilder path = new URLBuilder(organization.getUriPath()).path("/applications");
        if (accessedAPIInstanceId != null) {
            path.queryParam("apiInstanceId", accessedAPIInstanceId);
        }
        String json = null;
        try {
            json = client.getHttpHelper().httpPost(path.toString(), req);
        } catch (HttpException e) {
            String msg = e.getMessage();
            if (accessedAPIInstanceId == null && msg != null && msg.contains("apiVersionId")) {
                logger.warn("Client Application skipped because no access are defined and client providers have been set");
            } else {
                throw e;
            }
        }
        return client.getJsonHelper().readJson(new ClientApplication(organization), json);
    }

    public static List<ClientApplication> find(Organization organization, String filter) throws HttpException {
        // workaround for the fact that filters sometimes don't work in anypoint... *joy*
        ClientApplicationList list = new ClientApplicationList(organization, null);
        Iterator<ClientApplication> i = list.iterator();
        ArrayList<ClientApplication> matchingClientApplications = new ArrayList<>();
        while (i.hasNext()) {
            ClientApplication clientApplication = i.next();
            if (clientApplication.getName().contains(filter)) {
                matchingClientApplications.add(clientApplication);
            }
        }
        return matchingClientApplications;
    }

    public void delete() throws HttpException {
        httpHelper.httpDelete(getUriPath());
    }

    public APIContract requestAPIAccess(API apiVersion) throws HttpException {
        return requestAPIAccess(apiVersion, null, false);
    }

    public APIContract requestAPIAccess(API apiVersion, SLATier tier) throws HttpException {
        return requestAPIAccess(apiVersion, tier, true);
    }

    public APIContract requestAPIAccess(API apiVersion, SLATier tier, boolean acceptedTerms) throws HttpException {
        JsonHelper.MapBuilder mapBuilder = jsonHelper.buildJsonMap()
                .set("apiId", apiVersion.getId())
                .set("environmentId", apiVersion.getParent().getId())
                .set("acceptedTerms", acceptedTerms)
                .set("organizationId", apiVersion.getParent().getParent().getId())
                .set("groupId", apiVersion.getGroupId())
                .set("assetId", apiVersion.getAssetId())
                .set("version", apiVersion.getAssetVersion())
                .set("productAPIVersion", apiVersion.getProductVersion());
        if (tier != null && tier.getId() == null) {
            throw new IllegalArgumentException("Tier is missing tier id");
        }
        Long tierId = tier != null ? tier.getId() : null;
        if (tierId == null) {
            SLATierList apiTiers = apiVersion.findSLATiers();
            if (apiTiers.size() == 1) {
                tierId = apiTiers.iterator().next().getId();
            }
        }
        if (tierId != null) {
            mapBuilder.set("requestedTierId", tierId);
        }
        Map<String, Object> req = mapBuilder.toMap();
        String json = httpHelper.httpPost("/exchange/api/v1/organizations/" + parent.getId() + "/applications/" + id + "/contracts", req);
        return jsonHelper.readJson(new APIContract(apiVersion), json);
    }


    public APIContract requestAPIAccess(API api, AssetInstance assetInstance) throws HttpException {
        return requestAPIAccess(api, assetInstance, null, false);
    }

    public APIContract requestAPIAccess(API api, AssetInstance assetInstance, SLATier tier) throws HttpException {
        return requestAPIAccess(api, assetInstance, tier, true);
    }

    public APIContract requestAPIAccess(API api, AssetInstance apiVersion, SLATier tier, boolean acceptedTerms) throws HttpException {
        JsonHelper.MapBuilder mapBuilder = jsonHelper.buildJsonMap()
                .set("apiId", apiVersion.getId())
                .set("environmentId", apiVersion.getEnvironmentId())
                .set("acceptedTerms", acceptedTerms)
                .set("organizationId", apiVersion.getOrganizationId())
                .set("groupId", apiVersion.getGroupId())
                .set("assetId", apiVersion.getAssetId())
                .set("version", apiVersion.getVersion())
                .set("productAPIVersion", apiVersion.getProductAPIVersion());
        if (tier != null && tier.getId() == null) {
            throw new IllegalArgumentException("Tier is missing tier id");
        }
        Long tierId = tier != null ? tier.getId() : null;
//        if (tierId == null) {
//            SLATierList apiTiers = apiVersion.findSLATiers();
//            if (apiTiers.size() == 1) {
//                tierId = apiTiers.iterator().next().getId();
//            }
//        }
        if (tierId != null) {
            mapBuilder.set("requestedTierId", tierId);
        }
        Map<String, Object> req = mapBuilder.toMap();
        String json = httpHelper.httpPost("/exchange/api/v1/organizations/" + parent.getId() + "/applications/" + id + "/contracts", req);
        return jsonHelper.readJson(new APIContract(api), json);
    }
}
