/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning.api;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class APIProvisioningConfig {
    @JsonProperty(defaultValue = "anypoint.json")
    private String descriptorLocation = "anypoint.json";
    @JsonProperty
    private Map<String, String> variables = new HashMap<>();
    @JsonProperty
    private List<String> accessedBy = new ArrayList<>();
    @JsonProperty
    private String apiLabel;
    @JsonProperty(defaultValue = "true")
    private boolean autoApproveAPIAccessRequest = true;

    public APIProvisioningConfig() {
    }

    public APIProvisioningConfig(Map<String, String> variables, List<String> accessedBy) {
        this.variables.putAll(variables);
        if (accessedBy != null) {
            this.accessedBy.addAll(accessedBy);
        }
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public void setVariable(String key, String value) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put(key, value);
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

    public void addVariable(String key, String value) {
        variables.put(key, value);
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
                Objects.equals(variables, that.variables) &&
                Objects.equals(accessedBy, that.accessedBy) &&
                Objects.equals(apiLabel, that.apiLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descriptorLocation, variables, accessedBy, apiLabel, autoApproveAPIAccessRequest);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", APIProvisioningConfig.class.getSimpleName() + "[", "]")
                .add("descriptorLocation='" + descriptorLocation + "'")
                .add("variables=" + variables)
                .add("accessedBy=" + accessedBy)
                .add("apiLabel='" + apiLabel + "'")
                .add("autoApproveAPIAccessRequest=" + autoApproveAPIAccessRequest)
                .toString();
    }

    public void setEnvironment(Environment environment) {
        setVariable("environment.id", environment.getId());
        setVariable("environment.name", environment.getName());
        setVariable("environment.lname", environment.getLName() );
        setOrganization(environment.getOrganization());
    }

    public void setOrganization(Organization organization) {
        setVariable("organization.name", organization.getName());
        setVariable("organization.lname", organization.getName().replace(" ", "_").toLowerCase());
    }
}
