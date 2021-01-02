/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.anypoint.api;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class APIPortal extends AnypointObject<API> {
    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    @JsonIgnore
    private API apiVersion;

    public APIPortal() {
    }

    public APIPortal(API apiVersion) {
        super(apiVersion);
        this.apiVersion = apiVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public API getApiVersion() {
        return apiVersion;
    }

}
