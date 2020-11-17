/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.config;

import com.aeontronix.commons.URLBuilder;
import com.aeontronix.enhancedmule.tools.EnhancedMuleClient;
import com.aeontronix.enhancedmule.tools.authentication.AccessTokenCredentials;
import com.aeontronix.enhancedmule.tools.authentication.Credentials;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
