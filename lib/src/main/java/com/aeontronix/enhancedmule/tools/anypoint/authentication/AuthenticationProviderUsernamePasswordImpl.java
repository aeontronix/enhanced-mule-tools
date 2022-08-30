/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.authentication;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.util.AnypointAccessToken;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.RESTException;
import com.aeontronix.restclient.RESTRequest;
import com.aeontronix.restclient.auth.AuthenticationHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationProviderUsernamePasswordImpl extends AuthenticationProvider implements AuthenticationHandler {
    public static final String LOGIN_PATH = "/accounts/login";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String bearerToken;
    private String username;
    private String password;

    public AuthenticationProviderUsernamePasswordImpl(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public AnypointAccessToken getBearerToken(HttpHelper httpHelper) throws HttpException {
        try {
            if (StringUtils.isBlank(username)) {
                throw new IllegalArgumentException("Username missing");
            }
            if (StringUtils.isBlank(password)) {
                throw new IllegalArgumentException("Password missing");
            }
            Map<String, String> request = new HashMap<>();
            request.put("username", username);
            request.put("password", password);
            httpHelper.setLoginRequest(true);
            Map data = objectMapper.readValue(httpHelper.httpPost(LOGIN_PATH, request), Map.class);
            return new AnypointAccessToken((String) data.get("access_token"));
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    @Override
    public String filterSecret(String resStr) {
        return resStr.replace(password, "**********");
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void refreshCredential(RESTClient restClient) throws RESTException {
        Map<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        final Map result = restClient.get(URI.create("https://anypoint.mulesoft.com/login")).executeAndConvertToObject(Map.class);
        bearerToken = (String) result.get("access_token");
        if (StringUtils.isBlank(bearerToken)) {
            throw new RESTException("Login service didn't return valid bearer token");
        }
    }

    @Override
    public boolean isRefreshRequired() {
        return bearerToken == null;
    }

    @Override
    public boolean isRefreshable() {
        return true;
    }

    @Override
    public void applyCredentials(RESTRequest request) {
        if (bearerToken != null) {
            request.setHeader("Authorization", "bearer " + bearerToken);
        }
    }
}
