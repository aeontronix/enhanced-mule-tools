/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning.portal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PortalPageDescriptor {
    @JsonProperty
    private String name;
    @JsonProperty
    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
