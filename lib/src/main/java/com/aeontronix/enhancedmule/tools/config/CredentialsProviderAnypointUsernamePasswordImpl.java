/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.config;

import com.aeontronix.enhancedmule.tools.EnhancedMuleClient;
import com.aeontronix.commons.URLBuilder;

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
    public String getAnypointBearerToken(EnhancedMuleClient emClient) throws IOException {
        final String loginUrl = new URLBuilder(emClient.getAnypointPlatformUrl()).path("/accounts/login").toString();
        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("username", username);
        loginReq.put("password", password);
        final Map response = emClient.getRestClient().postJson(loginUrl, loginReq).execute(Map.class);
        final String accessToken = (String) response.get("access_token");
        if(accessToken == null) {
            throw new IOException("No access token returned by anypoint login");
        }
        return accessToken;
    }
}
