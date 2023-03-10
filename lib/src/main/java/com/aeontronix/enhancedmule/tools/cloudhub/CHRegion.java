/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.cloudhub;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CHRegion {
    private String id;
    private String name;
    private boolean defaultRegion;

    public CHRegion() {
    }

    public CHRegion(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("default")
    public boolean isDefaultRegion() {
        return defaultRegion;
    }

    public void setDefaultRegion(boolean defaultRegion) {
        this.defaultRegion = defaultRegion;
    }
}
