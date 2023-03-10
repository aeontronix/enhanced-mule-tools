/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.anypoint.alert;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Alert extends AlertUpdate {
    @JsonProperty
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
