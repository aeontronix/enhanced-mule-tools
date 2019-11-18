/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloudtek.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationProviderClientCredentialsImpl implements AuthenticationProvider {
    private String clientId;
    private String clientSecret;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthenticationProviderClientCredentialsImpl(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public String getBearerToken(HttpHelper httpHelper) throws HttpException {
        try {
            if (StringUtils.isBlank(clientId)) {
                throw new IllegalArgumentException("Username missing");
            }
            if (StringUtils.isBlank(clientSecret)) {
                throw new IllegalArgumentException("Username missing");
            }
            Map<String, String> request = new HashMap<>();
            request.put("client_id", clientId);
            request.put("client_secret", clientSecret);
            request.put("grant_type", "client_credentials");
            Map data = objectMapper.readValue(httpHelper.httpPost("/accounts/api/v2/oauth2/token", request),Map.class);
            return (String) data.get("access_token");
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }
}
