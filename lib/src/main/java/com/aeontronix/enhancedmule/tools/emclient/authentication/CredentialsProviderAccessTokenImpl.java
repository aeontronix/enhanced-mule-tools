/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.emclient.authentication;

import com.aeontronix.enhancedmule.tools.authentication.AccessTokenCredentials;
import com.aeontronix.enhancedmule.tools.authentication.Credentials;

import java.io.IOException;

public class CredentialsProviderAccessTokenImpl implements CredentialsProvider {
    private String tokenId;
    private String tokenSecret;

    public CredentialsProviderAccessTokenImpl(String tokenId, String tokenSecret) {
        this.tokenId = tokenId;
        this.tokenSecret = tokenSecret;
    }

    @Override
    public Credentials getCredentials() throws IOException {
        return new AccessTokenCredentials(tokenId, tokenSecret);
    }
}
