/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.Credential;
import com.aeontronix.enhancedmule.config.CredentialType;
import com.aeontronix.enhancedmule.config.ProfileNotFoundException;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "creds", description = "Set credentials in selected profile", mixinStandardHelpOptions = true)
public class ConfigSetCredentials implements Callable<Integer> {
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
        final ConfigProfile prof = cli.getProfile(true);
        prof.setCredential(new Credential(credentialId, credentialSecret, type));
        cli.saveConfig();
        return null;
    }
}
