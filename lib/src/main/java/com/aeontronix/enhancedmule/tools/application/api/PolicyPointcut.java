/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyPointcut {
    private String methodRegex;
    private String uriTemplateRegex;

    @JsonProperty
    public String getMethodRegex() {
        return methodRegex;
    }

    public void setMethodRegex(String methodRegex) {
        this.methodRegex = methodRegex;
    }

    @JsonProperty
    public String getUriTemplateRegex() {
        return uriTemplateRegex;
    }

    public void setUriTemplateRegex(String uriTemplateRegex) {
        this.uriTemplateRegex = uriTemplateRegex;
    }
}
