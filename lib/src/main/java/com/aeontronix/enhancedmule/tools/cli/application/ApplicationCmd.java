/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application;

import picocli.CommandLine;

@CommandLine.Command(name = "application", aliases = "ap", mixinStandardHelpOptions = true,subcommands = {
        ApplicationCreateCmd.class
})
public class ApplicationCmd {
}
