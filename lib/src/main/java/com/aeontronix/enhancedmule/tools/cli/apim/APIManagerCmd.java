/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.apim;

import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import picocli.CommandLine;

@CommandLine.Command(name = "api-manager", aliases = "apim", subcommands = {APIManagerDumpCmd.class})
public class APIManagerCmd {
    @CommandLine.ParentCommand
    private EMTCli cli;

    public EMTCli getCli() {
        return cli;
    }
}
