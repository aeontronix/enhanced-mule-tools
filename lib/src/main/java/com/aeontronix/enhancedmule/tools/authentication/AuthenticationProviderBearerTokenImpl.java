/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.authentication;

import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;

public class AuthenticationProviderBearerTokenImpl extends AuthenticationProvider {
    private String anypointBearerToken;

    public AuthenticationProviderBearerTokenImpl(String anypointBearerToken) {
        this.anypointBearerToken = anypointBearerToken;
    }

    @Override
    public AccessTokens getBearerToken(HttpHelper httpHelper) throws HttpException {
        return new AccessTokens(null, anypointBearerToken);
    }
}
