/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.CredentialsClientCredentialsImpl;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "cc", description = "Set connected apps client credentials authentication in configuration")
public class ConfigSetCredsClientCredentialsCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ConfigSetCredsClientCredentialsCmd.class);
    @ParentCommand
    private ConfigSetCredsCmd parent;
    @Parameters(description = "Client id", arity = "1")
    private String clientId;
    @Parameters(description = "Client secret", arity = "1")
    private String clientSecret;

    @Override
    public Integer call() throws Exception {
        final EMTCli cli = parent.getCli();
        final ConfigProfile profile = cli.getConfig().getOrCreateProfile(cli.getProfileName());
        profile.setCredentials(new CredentialsClientCredentialsImpl(clientId, clientSecret));
        cli.saveConfig();
        logger.info("Credentials updated");
        return 0;
    }
}
