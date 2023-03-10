/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import com.aeontronix.enhancedmule.tools.config.ConfigProfile;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.net.URL;
import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "server", description = "Set server URL in configuration")
public class ConfigSetServerCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ConfigSetServerCmd.class);
    @ParentCommand
    private ConfigCmd configCmd;
    @Parameters(description = "Server URL", arity = "0..1")
    private URL serverURL;

    @Override
    public Integer call() throws Exception {
        final EMTCli cli = configCmd.getCli();
        final ConfigProfile profile = cli.getActiveProfile();
        if (serverURL != null) {
            profile.setServerUrl(serverURL.toString());
            logger.info("Server URL updated to " + serverURL.toString());
        } else {
            profile.setServerUrl(null);
            logger.info("Using enhanced mule cloud server");
        }
        cli.saveConfig();
        return 0;
    }
}
