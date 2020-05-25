/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.api.provision;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClientApplicationDescriptor {
    private boolean create;
    private String url;
    private String description;
    private String name;

    public ClientApplicationDescriptor() {
    }

    public ClientApplicationDescriptor(String url, String description, String name) {
        this.url = url;
        this.description = description;
        this.name = name;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    @JsonProperty
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
