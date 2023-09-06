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
            boolean debug = checkIsDebug(args);
            if (debug) {
                System.out.println("Parameters: " + Arrays.asList(args));
            }
            commandLine.setExecutionExceptionHandler((ex, cl, parseResult) -> {
                if (debug) {
                    throw ex;
                } else {
                    logger.log(Level.SEVERE, ex.getMessage());
                    return -1;
                }
            });
            if (debug) {
                logFormatter.setDebug(cli.isDebug());
                for (final Handler handler : Logger.getLogger("").getHandlers()) {
                    handler.setLevel(Level.FINEST);
                }
                Logger.getLogger("com").setLevel(Level.FINEST);
                Logger.getLogger("org").setLevel(Level.FINEST);
            }
            System.exit(commandLine.execute(args));
        }
    }

    private static boolean checkIsDebug(String[] args) throws IOException, ProfileNotFoundException {
        try {
            EMTCli emtCli = new EMTCli(false);
            CommandLine commandLine = new CommandLine(emtCli);
            commandLine.addSubcommand(new ShellCmd());
            commandLine.addSubcommand(new MavenCmd());
            commandLine.parseArgs(args);
            return emtCli.isDebug();
        } catch (Exception e) {
            return false;
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
