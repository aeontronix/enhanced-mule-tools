/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.EMConfig;
import com.aeontronix.enhancedmule.tools.cli.util.VersionHelper;
import com.aeontronix.enhancedmule.tools.client.EMTClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(name = "emt", subcommands = {LoginCmd.class}, versionProvider = VersionHelper.class)
public class EMTCli {
    @Option(names = {"--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;
    @Option(names = {"?", "-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;
    @Option(names = "-p", description = "Profile")
    private String profileName;
    private EMTClient client;
    private ConfigProfile configProfile;
    private EMConfig configFile;

    public EMTCli() throws IOException {
    }

    public ConfigProfile getConfigProfile() throws IOException {
        if( configProfile == null ) {
            configProfile = getConfig().getOrCreateProfile(profileName);
        }
        return configProfile;
    }

    private EMConfig getConfig() throws IOException {
        if( configFile == null ) {
            configFile = EMConfig.findConfigFile();
        }
        return configFile;
    }

    public EMTClient getClient() throws IOException {
        if( client == null ) {
            client = new EMTClient(getConfigProfile());
        }
        return client;
    }

    public void clearClient() {
        client = null;
    }

    public void saveConfig() throws IOException {
        getConfig().save();
    }
}
