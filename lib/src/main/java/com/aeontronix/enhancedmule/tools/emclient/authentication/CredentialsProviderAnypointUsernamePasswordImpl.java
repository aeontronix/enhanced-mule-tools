/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.emclient.authentication;

import com.aeontronix.commons.URLBuilder;
import com.aeontronix.enhancedmule.tools.authentication.AnypointUsernamePasswordCredentials;
import com.aeontronix.enhancedmule.tools.authentication.Credentials;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;

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
    public String getAnypointBearerToken(EnhancedMuleClient emClient) throws IOException {
        final String loginUrl = new URLBuilder(emClient.getAnypointPlatformUrl()).path("/accounts/login").toString();
        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("username", username);
        loginReq.put("password", password);
        final Map response = emClient.getLegacyRestClient().postJson(loginUrl, loginReq).execute(Map.class);
        final String accessToken = (String) response.get("access_token");
        if(accessToken == null) {
            throw new IOException("No access token returned by anypoint login");
        }
        return accessToken;
    }
}
