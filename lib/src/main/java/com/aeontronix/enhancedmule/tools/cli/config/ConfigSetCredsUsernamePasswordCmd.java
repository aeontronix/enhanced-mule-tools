/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.CredentialsUsernamePasswordImpl;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "upw", description = "Set username/password authentication in configuration")
public class ConfigSetCredsUsernamePasswordCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ConfigSetCredsUsernamePasswordCmd.class);
    @ParentCommand
    private ConfigCmd parent;
    @CommandLine.Parameters(description = "username", arity = "1")
    String username;
    @CommandLine.Parameters(description = "password", arity = "1")
    String password;

    @Override
    public Integer call() throws Exception {
        final EMTCli cli = parent.getCli();
        final ConfigProfile profile = cli.getActiveProfile();
        profile.setCredentials(new CredentialsUsernamePasswordImpl(username, password));
        cli.saveConfig();
        logger.info("Credentials updated");
        return 0;
    }
}
