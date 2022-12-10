/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.emclient.authentication;

import com.aeontronix.enhancedmule.tools.authentication.AnypointBearerTokenCredentials;
import com.aeontronix.enhancedmule.tools.authentication.Credentials;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.auth.AuthenticationHandler;
import com.aeontronix.restclient.auth.BearerTokenAuthenticationHandler;

import java.io.IOException;

public class CredentialsProviderAnypointBearerToken implements CredentialsProvider, AnypointBearerTokenCredentialsProvider {
    private String anypointBearerToken;

    @Override
    public Credentials getCredentials() throws IOException {
        return new AnypointBearerTokenCredentials(anypointBearerToken);
    }

    @Override
    public AuthenticationHandler toAuthenticationHandler(RESTClient restClient, String anypointPlatformUrl) {
        return new BearerTokenAuthenticationHandler(anypointBearerToken);
    }

    public CredentialsProviderAnypointBearerToken(String anypointBearerToken) {
        this.anypointBearerToken = anypointBearerToken;
    }

    @Override
    public String getAnypointBearerToken(EnhancedMuleClient restClient) throws IOException {
        return anypointBearerToken;
    }
}
