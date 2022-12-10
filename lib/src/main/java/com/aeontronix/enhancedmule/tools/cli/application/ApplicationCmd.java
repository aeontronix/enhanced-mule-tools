/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application;

import com.aeontronix.enhancedmule.tools.cli.AbstractCommand;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(name = "application", aliases = "app", subcommands = {EnhanceApplicationCmd.class})
public class ApplicationCmd extends AbstractCommand {
    @ParentCommand
    private EMTCli cli;

    public EMTCli getCli() {
        return cli;
    }
}
