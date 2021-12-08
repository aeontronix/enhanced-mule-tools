/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.oidc.OIDCToken;
import com.aeontronix.enhancedmule.tools.client.EMTClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.net.URI;
import java.util.concurrent.Callable;

@Command(name = "login")
public class LoginCmd implements Callable {
    @Parameters(index = "0")
    private URI serverUrl;
    @ParentCommand
    private EMTCli cli;

    @Override
    public Object call() throws Exception {
        final ConfigProfile configProfile = cli.getConfigProfile();
        configProfile.setServerUrl(serverUrl);
        cli.clearClient();
        final EMTClient client = cli.getClient();
        final OIDCToken tokens = client.login();
        configProfile.setBearerToken(tokens.getAccessToken());
        configProfile.setRefreshToken(tokens.getRefreshToken());
        cli.saveConfig();
        return null;
    }
}
