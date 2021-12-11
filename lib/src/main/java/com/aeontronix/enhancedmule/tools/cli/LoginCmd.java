/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.tools.client.LoginHelper;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.net.URI;
import java.util.concurrent.Callable;

import static java.io.File.separator;
import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "login")
public class LoginCmd implements Callable<Void> {
    private static final Logger logger = getLogger(LoginCmd.class);
    @Parameters(index = "0")
    private URI serverUrl;
    @CommandLine.Option(names = {"--maven-settings-file", "-mf"})
    private File mvnSettingsFile = new File(System.getProperty("user.home") + separator + ".m2" + separator + "settings.xml");
    @CommandLine.Option(names = {"--maven-settings", "-ms"})
    private String mavenSettings;
    @ParentCommand
    private EMTCli cli;

    @Override
    public Void call() throws Exception {
        final ConfigProfile configProfile = cli.getConfigProfile();
        configProfile.setServerUrl(serverUrl);
        cli.clearClient();
        LoginHelper.login(mvnSettingsFile, mavenSettings, cli.getClient(), cli.getConfigProfile());
        cli.saveConfig();
        return null;
    }


}
