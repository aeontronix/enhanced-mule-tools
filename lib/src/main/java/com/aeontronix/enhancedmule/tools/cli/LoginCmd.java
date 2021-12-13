/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.tools.client.LoginHelper;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.net.URI;
import java.util.concurrent.Callable;

import static java.io.File.separator;
import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "login")
public class LoginCmd implements Callable<Void> {
    private static final Logger logger = getLogger(LoginCmd.class);
    @Option(names = {"--server-url", "-s"})
    private URI serverUrl;
    @Option(names = {"--maven-settings-file", "-f"})
    private File mvnSettingsFile = new File(System.getProperty("user.home") + separator + ".m2" + separator + "settings.xml");
    @Option(names = {"--maven-settings-id", "-i"}, defaultValue = "anypoint")
    private String mavenSettingsId;
    @ParentCommand
    private EMTCli cli;

    @Override
    public Void call() throws Exception {
        LoginHelper.login(mvnSettingsFile, mavenSettingsId, cli.getClient(), cli.getConfig(), cli.getConfigProfile(), serverUrl);
        return null;
    }
}
