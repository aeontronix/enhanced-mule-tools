/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.CredentialsBearerTokenImpl;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "bearer", description = "Set username/password authentication in configuration")
public class ConfigSetCredsBearerCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ConfigSetCredsBearerCmd.class);
    @ParentCommand
    private ConfigSetCredsCmd parent;
    @CommandLine.Parameters(description = "Bearer token", arity = "1")
    String bearer;

    @Override
    public Integer call() throws Exception {
        final EMTCli cli = parent.getCli();
        final ConfigProfile profile = cli.getConfig().getOrCreateProfile(cli.getProfileName());
        profile.setCredentials(new CredentialsBearerTokenImpl(bearer));
        cli.saveConfig();
        logger.info("Credentials updated");
        return 0;
    }
}
