/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.emclient.authentication;

import com.aeontronix.commons.URLBuilder;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProviderUsernamePasswordImpl;
import com.aeontronix.enhancedmule.tools.authentication.AnypointUsernamePasswordCredentials;
import com.aeontronix.enhancedmule.tools.authentication.Credentials;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.RESTException;
import com.aeontronix.restclient.auth.AuthenticationHandler;
import com.aeontronix.restclient.json.JsonConvertionException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CredentialsProviderAnypointUsernamePasswordImpl implements AnypointBearerTokenCredentialsProvider {
    private String username;
    private String password;

    public CredentialsProviderAnypointUsernamePasswordImpl(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Credentials getCredentials() throws IOException {
        return new AnypointUsernamePasswordCredentials(username, password);
    }

    @Override
    public AuthenticationHandler toAuthenticationHandler(RESTClient restClient, String anypointPlatformUrl) {
        return new AuthenticationProviderUsernamePasswordImpl(username, password);
    }

    @Override
    public String getAnypointBearerToken(EnhancedMuleClient emClient) throws IOException {
        try {
            final String loginUrl = new URLBuilder(emClient.getAnypointPlatformUrl()).path("/accounts/login").toString();
            Map<String, String> loginReq = new HashMap<>();
            loginReq.put("username", username);
            loginReq.put("password", password);
            final Map response = emClient.getLegacyAnypointClient().getAnypointRestClient().post(loginUrl).jsonBody(loginUrl).executeAndConvertToObject(Map.class);
            final String accessToken = (String) response.get("access_token");
            if (accessToken == null) {
                throw new IOException("No access token returned by anypoint login");
            }
            return accessToken;
        } catch (RESTException | JsonConvertionException e) {
            throw new IOException(e);
        }
    }
}
