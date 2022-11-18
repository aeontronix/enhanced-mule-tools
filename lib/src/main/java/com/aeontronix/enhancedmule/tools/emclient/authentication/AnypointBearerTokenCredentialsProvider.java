/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.emclient.authentication;

import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;

import java.io.IOException;

public interface AnypointBearerTokenCredentialsProvider extends LegacyCredentialsProvider {
    String getAnypointBearerToken(EnhancedMuleClient restClient) throws IOException;
}
