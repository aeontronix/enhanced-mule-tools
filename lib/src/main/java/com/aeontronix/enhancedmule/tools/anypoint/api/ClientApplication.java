/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.anypoint.api;

import com.aeontronix.anypointsdk.AnypointClient;
import com.aeontronix.anypointsdk.exchange.ExchangeClientApplication;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointObject;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetInstance;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.aeontronix.restclient.RESTException;
import com.aeontronix.restclient.json.JsonConvertionException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class ClientApplication extends AnypointObject<Organization> {
    private static final Logger logger = getLogger(ClientApplication.class);
    private ExchangeClientApplication cApp;

    public ClientApplication(LegacyAnypointClient client) {
        super(client);
    }

    public ClientApplication(Organization parent) {
        super(parent);
    }

    public ClientApplication(ExchangeClientApplication clientApplication, Organization organization) {
        super(organization);
        cApp = clientApplication;
    }

    public ClientApplication() {
    }

    @JsonProperty
    public Integer getId() {
        return cApp.getId();
    }

    public void setId(Integer id) {
        cApp.setId(id);
    }

    @JsonProperty
    public String getName() {
        return cApp.getName();
    }

    public void setName(String name) {
        cApp.setName(name);
    }

    @JsonProperty
    public String getDescription() {
        return cApp.getDescription();
    }

    public void setDescription(String description) {
        cApp.setDescription(description);
    }

    @JsonProperty
    public String getUrl() {
        return cApp.getUrl();
    }

    public void setUrl(String url) {
        cApp.getUrl();
    }

    @JsonProperty
    public String getClientId() {
        return cApp.getClientId();
    }

    public void setClientId(String clientId) {
        cApp.setClientId(clientId);
    }

    @JsonProperty
    public String getClientSecret() {
        return cApp.getClientSecret();
    }

    public void setClientSecret(String clientSecret) {
        cApp.setClientSecret(clientSecret);
    }

    @JsonIgnore
    public String getUriPath() {
        return parent.getUriPath() + "/applications/" + getId();
    }

    public static ClientApplication create(AnypointClient anypointClient, @NotNull Organization organization, @NotNull String name, String url,
                                           String description, List<String> redirectUri, String apiEndpoints,
                                           String accessedAPIInstanceId) throws HttpException {
        try {
            ExchangeClientApplication clientApplication = anypointClient.getExchangeClient().createClientApplication(organization.getId(), name, url, description, redirectUri, null, apiEndpoints, accessedAPIInstanceId);
            ClientApplication cApp = new ClientApplication();
            cApp.setName(clientApplication.getName());
            cApp.setClientId(clientApplication.getClientId());
            cApp.setClientSecret(clientApplication.getClientSecret());
            cApp.setId(clientApplication.getId());
            cApp.setDescription(clientApplication.getDescription());
            cApp.setUrl(url);
            return cApp;
        } catch (JsonConvertionException | RESTException e) {
            throw new HttpException(e);
        }
    }

    public static List<ClientApplication> find(AnypointClient client, LegacyAnypointClient legacyAnypointClient, Organization organization, String filter) throws HttpException {
        List<ClientApplication> list = new ArrayList<>();
        try {
            List<ExchangeClientApplication> clientApplications = client.getExchangeClient().listClientApplications(organization.getId());
            for (ExchangeClientApplication clientApplication : clientApplications) {
                if (clientApplication.getName().contains(filter)) {
                    list.add(new ClientApplication(clientApplication, organization));
                }
            }
        } catch (RESTException e) {
            throw new HttpException(e);
        }
        return list;
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
        if (jsonHelper == null) {
            // attempting to fix weird situation where those are null (shouldn't happen)
            client = apiVersion.getClient();
            jsonHelper = client.getJsonHelper();
            httpHelper = client.getHttpHelper();
        }
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
        String json = httpHelper.httpPost("/exchange/api/v1/organizations/" + parent.getId() + "/applications/" + getId() + "/contracts", req);
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
        String json = httpHelper.httpPost("/exchange/api/v1/organizations/" + parent.getId() + "/applications/" + getId() + "/contracts", req);
        return jsonHelper.readJson(new APIContract(api), json);
    }
}
