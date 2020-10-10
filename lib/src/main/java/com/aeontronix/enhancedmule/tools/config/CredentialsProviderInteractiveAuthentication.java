/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.config;

import com.aeontronix.enhancedmule.tools.EnhancedMuleClient;
import com.aeontronix.enhancedmule.tools.authentication.EMuleInteractiveAuthenticator;

import java.io.IOException;

public class CredentialsProviderInteractiveAuthentication implements AnypointBearerTokenCredentialsProvider {
    @Override
    public String getAnypointBearerToken(EnhancedMuleClient restClient) throws IOException {
        return new EMuleInteractiveAuthenticator(restClient.getRestClient()).authenticate().getAnypointAccessToken();
    }
}
