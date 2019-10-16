/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.alert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AlertCondition {
    @JsonProperty
    private String type;
    @JsonProperty
    private List<String> resources;
    @JsonProperty
    private String resourceType;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
}
