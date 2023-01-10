/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli.cloudhub;

import com.aeontronix.enhancedmule.tools.cli.AbstractCommand;
import com.aeontronix.enhancedmule.tools.cli.cloudhub.application.CHApplicationCmd;
import picocli.CommandLine.Command;

@Command(name = "cloudhub", subcommands = {CHApplicationCmd.class})
public class CloudhubCmd extends AbstractCommand {
}
