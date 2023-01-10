/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli.cloudhub.application;

import com.aeontronix.enhancedmule.tools.cli.AbstractCommand;
import picocli.CommandLine.Command;

@Command(name = "application", subcommands = {CHApplicationDownloadCmd.class})
public class CHApplicationCmd extends AbstractCommand {
}
