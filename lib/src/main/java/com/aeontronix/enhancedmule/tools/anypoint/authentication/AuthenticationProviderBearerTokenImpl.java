/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.authentication;

import com.aeontronix.enhancedmule.tools.util.AnypointAccessToken;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import org.apache.http.client.methods.HttpRequestBase;

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
        return resStr.replace(anypointBearerToken,"**********");
    }

    @Override
    public void applyCredentials(HttpRequestBase request) {
        request.setHeader("Authorization","bearer "+anypointBearerToken);
    }
}
