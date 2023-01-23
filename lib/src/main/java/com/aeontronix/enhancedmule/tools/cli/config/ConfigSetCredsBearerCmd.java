/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import com.aeontronix.enhancedmule.tools.config.ConfigProfile;
import com.aeontronix.enhancedmule.tools.config.CredentialsBearerTokenImpl;
import com.aeontronix.enhancedmule.tools.util.MavenHelper;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "bearer", description = "Set username/password authentication in configuration, and add to maven settings.xml")
public class ConfigSetCredsBearerCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ConfigSetCredsBearerCmd.class);
    @ParentCommand
    private ConfigCmd parent;
    @CommandLine.Parameters(description = "Bearer token", arity = "1")
    String bearer;
    @CommandLine.Option(names = {"-ss", "--skip-maven-settings-update"},
            description = "If set to true, maven settings.xml will not be updated with bearer token",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private boolean updateSettingsXml;
    @CommandLine.Option(names = {"-sf", "--maven-settings-file"},
            description = "Maven settings.xml file location",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private File mavenSettingsFile = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "settings.xml");

    @Override
    public Integer call() throws Exception {
        final EMTCli cli = parent.getCli();
        final ConfigProfile profile = cli.getActiveProfile();
        profile.setCredentials(new CredentialsBearerTokenImpl(bearer));
        cli.saveConfig();
        MavenHelper.updateMavenSettings(mavenSettingsFile, cli.getActiveProfileId(), bearer);
        logger.info("Credentials updated");
        return 0;
    }
}
