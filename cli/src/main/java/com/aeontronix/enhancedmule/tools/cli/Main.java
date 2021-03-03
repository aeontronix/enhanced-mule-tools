/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.commons.logging.LoggingUtils;
import com.aeontronix.enhancedmule.config.ProfileNotFoundException;
import com.aeontronix.enhancedmule.tools.util.MavenExecutor;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) throws IOException, ProfileNotFoundException {
        LoggingUtils.setupSimpleLogging(Level.INFO, true, false);
        if (args.length > 0 && args[0].equalsIgnoreCase("mvn")) {
            System.exit(MavenExecutor.execute(new File("."),Arrays.asList(Arrays.copyOfRange(args, 1, args.length))));
        } else {
            final EMTCli cli = new EMTCli();
            final CommandLine commandLine = new CommandLine(cli);
            commandLine.addSubcommand(new ShellCmd());
            commandLine.addSubcommand(new MavenCmd());
            commandLine.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.ON));
            commandLine.setUsageHelpAutoWidth(true);
            commandLine.setCaseInsensitiveEnumValuesAllowed(true);
            commandLine.setPosixClusteredShortOptionsAllowed(false);
            System.exit(commandLine.execute(args));
        }
    }
}
