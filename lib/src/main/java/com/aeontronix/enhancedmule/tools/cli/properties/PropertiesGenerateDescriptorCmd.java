/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.cli.properties;

import com.aeontronix.enhancedmule.tools.cli.AbstractCommand;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import java.io.File;

@CommandLine.Command(name = "generate-descriptor", aliases = {"gendesc"}, description = "Generate properties.xml descriptor from config file")
public class PropertiesGenerateDescriptorCmd extends AbstractCommand {
    @Parameters
    private File file;
    @Parameters
    private File output;
}
