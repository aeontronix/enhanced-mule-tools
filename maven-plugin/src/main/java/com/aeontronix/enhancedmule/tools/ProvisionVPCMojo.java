/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.UserDisplayableException;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

//@Mojo(name = "provisionvpc", requiresProject = false)
public class ProvisionVPCMojo extends AbstractOrganizationalMojo {
    private static final Logger logger = LoggerFactory.getLogger(ProvisionVPCMojo.class);
    /**
     * Delete pre-existing VPC with same name (and all applications in associated environments) if it exists prior to creation
     */
    @Parameter(name = "delete", property = "anypoint.vpc.delete")
    private boolean delete;
    /**
     * VPC descriptor file
     */
    @Parameter(name = "file", property = "anypoint.vpc.file", required = true)
    private File file;

    @Override
    protected void doExecute() throws Exception {
        logger.info("Provisioning VPC");
        if (!file.exists()) {
            throw new UserDisplayableException("VPC descriptor file not found: " + file.getPath());
        }
        getOrganization().provisionVPC(file, delete);
        getLog().info("VPC Provisioning complete");
    }
}
