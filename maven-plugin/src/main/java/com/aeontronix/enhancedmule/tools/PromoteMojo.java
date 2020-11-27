/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;


import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "promote", requiresProject = false, defaultPhase = LifecyclePhase.DEPLOY)
public class PromoteMojo extends AbstractOrganizationalMojo {
    @Parameter(property = "groupId")
    private String groupId;
    @Parameter(property = "artifactId", required = true)
    private String artifactId;
    @Parameter(property = "version", required = true)
    private String version;
    @Parameter(property = "newVersion")
    private String newVersion;

    @Override
    protected void doExecute() throws Exception {
        getOrganization().promoteExchangeApplication(emClient, groupId, artifactId, version, newVersion);
    }
}
