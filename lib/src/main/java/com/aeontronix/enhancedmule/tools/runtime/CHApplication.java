/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.runtime;

import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointObject;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class CHApplication extends AnypointObject<Environment> {
    private String domain;
    private String versionId;
    private String status;
    private String region;
    private String deploymentUpdateStatus;
    private long lastUpdateTime;
    private Map<String,String> properties;

    @JsonProperty
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @JsonProperty
    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    @JsonProperty
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty
    public String getDeploymentUpdateStatus() {
        return deploymentUpdateStatus;
    }

    public void setDeploymentUpdateStatus(String deploymentUpdateStatus) {
        this.deploymentUpdateStatus = deploymentUpdateStatus;
    }

    @JsonProperty
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @JsonProperty
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @JsonProperty
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public CHApplication refresh() throws HttpException, NotFoundException {
        return find(parent, domain);
    }

    public static CHApplication find(Environment environment, String domain) throws HttpException, NotFoundException {
        LegacyAnypointClient client = environment.getClient();
        try {
            String json = client.getHttpHelper().anypointHttpGet("/cloudhub/api/v2/applications/" + domain, environment);
            return client.getJsonHelper().readJson(new CHApplication(), json, environment);
        } catch (HttpException e) {
            if (e.getStatusCode() == 404) {
                throw new NotFoundException("Application not found: " + domain);
            } else {
                throw e;
            }
        }
    }

    public void start() throws HttpException {
        HashMap<Object, Object> reqJson = new HashMap<>();
        reqJson.put("status","START");
        httpHelper.anypointHttpPost("/cloudhub/api/applications/" + domain+"/status", reqJson, getParent());
    }
}
