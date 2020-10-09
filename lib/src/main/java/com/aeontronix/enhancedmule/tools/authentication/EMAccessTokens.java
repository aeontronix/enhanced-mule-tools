/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.authentication;

public class EMAccessTokens {
    private String accessToken;
    private String anypointAccessToken;

    public EMAccessTokens() {
    }

    public EMAccessTokens(String accessToken, String anypointAccessToken) {
        this.accessToken = accessToken;
        this.anypointAccessToken = anypointAccessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAnypointAccessToken() {
        return anypointAccessToken;
    }

    public void setAnypointAccessToken(String anypointAccessToken) {
        this.anypointAccessToken = anypointAccessToken;
    }
}
