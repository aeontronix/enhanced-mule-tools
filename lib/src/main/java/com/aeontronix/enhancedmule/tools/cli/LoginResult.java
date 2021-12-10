/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.oidc.OIDCToken;
import com.aeontronix.enhancedmule.oidc.UserInfo;

public class LoginResult {
    private OIDCToken token;
    private UserInfo userInfo;

    public LoginResult(OIDCToken token, UserInfo userInfo) {
        this.token = token;
        this.userInfo = userInfo;
    }

    public OIDCToken getToken() {
        return token;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }
}
