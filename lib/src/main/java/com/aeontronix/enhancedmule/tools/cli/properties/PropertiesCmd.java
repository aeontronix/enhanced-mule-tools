/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.properties;

import com.aeontronix.enhancedmule.tools.cli.AbstractCommand;
import picocli.CommandLine.Command;

@Command(name = "properties", aliases = "prop", subcommands = {KeyGenCmd.class, EncryptCmd.class, DecryptCmd.class,
        PropertiesGenerateDescriptorCmd.class})
public class PropertiesCmd extends AbstractCommand {
}
