/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.emclient.authentication;

import com.aeontronix.enhancedmule.tools.authentication.Credentials;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;

import java.io.IOException;

public class CredentialsProviderEmptyImpl implements AnypointBearerTokenCredentialsProvider {
    @Override
    public String getAnypointBearerToken(EnhancedMuleClient restClient) throws IOException {
        throw new IOException("No credentials provided");
    }

    @Override
    public Credentials getCredentials() throws IOException {
        throw new IOException("No credentials provided");
    }
}
