/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.config;

public class CredentialsClientCredentialsImpl implements ConfigCredentials {
    private String clientId;
    private String clientSecret;

    public CredentialsClientCredentialsImpl() {
    }

    public CredentialsClientCredentialsImpl(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
