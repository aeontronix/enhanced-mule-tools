/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.provisioning.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PolicyDescriptor {
    private List<PolicyPointcut> pointcutData;
    private String policyTemplateId;
    private String groupId;
    private String assetId;
    private String assetVersion;
    private Object configurationData;

    public PolicyDescriptor() {
    }

    public PolicyDescriptor(String groupId, String assetId, String assetVersion, Object configurationData) {
        this.groupId = groupId;
        this.assetId = assetId;
        this.assetVersion = assetVersion;
        this.configurationData = configurationData;
    }

    @JsonProperty
    public List<PolicyPointcut> getPointcutData() {
        return pointcutData;
    }

    public void setPointcutData(List<PolicyPointcut> pointcutData) {
        this.pointcutData = pointcutData;
    }

    @JsonProperty
    public String getPolicyTemplateId() {
        return policyTemplateId;
    }

    public void setPolicyTemplateId(String policyTemplateId) {
        this.policyTemplateId = policyTemplateId;
    }

    @JsonProperty
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @JsonProperty
    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    @JsonProperty
    public String getAssetVersion() {
        return assetVersion;
    }

    public void setAssetVersion(String assetVersion) {
        this.assetVersion = assetVersion;
    }

    @JsonProperty
    public Object getConfigurationData() {
        return configurationData;
    }

    public void setConfigurationData(Object configurationData) {
        this.configurationData = configurationData;
    }
}
