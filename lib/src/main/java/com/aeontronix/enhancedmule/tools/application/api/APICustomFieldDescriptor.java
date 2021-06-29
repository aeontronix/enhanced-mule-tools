/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class APICustomFieldDescriptor {
    @JsonProperty(required = true)
    private String key;
    @JsonProperty(required = true)
    private Object value;
    @JsonProperty()
    private boolean required;

    public APICustomFieldDescriptor() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
