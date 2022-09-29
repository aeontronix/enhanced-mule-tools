/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "creds", description = "Set credentials in configuration", subcommands = {
        ConfigSetCredsUsernamePasswordCmd.class, ConfigSetCredsBearerCmd.class, ConfigSetCredsClientCredentialsCmd.class})
public class ConfigSetCredsCmd {
    @ParentCommand
    private ConfigCmd configCmd;

    public ConfigCmd getConfigCmd() {
        return configCmd;
    }

    public EMTCli getCli() {
        return configCmd.getCli();
    }
}
