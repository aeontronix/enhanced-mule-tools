/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.emclient.authentication;

import com.aeontronix.enhancedmule.tools.authentication.AccessTokenCredentials;
import com.aeontronix.enhancedmule.tools.authentication.Credentials;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;

import java.io.IOException;

public class CredentialsProviderClientCredentialsImpl implements AnypointBearerTokenCredentialsProvider {
    private String tokenId;
    private String tokenSecret;

    public CredentialsProviderClientCredentialsImpl(String tokenId, String tokenSecret) {
        this.tokenId = tokenId;
        this.tokenSecret = tokenSecret;
    }

    @Override
    public Credentials getCredentials() throws IOException {
        return new AccessTokenCredentials(tokenId, tokenSecret);
    }

    @Override
    public String getAnypointBearerToken(EnhancedMuleClient restClient) throws IOException {
        return restClient.getLegacyRestClient().get("/anypoint/bearer").execute(String.class);
    }
}
