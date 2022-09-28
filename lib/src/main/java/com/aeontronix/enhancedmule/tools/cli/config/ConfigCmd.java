/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "config", subcommands = {ConfigSetCredentialsCmd.class, ConfigSetKeyCmd.class})
public class ConfigCmd {
    @ParentCommand
    private EMTCli cli;

    public EMTCli getCli() {
        return cli;
    }
}
