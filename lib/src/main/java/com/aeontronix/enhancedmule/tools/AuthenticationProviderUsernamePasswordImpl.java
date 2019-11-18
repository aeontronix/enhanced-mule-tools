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

public class AuthenticationProviderUsernamePasswordImpl implements AuthenticationProvider {
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
                throw new IllegalArgumentException("Username missing");
            }
            Map<String, String> request = new HashMap<>();
            request.put("username", username);
            request.put("password", password);
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
