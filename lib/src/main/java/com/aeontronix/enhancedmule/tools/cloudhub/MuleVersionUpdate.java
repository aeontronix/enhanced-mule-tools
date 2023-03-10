/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.cloudhub;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MuleVersionUpdate {
    @JsonProperty
    private String id;
    @JsonProperty
    private String name;

    public MuleVersionUpdate() {
    }

    public MuleVersionUpdate(String id) {
        this.id = id;
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
}
