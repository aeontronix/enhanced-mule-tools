/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.authentication;

import java.util.HashMap;
import java.util.Map;

public class AccessTokenCredentials implements Credentials {
    private String id;
    private String secret;

    public AccessTokenCredentials() {
    }

    public AccessTokenCredentials(String id, String secret) {
        this.id = id;
        this.secret = secret;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public Map<String,String> toAuthRequestPayload() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("id",id);
        map.put("secret",secret);
        return map;
    }
}
