/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.config;

import com.aeontronix.enhancedmule.tools.EnhancedMuleClient;
import com.aeontronix.enhancedmule.tools.authentication.Credentials;

import java.io.IOException;

public class CredentialsProviderEmptyImpl implements AnypointBearerTokenCredentialsProvider {
    @Override
    public String getAnypointBearerToken(EnhancedMuleClient restClient) throws IOException {
        throw new IOException("No credentials provided and not in interactive mode");
    }

    @Override
    public Credentials getCredentials() throws IOException {
        throw new IOException("No credentials provided and not in interactive mode");
    }
}
