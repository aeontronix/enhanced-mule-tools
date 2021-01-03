/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.tools.util.VersionHelper;
import picocli.CommandLine.Command;

@Command(name = "emt",mixinStandardHelpOptions = true,versionProvider = VersionHelper.class)
public class EMTCli {
}
