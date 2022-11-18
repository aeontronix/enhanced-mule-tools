/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.emclient.authentication;

import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProviderConnectedAppsImpl;
import com.aeontronix.enhancedmule.tools.authentication.AccessTokenCredentials;
import com.aeontronix.enhancedmule.tools.authentication.Credentials;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.RESTException;
import com.aeontronix.restclient.auth.AuthenticationHandler;
import com.aeontronix.restclient.json.JsonConvertionException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CredentialsProviderClientCredentialsImpl implements AnypointBearerTokenCredentialsProvider {
    private String clientId;
    private String clientSecret;

    public CredentialsProviderClientCredentialsImpl(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public Credentials getCredentials() throws IOException {
        return new AccessTokenCredentials(clientId, clientSecret);
    }

    @Override
    public AuthenticationHandler toAuthenticationHandler(RESTClient restClient, String anypointPlatformUrl) {
        return new AuthenticationProviderConnectedAppsImpl(restClient, anypointPlatformUrl, clientId, clientSecret);
    }

    @Override
    public String getAnypointBearerToken(EnhancedMuleClient emClient) throws IOException {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("grant_type", "client_credentials");
            body.put("client_id", clientId);
            body.put("client_secret", clientSecret);
            return (String) emClient.getAnypointRestClient().post("/accounts/api/v2/oauth2/token").jsonBody(body).executeAndConvertToObject(Map.class)
                    .get("access_token");
        } catch (RESTException | JsonConvertionException e) {
            throw new IOException(e);
        }
    }
}
