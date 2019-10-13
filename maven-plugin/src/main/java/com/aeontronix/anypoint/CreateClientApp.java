/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.anypoint;

import com.aeontronix.anypoint.api.ClientApplication;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "create-client-app", requiresProject = false)
public class CreateClientApp extends AbstractOrganizationalMojo {
    @Parameter(name = "name", property = "anypoint.clientapp.name", required = true)
    private String name;
    @Parameter(name = "url", property = "anypoint.clientapp.url", required = true)
    private String url;
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
