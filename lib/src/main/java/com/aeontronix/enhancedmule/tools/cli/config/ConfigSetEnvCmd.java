/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import com.aeontronix.enhancedmule.tools.config.ConfigProfile;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "env", description = "Set default environment in configuration")
public class ConfigSetEnvCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ConfigSetEnvCmd.class);
    @ParentCommand
    private ConfigCmd configCmd;
    @Parameters(description = "Default environment name or id", arity = "1")
    private String env;

    @Override
    public Integer call() throws Exception {
        final EMTCli cli = configCmd.getCli();
        final ConfigProfile profile = cli.getActiveProfile();
        profile.setDefaultEnv(env);
        cli.saveConfig();
        logger.info("Default environment set to " + env);
        return 0;
    }
}
