/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.tools.cli.CredentialsArgs;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "creds", description = "Set credentials in selected profile")
public class ConfigSetCredentialsCmd implements Callable<Integer> {
    @CommandLine.Option(names = {"?", "-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;
    @ParentCommand
    private ConfigCmd configCmd;
    @ArgGroup(exclusive = false, multiplicity = "0..1")
    private CredentialsArgs credentials;

    @Override
    public Integer call() throws Exception {
        final EMTCli cli = configCmd.getCli();
        final ConfigProfile profile = cli.getConfig().getOrCreateProfile(cli.getProfileName());
        profile.setCredentials(credentials.getCredentials());
        cli.saveConfig();
        return null;
    }
}
