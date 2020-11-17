/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.authentication;

import com.aeontronix.enhancedmule.tools.util.AnypointAccessToken;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;

public class AuthenticationProviderBearerTokenImpl extends AuthenticationProvider {
    private String anypointBearerToken;

    public AuthenticationProviderBearerTokenImpl(String anypointBearerToken) {
        this.anypointBearerToken = anypointBearerToken;
    }

    @Override
    public AnypointAccessToken getBearerToken(HttpHelper httpHelper) throws HttpException {
        return new AnypointAccessToken(anypointBearerToken);
    }
}
