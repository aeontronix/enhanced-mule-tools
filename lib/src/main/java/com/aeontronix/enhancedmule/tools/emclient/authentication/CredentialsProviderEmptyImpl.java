/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.emclient.authentication;

import com.aeontronix.enhancedmule.tools.authentication.Credentials;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.RESTException;
import com.aeontronix.restclient.RESTRequest;
import com.aeontronix.restclient.auth.AuthenticationHandler;

import java.io.IOException;

public class CredentialsProviderEmptyImpl implements AnypointBearerTokenCredentialsProvider {
    @Override
    public String getAnypointBearerToken(EnhancedMuleClient restClient) throws IOException {
        return "NOBEARER";
    }

    @Override
    public Credentials getCredentials() throws IOException {
        throw new IOException("No credentials provided");
    }

    @Override
    public AuthenticationHandler toAuthenticationHandler(RESTClient restClient, String anypointPlatformUrl) {
        return new AuthenticationHandler() {
            @Override
            public boolean isRefreshRequired() {
                return false;
            }

            @Override
            public boolean isRefreshable() {
                return false;
            }

            @Override
            public void applyCredentials(RESTRequest request) {
                throw new RuntimeException("No credentials provided");
            }

            @Override
            public void refreshCredential(RESTClient restClient) throws RESTException {
            }
        };
    }
}
