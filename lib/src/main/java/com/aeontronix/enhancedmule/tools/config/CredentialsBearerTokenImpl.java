/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.config;

public class CredentialsBearerTokenImpl implements ConfigCredentials {
    private String bearerToken;

    public CredentialsBearerTokenImpl() {
    }

    public CredentialsBearerTokenImpl(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }
}
