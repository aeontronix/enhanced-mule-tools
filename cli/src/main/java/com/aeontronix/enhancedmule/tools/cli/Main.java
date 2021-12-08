/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import picocli.CommandLine;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
            final EMTCli cli = new EMTCli();
            final CommandLine commandLine = new CommandLine(cli);
//            commandLine.addSubcommand(new ShellCmd());
//            commandLine.addSubcommand(new MavenCmd());
            commandLine.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.ON));
            commandLine.setUsageHelpAutoWidth(true);
            commandLine.setCaseInsensitiveEnumValuesAllowed(true);
            commandLine.setPosixClusteredShortOptionsAllowed(false);
            System.exit(commandLine.execute(args));
    }
}
