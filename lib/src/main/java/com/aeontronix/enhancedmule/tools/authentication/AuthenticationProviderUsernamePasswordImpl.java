/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.authentication;

import com.aeontronix.enhancedmule.tools.util.EMHttpClient;
import com.aeontronix.enhancedmule.tools.util.EMHttpClientDefaultImpl;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloudtek.util.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationProviderUsernamePasswordImpl extends AuthenticationProvider {
    public static final String LOGIN_PATH = "/accounts/login";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String username;
    private String password;

    public AuthenticationProviderUsernamePasswordImpl(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getBearerToken(HttpHelper httpHelper) throws HttpException {
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
            Map data = objectMapper.readValue(httpHelper.httpPost(LOGIN_PATH, request),Map.class);
            return (String) data.get("access_token");
        } catch (IOException e) {
            throw new HttpException(e);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
