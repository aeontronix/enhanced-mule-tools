/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.tools.cli.application.ApplicationCmd;
import com.aeontronix.enhancedmule.tools.util.VersionHelper;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "emt",mixinStandardHelpOptions = true,versionProvider = VersionHelper.class)
public class EMTCli {
    public static void main(String[] args) {
        final EMTCli cli = new EMTCli();
        final CommandLine commandLine = new CommandLine(cli);
        commandLine.addSubcommand("application",new ApplicationCmd());
        commandLine.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.ON));
        commandLine.setUsageHelpAutoWidth(true);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
