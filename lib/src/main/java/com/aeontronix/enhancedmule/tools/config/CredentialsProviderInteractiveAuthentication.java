/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.config;

import com.aeontronix.enhancedmule.tools.EnhancedMuleClient;

public class CredentialsProviderInteractiveAuthentication implements AnypointBearerTokenCredentialsProvider {
    @Override
    public String getAnypointBearerToken(EnhancedMuleClient restClient) {
        return null;
    }
}
