/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application.api;

import com.aeontronix.enhancedmule.tools.anypoint.api.API;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.StringJoiner;

public class APIAccessDescriptor {
    private String orgId;
    private String groupId;
    private String assetId;
    private String env;
    private String assetVersion;
    private String label;
    private String slaTier;
    private Boolean approve;

    public APIAccessDescriptor() {
    }

    public APIAccessDescriptor(String groupId, String assetId, String assetVersion) {
        this(groupId, assetId, assetVersion, null, null);
    }

    public APIAccessDescriptor(String groupId, String assetId, String assetVersion, String label, String slaTier) {
        this.groupId = groupId;
        this.assetId = assetId;
        this.assetVersion = assetVersion;
        this.label = label;
        this.slaTier = slaTier;
    }

    public APIAccessDescriptor(API api) {
        this(api, null);
    }

    public APIAccessDescriptor(API api, String slaTier) {
        this(api.getGroupId(), api.getAssetId(), api.getAssetVersion(), api.getInstanceLabel(), slaTier);
    }

    @JsonProperty()
    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    @JsonProperty()
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @JsonProperty(required = true)
    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    @JsonProperty(required = true)
    public String getAssetVersion() {
        return assetVersion;
    }

    public void setAssetVersion(String assetVersion) {
        this.assetVersion = assetVersion;
    }

    @JsonProperty(required = false)
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty(required = false)
    public String getSlaTier() {
        return slaTier;
    }

    public void setSlaTier(String slaTier) {
        this.slaTier = slaTier;
    }

    @JsonProperty(required = false)
    public Boolean getApprove() {
        return approve;
    }

    public void setApprove(Boolean approve) {
        this.approve = approve;
    }

    @JsonProperty(required = false)
    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", APIAccessDescriptor.class.getSimpleName() + "[", "]")
                .add("orgId='" + orgId + "'")
                .add("groupId='" + groupId + "'")
                .add("assetId='" + assetId + "'")
                .add("env='" + env + "'")
                .add("assetVersion='" + assetVersion + "'")
                .add("label='" + label + "'")
                .add("slaTier='" + slaTier + "'")
                .toString();
    }
}
