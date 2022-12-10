/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.emclient.authentication;

import com.aeontronix.enhancedmule.tools.authentication.Credentials;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.auth.AuthenticationHandler;

import java.io.IOException;

public interface CredentialsProvider {
    Credentials getCredentials() throws IOException;

    AuthenticationHandler toAuthenticationHandler(RESTClient restClient, String anypointPlatformUrl);
}
