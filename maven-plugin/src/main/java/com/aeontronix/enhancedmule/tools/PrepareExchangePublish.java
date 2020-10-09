/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Prepare a project for deployment to anypoint exchange maven by changing group ids to org ids
 */
@Mojo(name = "prepare-publish", defaultPhase = LifecyclePhase.VALIDATE)
public class PrepareExchangePublish extends AbstractOrganizationalMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(defaultValue = "false", property = "anypoint.prepare-publish.skip")
    private boolean skip;

    @Override
    protected void doExecute() throws Exception {
        Organization organization = getOrganization();
        if (!skip) {
            String groupId = organization.getId();
            project.setGroupId(groupId);
            if (project.getAttachedArtifacts() != null) {
                for (Artifact attachedArtifact : project.getAttachedArtifacts()) {
                    attachedArtifact.setGroupId(groupId);
                }
            }
            if (project.getArtifact() != null) {
                project.getArtifact().setGroupId(groupId);
            }
        }
    }
}
