/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import com.aeontronix.enhancedmule.tools.config.ConfigProfile;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "set", description = "Set configuration parameters")
public class ConfigSetCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ConfigSetCmd.class);
    @ParentCommand
    private ConfigCmd parent;
    @Option(names = {"-c", "--clear-unset"}, description = "If this is set, then any configuration not specified in the command will be reset to default value")
    private boolean clearUnset;
    @Option(names = {"-s", "--server-url"}, description = "Set server url")
    private String serverUrl;
    @Option(names = {"-i", "--insecure-server"}, description = "Allow insecure server with invalid SSL certificate", negatable = true)
    private Boolean insecureServer;
    @Option(names = {"-u", "--anypoint-url"}, description = "Set anypoint url")
    private String anypointUrl;
    @Option(names = {"-d", "--domain"}, description = "Set anypoint org domain")
    private String orgDomain;
    @Option(names = {"-e", "--default-env"}, description = "Set default environment")
    private String defaultEnv;
    @Option(names = {"-o", "--default-org"}, description = "Set default org")
    private String defaultOrg;

    @Override
    public Integer call() throws Exception {
        final EMTCli cli = parent.getCli();
        final ConfigProfile profile = cli.getActiveProfile();
        if (anypointUrl == null && serverUrl == null && defaultEnv == null && defaultOrg == null && insecureServer == null
                && orgDomain == null) {
            logger.warn("No configuration option selected");
        } else {
            if (anypointUrl != null) {
                if (anypointUrl.equals("gov")) {
                    anypointUrl = "https://gov.anypoint.mulesoft.com/";
                }
                profile.setAnypointUrl(anypointUrl);
                logger.info("Anypoint URL set to " + anypointUrl);
            } else if (clearUnset) {
                profile.setAnypointUrl(null);
                logger.info("Anypoint URL set to default value");
            }
            if (serverUrl != null) {
                profile.setServerUrl(serverUrl);
                logger.info("Enhanced Mule Server URL set to " + anypointUrl);
            } else if (clearUnset) {
                profile.setServerUrl(null);
                logger.info("Enhanced Mule Server URL removed");
            }
            if (defaultEnv != null) {
                profile.setDefaultEnv(defaultEnv);
                logger.info("Default environment set to " + defaultEnv);
            } else if (clearUnset) {
                profile.setDefaultEnv(null);
                logger.info("Default environment removed");
            }
            if (defaultOrg != null) {
                profile.setDefaultOrg(defaultOrg);
                logger.info("Default org set to " + defaultOrg);
            } else if (clearUnset) {
                profile.setDefaultOrg(null);
                logger.info("Default org removed");
            }
            if (orgDomain != null) {
                profile.setOrgDomain(orgDomain);
                logger.info("Org domain set to " + defaultOrg);
            } else if (clearUnset) {
                profile.setDefaultOrg(null);
                logger.info("Org domain removed");
            }
            if (insecureServer != null) {
                profile.setInsecureServer(insecureServer);
                if (insecureServer) {
                    logger.info("Allowing insecure communication to enhanced mule server");
                } else {
                    logger.info("Disallowing insecure communication to enhanced mule server");
                }
            } else if (clearUnset) {
                profile.setInsecureServer(false);
                logger.info("Disallowing insecure communication to enhanced mule server");
            }
            cli.saveConfig();
        }
        return 0;
    }
}
