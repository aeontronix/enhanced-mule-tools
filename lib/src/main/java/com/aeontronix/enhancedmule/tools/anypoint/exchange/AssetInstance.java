/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.anypoint.exchange;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointObject;
import com.aeontronix.enhancedmule.tools.anypoint.api.SLATier;
import com.aeontronix.enhancedmule.tools.anypoint.api.SLATierNotFoundException;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.aeontronix.commons.UnexpectedException;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

public class AssetInstance extends AnypointObject<ExchangeAsset> {
    @JsonProperty
    private String versionGroup;
    @JsonProperty
    private String organizationId;
    @JsonProperty
    private String id;
    @JsonProperty
    private String groupId;
    @JsonProperty
    private String assetId;
    @JsonProperty
    private String productAPIVersion;
    @JsonProperty
    private String version;
    @JsonProperty
    private String environmentId;
    @JsonProperty
    private String endpointUri;
    @JsonProperty
    private String name;
    @JsonProperty
    private boolean isPublic;
    @JsonProperty
    private String type;
    @JsonProperty
    private String fullname;
    @JsonProperty
    private String assetName;

    public AssetInstance() {
    }

    public List<SLATier> findSLATiers() throws HttpException {
        String json = httpHelper.httpGet("https://anypoint.mulesoft.com/exchange/api/v1/organizations/" + getParent().getParent().getId() + "/assets/" + getParent().getGroupId() + "/" + getParent().getAssetId() + "/productApiVersion/" + getParent().getProductAPIVersion() + "/instances/" + id + "/tiers");
        try {
            return jsonHelper.getJsonMapper().readValue(json,new TypeReference<List<SLATier>>(){});
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    public SLATier findSLATier(String name ) throws HttpException, SLATierNotFoundException {
        for (SLATier tier : findSLATiers()) {
            if( tier.getName().equalsIgnoreCase(name) ) {
                return tier;
            }
        }
        throw new SLATierNotFoundException("SLA Tier not found: " +name);
    }

    public String getVersionGroup() {
        return versionGroup;
    }

    public void setVersionGroup(String versionGroup) {
        this.versionGroup = versionGroup;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getProductAPIVersion() {
        return productAPIVersion;
    }

    public void setProductAPIVersion(String productAPIVersion) {
        this.productAPIVersion = productAPIVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public String getEndpointUri() {
        return endpointUri;
    }

    public void setEndpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AssetInstance.class.getSimpleName() + "[", "]")
                .add("versionGroup='" + versionGroup + "'")
                .add("organizationId='" + organizationId + "'")
                .add("id='" + id + "'")
                .add("groupId='" + groupId + "'")
                .add("assetId='" + assetId + "'")
                .add("productAPIVersion='" + productAPIVersion + "'")
                .add("version='" + version + "'")
                .add("environmentId='" + environmentId + "'")
                .add("endpointUri='" + endpointUri + "'")
                .add("name='" + name + "'")
                .add("isPublic=" + isPublic)
                .add("type='" + type + "'")
                .add("fullname='" + fullname + "'")
                .add("assetName='" + assetName + "'")
                .toString();
    }
}
