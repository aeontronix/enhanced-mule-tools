/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PropertyDescriptor {
    @JsonProperty
    private String name;
    @JsonProperty
    private String description;
    @JsonProperty
    private boolean file;
    @JsonProperty
    private boolean secure;
    @JsonProperty("static")
    private boolean staticProperty;
    @JsonProperty
    private boolean required;
    @JsonProperty("default")
    private Object defaultValue;
    @JsonProperty
    private Object values;

    public PropertyDescriptor() {
    }

    public PropertyDescriptor(String name, boolean secure) {
        this.name = name;
        this.secure = secure;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isStaticProperty() {
        return staticProperty;
    }

    public void setStaticProperty(boolean staticProperty) {
        this.staticProperty = staticProperty;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getValues() {
        return values;
    }

    public void setValues(Object values) {
        this.values = values;
    }
}
