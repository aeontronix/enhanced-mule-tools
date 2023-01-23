/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.enhancedmule.tools.config.ConfigCredentials;
import com.aeontronix.enhancedmule.tools.config.CredentialsBearerTokenImpl;
import com.aeontronix.enhancedmule.tools.config.CredentialsClientCredentialsImpl;
import com.aeontronix.enhancedmule.tools.config.CredentialsUsernamePasswordImpl;
import com.aeontronix.enhancedmule.tools.emclient.authentication.AnypointBearerTokenCredentialsProvider;
import com.aeontronix.enhancedmule.tools.emclient.authentication.CredentialsProviderAnypointBearerToken;
import com.aeontronix.enhancedmule.tools.emclient.authentication.CredentialsProviderAnypointUsernamePasswordImpl;
import com.aeontronix.enhancedmule.tools.emclient.authentication.CredentialsProviderClientCredentialsImpl;

public class CredentialsConverter {
    public static AnypointBearerTokenCredentialsProvider convert(ConfigCredentials credentials) {
        if (credentials == null) {
            return null;
        } else if (credentials instanceof CredentialsUsernamePasswordImpl) {
            return new CredentialsProviderAnypointUsernamePasswordImpl(((CredentialsUsernamePasswordImpl) credentials).getUsername(),
                    ((CredentialsUsernamePasswordImpl) credentials).getPassword());
        } else if (credentials instanceof CredentialsBearerTokenImpl) {
            return new CredentialsProviderAnypointBearerToken(((CredentialsBearerTokenImpl) credentials).getBearerToken());
        } else if (credentials instanceof CredentialsClientCredentialsImpl) {
            return new CredentialsProviderClientCredentialsImpl(((CredentialsClientCredentialsImpl) credentials).getClientId(),
                    ((CredentialsClientCredentialsImpl) credentials).getClientSecret());
        } else {
            throw new IllegalArgumentException("Invalid credentials class " + credentials.getClass().getName());
        }
    }
}
