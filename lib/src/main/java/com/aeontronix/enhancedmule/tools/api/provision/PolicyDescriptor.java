/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.api.provision;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolicyDescriptor {
    private List<PolicyPointcut> pointcutData;
    private String policyTemplateId;
    private String groupId;
    private String assetId;
    private String assetVersion;
    private Map<String, Object> configurationData;

    public PolicyDescriptor() {
    }

    public PolicyDescriptor(String groupId, String assetId, String assetVersion, HashMap<String, Object> configurationData) {
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
    public Map<String, Object> getConfigurationData() {
        return configurationData;
    }

    public void setConfigurationData(Map<String, Object> configurationData) {
        this.configurationData = configurationData;
    }
}
