/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@Command(name = "key", description = "Set encrypted key in active profile")
public class ConfigSetKeyCmd implements Callable<Integer> {
    @CommandLine.Option(names = {"?", "-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;
    @ParentCommand
    private ConfigCmd configCmd;
    @Parameters(description = "Encryption key", arity = "1")
    private String key;

    @Override
    public Integer call() throws Exception {
        final EMTCli cli = configCmd.getCli();
        final ConfigProfile profile = cli.getConfig().getOrCreateProfile(cli.getProfileName());
        profile.setCryptoKey(key);
        cli.saveConfig();
        return null;
    }
}
