/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractOrganizationalMojo extends AbstractAnypointMojo {
    private static final Logger logger = LoggerFactory.getLogger(AbstractOrganizationalMojo.class);
    private Organization organization;

    public synchronized Organization getOrganization() throws NotFoundException, IOException {
        if (organization == null) {
            if (org != null) {
                organization = getClient().findOrganizationByNameOrId(org);
            } else {
                organization = getClient().getUser().getOrganization();
                if( organization == null ) {
                    throw new IllegalArgumentException("Organization must be specified");
                }
            }
        }
        return organization;
    }
}
