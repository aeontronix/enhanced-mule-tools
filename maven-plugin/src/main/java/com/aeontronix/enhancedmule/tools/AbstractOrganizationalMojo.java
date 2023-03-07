/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.config.ProfileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractOrganizationalMojo extends AbstractAnypointMojo {
    private static final Logger logger = LoggerFactory.getLogger(AbstractOrganizationalMojo.class);
    private Organization organization;

    public synchronized Organization getOrganization() throws NotFoundException, IOException, ProfileNotFoundException {
        if (organization == null) {
            final LegacyAnypointClient client = getLegacyClient();
            if (org == null) {
                org = configProfile.getDefaultOrg();
            }
            if (org != null) {
                organization = client.findOrganizationByNameOrId(org);
            } else {
                organization = client.getUser().getOrganization();
                organization.setClient(client);
                if (organization == null) {
                    throw new IllegalArgumentException("Organization not set, use configuration element 'org' or maven property 'anypoint.org' to set");
                }
            }
            logger.info("Organization Name: {}", organization.getName());
            logger.info("Organization Id: {}", organization.getId());
        }
        return organization;
    }
}
