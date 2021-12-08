/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

public class LoginRequired extends RuntimeException {
    public LoginRequired() {
        super("Not authenticated or authentication expired, please login");
    }
}
