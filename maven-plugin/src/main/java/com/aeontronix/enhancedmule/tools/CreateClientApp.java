/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.api.ClientApplication;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a client application
 */
@Mojo(name = "create-client-app", requiresProject = false)
public class CreateClientApp extends AbstractOrganizationalMojo {
    private static final Logger logger = LoggerFactory.getLogger(CreateClientApp.class);
    /**
     * Client application name
     */
    @Parameter(name = "name", property = "anypoint.clientapp.name", required = true)
    private String name;
    /**
     * Client application URL
     */
    @Parameter(name = "url", property = "anypoint.clientapp.url", required = true)
    private String url;
    /**
     * Client application description
     */
    @Parameter(name = "desc", property = "anypoint.clientapp.desc", required = true)
    private String desc;

    @Override
    protected void doExecute() throws Exception {
        logger.info("Creating client application " + name);
        ClientApplication clientApp = getOrganization().findClientApplicationByName(name);
        if (clientApp != null) {
            clientApp = getOrganization().createClientApplication(name, url, desc);
            logger.info("Created");
        } else {
            logger.info("Client application already exists");
        }
    }
}
