/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.commons.logging.SimpleLogFormatter;
import com.aeontronix.enhancedmule.tools.config.ProfileNotFoundException;
import com.aeontronix.enhancedmule.tools.util.MavenExecutor;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) throws IOException, ProfileNotFoundException {
        final CLILogFormatter logFormatter = new CLILogFormatter();
        Logger logger = Logger.getLogger("");
        for (final Handler handler : logger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                handler.setFormatter(logFormatter);
            }
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("mvn")) {
            System.exit(MavenExecutor.execute(new File("."), Arrays.asList(Arrays.copyOfRange(args, 1, args.length))));
        } else {
            final EMTCli cli = new EMTCli();
            final CommandLine commandLine = new CommandLine(cli);
            commandLine.addSubcommand(new ShellCmd());
            commandLine.addSubcommand(new MavenCmd());
            commandLine.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.ON));
            commandLine.setUsageHelpAutoWidth(true);
            commandLine.setCaseInsensitiveEnumValuesAllowed(true);
            commandLine.setPosixClusteredShortOptionsAllowed(false);
            if (args != null && args.length > 0 && "--debug".equalsIgnoreCase(args[0])) {
                System.out.println("Parameters: " + Arrays.asList(args));
            }
            try {
                commandLine.parseArgs(args);
            } catch (Exception e) {
                logger.log(Level.FINE, e.getMessage(), e);
            }
            commandLine.setExecutionExceptionHandler((ex, cl, parseResult) -> {
                if (cli.isDebug()) {
                    throw ex;
                } else {
                    logger.log(Level.SEVERE, ex.getMessage());
                    return -1;
                }
            });
            logFormatter.setDebug(cli.isDebug());
            System.exit(commandLine.execute(args));
        }
    }

    public static void setupSimpleLogging(Level lvl, boolean showLevel, boolean showTimestamp) {
        Logger logger = Logger.getLogger("");
        for (final Handler handler : logger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                final SimpleLogFormatter logFormatter = new SimpleLogFormatter();
                logFormatter.setShowLevel(showLevel);
                logFormatter.setShowTimestamp(showTimestamp);
                logFormatter.setSeparator(": ");
                handler.setFormatter(logFormatter);
            }
        }
        setLoggingLevel(lvl);
    }

    public static void setLoggingLevel(Level lvl) {
        Logger logger = Logger.getLogger("");
        logger.setLevel(lvl);
        for (final Handler handler : logger.getHandlers()) {
            handler.setLevel(lvl);
        }
    }
}
