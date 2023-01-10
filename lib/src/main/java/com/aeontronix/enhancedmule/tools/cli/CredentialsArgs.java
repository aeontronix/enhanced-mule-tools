/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.anypointsdk.auth.AnypointAuthenticationHandler;
import com.aeontronix.anypointsdk.auth.AnypointClientCredentialsAuthenticationHandler;
import com.aeontronix.anypointsdk.auth.AnypointUPWAuthenticationHandler;
import com.aeontronix.enhancedmule.config.ConfigCredentials;
import com.aeontronix.enhancedmule.config.CredentialsBearerTokenImpl;
import com.aeontronix.enhancedmule.config.CredentialsClientCredentialsImpl;
import com.aeontronix.enhancedmule.config.CredentialsUsernamePasswordImpl;
import com.aeontronix.restclient.auth.AuthenticationHandler;
import com.aeontronix.restclient.auth.BearerTokenAuthenticationHandler;
import picocli.CommandLine;

public class CredentialsArgs {
    @CommandLine.Option(names = {"-upw", "--username-password"}, description = "Username / Password credentials (first parameter is username, second is password)", arity = "2")
    public String[] upw;
    @CommandLine.Option(names = {"-bt", "--bearer"}, description = "Bearer token credentials")
    public String bearer;
    @CommandLine.Option(names = {"-cc", "--credential-credentials"}, description = "Credential Credentials  (first parameter is client id, second is client password)", arity = "2")
    public String[] clientCreds;

    public ConfigCredentials getCredentials() {
        if (upw != null) {
            return new CredentialsUsernamePasswordImpl(upw[0], upw[1]);
        } else if (bearer != null) {
            return new CredentialsBearerTokenImpl(bearer);
        } else if (clientCreds != null) {
            return new CredentialsClientCredentialsImpl(clientCreds[0], clientCreds[1]);
        } else {
            return null;
        }
    }

    public AuthenticationHandler getAuthenticationHandler() {
        if (upw != null) {
            return new AnypointUPWAuthenticationHandler(upw[0], upw[1]);
        } else if (bearer != null) {
            return new BearerTokenAuthenticationHandler(bearer);
        } else if (clientCreds != null) {
            return new AnypointClientCredentialsAuthenticationHandler(clientCreds[0], clientCreds[1]);
        } else {
            throw new IllegalStateException("No valid credentials specified in command or present in active configuration");
        }
    }
}
