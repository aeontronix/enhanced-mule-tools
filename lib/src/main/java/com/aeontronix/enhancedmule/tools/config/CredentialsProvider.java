/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.config;

import com.aeontronix.enhancedmule.tools.authentication.Credentials;

import java.io.IOException;

public interface CredentialsProvider {
    Credentials getCredentials() throws IOException;
}
