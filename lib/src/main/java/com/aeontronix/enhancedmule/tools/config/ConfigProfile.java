/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ConfigProfile {
    @JsonProperty
    private List<String> orgs;
    @JsonProperty
    private String cryptoKey;
    @JsonProperty
    private String defaultEnv;
    @JsonProperty
    private boolean anypointUPW;
    @JsonProperty
    private String accessKeyId;
    @JsonProperty
    private String accessKeySecret;

    public List<String> getOrgs() {
        return orgs;
    }

    public void setOrgs(List<String> orgs) {
        this.orgs = orgs;
    }

    public String getDefaultEnv() {
        return defaultEnv;
    }

    public void setDefaultEnv(String defaultEnv) {
        this.defaultEnv = defaultEnv;
    }

    public String getCryptoKey() {
        return cryptoKey;
    }

    public void setCryptoKey(String cryptoKey) {
        this.cryptoKey = cryptoKey;
    }

    public boolean isAnypointUPW() {
        return anypointUPW;
    }

    public void setAnypointUPW(boolean anypointUPW) {
        this.anypointUPW = anypointUPW;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }
}
