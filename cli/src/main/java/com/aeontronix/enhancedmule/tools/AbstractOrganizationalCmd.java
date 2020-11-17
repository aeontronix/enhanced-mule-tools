/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.kloudtek.ktcli.CliCommand;
import com.aeontronix.commons.UserDisplayableException;
import picocli.CommandLine.Option;

public abstract class AbstractOrganizationalCmd extends CliCommand<AnypointCli> {
    @Option(description = "Organization", names = {"-o", "--organizationName"})
    protected String organizationName;
    @Option(description = "Create organizationName if it doesn't exist", names = {"-co", "--create-organizationName"})
    protected boolean createOrganization = false;

    @Override
    protected final void execute() throws Exception {
        AnypointClient client = parent.getClient();
        if (organizationName == null) {
            organizationName = parent.getDefaultOrganization();
            if (organizationName == null) {
                throw new NotFoundException("Organization parameter missing");
            }
        } else if (cli.isSaveConfig()) {
            parent.setDefaultOrganization(organizationName);
        }
        Organization organization = null;
        try {
            organization = client.findOrganizationByNameOrId(organizationName);
        } catch (NotFoundException e) {
            if (createOrganization) {
                client.createOrganization(organizationName);
            } else {
                throw new UserDisplayableException("Organization not found: " + organizationName);
            }
        }
        execute(organization);
    }

    protected abstract void execute(Organization organization) throws Exception;
}
