/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.provisioning.OrganizationDescriptor;
import com.kloudtek.ktcli.CliCommand;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.File;

import static org.slf4j.LoggerFactory.getLogger;

@CommandLine.Command(name = "provision-org", description = "Provision Organization", sortOptions = false)
public class ProvisionOrgCmd extends CliCommand<AnypointCli> {
    private static final Logger logger = getLogger(ProvisionOrgCmd.class);
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message")
    private boolean usageHelpRequested;
    @CommandLine.Parameters(description = "Org descriptor file")
    private File file;
    @CommandLine.Option(names = {"-n","--provisioned-org-name"},description = "Provisioned organization Name")
    private String pOrgName;

    @Override
    protected final void execute() throws Exception {
        logger.info("Organization provisioning started");
        OrganizationDescriptor.provision(parent.getClient(), file, pOrgName);
        logger.info("Organization provisioning complete");
    }
}
