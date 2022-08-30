/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.authentication;

import com.aeontronix.enhancedmule.tools.util.AnypointAccessToken;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.RESTException;
import com.aeontronix.restclient.RESTRequest;

public class AuthenticationProviderBearerTokenImpl extends AuthenticationProvider {
    private String anypointBearerToken;

    public AuthenticationProviderBearerTokenImpl(String anypointBearerToken) {
        this.anypointBearerToken = anypointBearerToken;
    }

    @Override
    public AnypointAccessToken getBearerToken(HttpHelper httpHelper) throws HttpException {
        return new AnypointAccessToken(anypointBearerToken);
    }

    @Override
    public String filterSecret(String resStr) {
        return resStr.replace(anypointBearerToken, "**********");
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
    }
}
