/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.config;

import com.aeontronix.enhancedmule.tools.EnhancedMuleClient;

import java.io.IOException;

public class CredentialsProviderAnypointBearerToken implements CredentialsProvider, AnypointBearerTokenCredentialsProvider {
    private String anypointBearerToken;

    public CredentialsProviderAnypointBearerToken(String anypointBearerToken) {
        this.anypointBearerToken = anypointBearerToken;
    }

    @Override
    public String getAnypointBearerToken(EnhancedMuleClient restClient) throws IOException {
        return anypointBearerToken;
    }
}
