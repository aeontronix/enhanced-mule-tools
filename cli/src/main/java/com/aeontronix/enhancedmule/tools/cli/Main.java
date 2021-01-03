/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.tools.util.MavenExecutor;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equalsIgnoreCase("mvn")) {
            System.exit(MavenExecutor.execute(Arrays.asList(Arrays.copyOfRange(args, 1, args.length))));
        } else {
            final EMTCli cli = new EMTCli();
            final CommandLine commandLine = new CommandLine(cli);
            commandLine.addSubcommand(new ShellCmd());
            commandLine.addSubcommand(new MavenCmd());
            commandLine.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.ON));
            commandLine.setUsageHelpAutoWidth(true);
            commandLine.setCaseInsensitiveEnumValuesAllowed(true);
            System.exit(commandLine.execute(args));
        }
    }
}
