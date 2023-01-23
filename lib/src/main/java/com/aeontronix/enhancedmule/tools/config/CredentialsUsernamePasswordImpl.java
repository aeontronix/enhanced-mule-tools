/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.config;

public class CredentialsUsernamePasswordImpl implements ConfigCredentials {
    private String username;
    private String password;

    public CredentialsUsernamePasswordImpl() {
    }

    public CredentialsUsernamePasswordImpl(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
