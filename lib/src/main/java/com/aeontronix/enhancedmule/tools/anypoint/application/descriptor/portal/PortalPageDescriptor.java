/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.portal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PortalPageDescriptor {
    @JsonProperty
    private String name;
    @JsonProperty
    private String path;
    @JsonProperty
    private String content;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
