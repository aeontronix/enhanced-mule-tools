/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.config;

import com.aeontronix.enhancedmule.tools.EnhancedMuleClient;

import java.io.IOException;

public interface AnypointBearerTokenCredentialsProvider extends CredentialsProvider {
    String getAnypointBearerToken(EnhancedMuleClient restClient) throws IOException;
}
