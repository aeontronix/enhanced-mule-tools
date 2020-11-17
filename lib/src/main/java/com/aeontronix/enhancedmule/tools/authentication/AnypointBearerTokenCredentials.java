/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.authentication;

import java.util.HashMap;
import java.util.Map;

public class AnypointBearerTokenCredentials implements Credentials {
    private String bearerToken;

    public AnypointBearerTokenCredentials() {
    }

    public AnypointBearerTokenCredentials(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @Override
    public Map<String,String> toAuthRequestPayload() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("type","AN_BEARER");
        map.put("secret",bearerToken);
        return map;
    }
}
