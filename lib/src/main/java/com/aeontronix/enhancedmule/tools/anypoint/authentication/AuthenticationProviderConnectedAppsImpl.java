/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.authentication;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.URLBuilder;
import com.aeontronix.enhancedmule.tools.util.AnypointAccessToken;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.RESTException;
import com.aeontronix.restclient.RESTRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationProviderConnectedAppsImpl extends AuthenticationProvider {
    public static final String TOKEN_PATH = "/accounts/api/v2/oauth2/token";
    private String anypointPlatformUrl;
    private String clientId;
    private String clientSecret;
    private String anypointBearerToken;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthenticationProviderConnectedAppsImpl(RESTClient restClient, String anypointPlatformUrl, String clientId, String clientSecret) {
        this.anypointPlatformUrl = anypointPlatformUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public AnypointAccessToken getBearerToken(HttpHelper httpHelper) throws HttpException {
        try {
            if (StringUtils.isBlank(clientId)) {
                throw new IllegalArgumentException("Client ID is missing");
            }
            if (StringUtils.isBlank(clientSecret)) {
                throw new IllegalArgumentException("Client Secret is missing");
            }
            Map<String, String> request = new HashMap<>();
            request.put("client_id", clientId);
            request.put("client_secret", clientSecret);
            request.put("grant_type", "client_credentials");
            httpHelper.setLoginRequest(true);
            Map data = objectMapper.readValue(httpHelper.httpPost(TOKEN_PATH, request), Map.class);
            return new AnypointAccessToken((String) data.get("access_token"));
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    @Override
    public String filterSecret(String resStr) {
        return resStr.replace(clientSecret, "**********");
    }

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
        if (anypointBearerToken != null) {
            request.setHeader("Authorization", "bearer " + anypointBearerToken);
        }
    }

    @Override
    public void refreshCredential(RESTClient restClient) throws RESTException {
        anypointBearerToken = (String) restClient.get(new URLBuilder(anypointPlatformUrl).path(TOKEN_PATH).toUri()).executeAndConvertToObject(Map.class).get("access_token");
    }
}
