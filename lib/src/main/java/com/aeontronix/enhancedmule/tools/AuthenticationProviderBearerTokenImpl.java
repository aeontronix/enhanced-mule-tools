/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;

public class AuthenticationProviderBearerTokenImpl implements AuthenticationProvider {
    private String bearer;

    public AuthenticationProviderBearerTokenImpl(String bearer) {
        this.bearer = bearer;
    }

    @Override
    public String getBearerToken(HttpHelper httpHelper) throws HttpException {
        return bearer;
    }
}
