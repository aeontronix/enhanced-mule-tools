/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class APIProvisioningConfig {
    @JsonProperty(defaultValue = "anypoint.json")
    private String descriptorLocation = "anypoint.json";
    @JsonProperty
    private List<String> accessedBy = new ArrayList<>();
    @JsonProperty
    private String apiLabel;
    @JsonProperty(defaultValue = "true")
    private boolean autoApproveAPIAccessRequest = true;

    public APIProvisioningConfig() {
    }

    public APIProvisioningConfig(Map<String, String> variables, List<String> accessedBy) {
        if (accessedBy != null) {
            this.accessedBy.addAll(accessedBy);
        }
    }

    public List<String> getAccessedBy() {
        return accessedBy;
    }

    public void addAccessedBy(String clientAppName) {
        accessedBy.add(clientAppName);
    }

    public void setAccessedBy(List<String> accessedBy) {
        this.accessedBy = accessedBy;
    }

    public String getDescriptorLocation() {
        return descriptorLocation;
    }

    public void setDescriptorLocation(String descriptorLocation) {
        this.descriptorLocation = descriptorLocation;
    }

    public String getApiLabel() {
        return apiLabel;
    }

    public void setApiLabel(String apiLabel) {
        this.apiLabel = apiLabel;
    }

    public boolean isAutoApproveAPIAccessRequest() {
        return autoApproveAPIAccessRequest;
    }

    public void setAutoApproveAPIAccessRequest(boolean autoApproveAPIAccessRequest) {
        this.autoApproveAPIAccessRequest = autoApproveAPIAccessRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof APIProvisioningConfig)) return false;
        APIProvisioningConfig that = (APIProvisioningConfig) o;
        return autoApproveAPIAccessRequest == that.autoApproveAPIAccessRequest &&
                Objects.equals(descriptorLocation, that.descriptorLocation) &&
                Objects.equals(accessedBy, that.accessedBy) &&
                Objects.equals(apiLabel, that.apiLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descriptorLocation, accessedBy, apiLabel, autoApproveAPIAccessRequest);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", APIProvisioningConfig.class.getSimpleName() + "[", "]")
                .add("descriptorLocation='" + descriptorLocation + "'")
                .add("accessedBy=" + accessedBy)
                .add("apiLabel='" + apiLabel + "'")
                .add("autoApproveAPIAccessRequest=" + autoApproveAPIAccessRequest)
                .toString();
    }
}
