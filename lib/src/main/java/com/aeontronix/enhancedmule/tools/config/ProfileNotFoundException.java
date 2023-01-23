/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.config;

public class ProfileNotFoundException extends Exception {
    public ProfileNotFoundException(String profile) {
        super("Profile not found: " + profile);
    }
}
