/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import org.apache.maven.plugins.annotations.Parameter;

public abstract class AbstractOrganizationalMojo extends AbstractAnypointMojo {
    private Organization organization;
    /**
     * Anypoint organization name
     */
    @Parameter(property = "anypoint.org")
    protected String org;

    public synchronized Organization getOrganization() throws NotFoundException, HttpException {
        if (organization == null) {
            if (org != null) {
                organization = getClient().findOrganization(org);
            } else {
                logger.info("Getting default org");
                organization = getClient().getUser().getOrganization();
            }
        }
        return organization;
    }
}
