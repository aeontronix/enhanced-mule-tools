/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.provisioning.OrganizationDescriptor;
import com.kloudtek.ktcli.CliCommand;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;

import static org.slf4j.LoggerFactory.getLogger;

@CommandLine.Command(name = "org-desc-to-md", description = "Generate markdown document from org descriptor", sortOptions = false)
public class OrgDescriptorToMarkdownCmd extends CliCommand<AnypointCli> {
    private static final Logger logger = getLogger(OrgDescriptorToMarkdownCmd.class);
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message")
    private boolean usageHelpRequested;
    @CommandLine.Parameters(description = "Org descriptor file")
    private File file;
    @CommandLine.Option(names = {"-h", "--heading-depth"}, description = "Heading depth")
    private int headingDepth = 0;

    @Override
    protected final void execute() throws Exception {
        try (Writer os = new OutputStreamWriter(System.out)) {
            OrganizationDescriptor.toMarkdown(parent.getClient(), os, file, headingDepth);
        }
    }
}
