/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.config.ConfigCredentials;
import com.aeontronix.enhancedmule.config.CredentialsBearerTokenImpl;
import com.aeontronix.enhancedmule.config.CredentialsClientCredentialsImpl;
import com.aeontronix.enhancedmule.config.CredentialsUsernamePasswordImpl;
import picocli.CommandLine;

public class CredentialsArgs {
    @CommandLine.Option(names = {"-upw", "--username-password"}, description = "Username / Password credentials", arity = "2")
    public String[] upw;
    @CommandLine.Option(names = {"-bt", "--bearer"}, description = "Bearer token credentials")
    public String bearer;
    @CommandLine.Option(names = {"-cc", "--credential-credentials"}, description = "Credential Credentials", arity = "2")
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
}
