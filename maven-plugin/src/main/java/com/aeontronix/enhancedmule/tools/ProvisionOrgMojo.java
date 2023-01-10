/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.OrganizationDescriptor;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

public class ProvisionOrgMojo extends AbstractAnypointMojo {
    @Parameter(property = "anypoint.descriptor.org", required = true)
    private File orgDescriptorFile;

    @Override
    protected void doExecute() throws Exception {
        LegacyAnypointClient client = getClient();
        OrganizationDescriptor org = client.getJsonHelper().getJsonMapper().readValue(orgDescriptorFile, OrganizationDescriptor.class);
        org.provision(client);
    }
}
