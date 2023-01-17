/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "set", description = "Set configuration parameters")
public class ConfigSetCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ConfigSetCmd.class);
    @ParentCommand
    private ConfigCmd parent;
    @CommandLine.Option(names = {"-s", "--server-url"}, description = "Set server url")
    private String serverUrl;
    @CommandLine.Option(names = {"-u", "--anypoint-url"}, description = "Set anypoint url")
    private String anypointUrl;
    @CommandLine.Option(names = {"-e", "--default-env"}, description = "Set default environment")
    private String defaultEnv;
    @CommandLine.Option(names = {"-o", "--default-org"}, description = "Set default org")
    private String defaultOrg;

    @Override
    public Integer call() throws Exception {
        final EMTCli cli = parent.getCli();
        final ConfigProfile profile = cli.getActiveProfile();
        if (anypointUrl == null && serverUrl == null && defaultEnv == null && defaultOrg == null) {
            logger.warn("No configuration option selected");
        } else {
            if (anypointUrl != null) {
                profile.setAnypointUrl(anypointUrl);
                logger.info("Anypoint URL set to " + anypointUrl);
            }
            if (serverUrl != null) {
                profile.setServerUrl(serverUrl);
                logger.info("Enhanced Mule Server URL set to " + anypointUrl);
            }
            if (defaultEnv != null) {
                profile.setDefaultEnv(defaultEnv);
                logger.info("Default environment set to " + defaultEnv);
            }
            if (defaultOrg != null) {
                profile.setDefaultOrg(defaultOrg);
                logger.info("Default org set to " + defaultOrg);
            }
            cli.saveConfig();
        }
        return 0;
    }
}
