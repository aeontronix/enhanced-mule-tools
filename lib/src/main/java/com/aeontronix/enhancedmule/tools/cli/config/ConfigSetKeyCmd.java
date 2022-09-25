/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.Credential;
import com.aeontronix.enhancedmule.config.CredentialType;
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
    @Parameters(arity = "1", index = "0", description = "Credential type (PASSWORD, ACCESS or REFRESH)")
    private CredentialType type;
    @Parameters(arity = "1", index = "1", description = "Credential id")
    private String credentialId;
    @Parameters(arity = "1", index = "2", description = "Credential secret")
    private String credentialSecret;

    @Override
    public Integer call() throws Exception {
        final EMTCli cli = configCmd.getCli();
        final ConfigProfile profile = cli.getConfig().getOrCreateProfile(cli.getProfileName());
        profile.setCredential(new Credential(credentialId, credentialSecret, type));
        cli.saveConfig();
        return null;
    }
}
