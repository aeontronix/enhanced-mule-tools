/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.authentication;

import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;

public class AuthenticationProviderBearerTokenImpl extends AuthenticationProvider {
    private String bearer;

    public AuthenticationProviderBearerTokenImpl(String bearer) {
        this.bearer = bearer;
    }

    @Override
    public String getBearerToken(HttpHelper httpHelper) throws HttpException {
        return bearer;
    }
}
