/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "key", description = "Set encryption key in configuration")
public class ConfigSetKeyCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ConfigSetKeyCmd.class);
    @ParentCommand
    private ConfigCmd configCmd;
    @Parameters(description = "Encryption key", arity = "1")
    private String key;

    @Override
    public Integer call() throws Exception {
        final EMTCli cli = configCmd.getCli();
        final ConfigProfile profile = cli.getActiveProfile();
        profile.setCryptoKey(key);
        cli.saveConfig();
        logger.info("Encryption key updated");
        return 0;
    }
}
