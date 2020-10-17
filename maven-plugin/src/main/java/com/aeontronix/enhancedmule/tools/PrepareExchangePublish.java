/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Prepare a project for deployment to anypoint exchange maven by changing group ids to org ids
 */
@Mojo(name = "prepare-publish", defaultPhase = LifecyclePhase.VALIDATE)
public class PrepareExchangePublish extends AbstractOrganizationalMojo {
    @Parameter(property = "anypoint.adddistmngmt",defaultValue = "true")
    private boolean addDistributionManagement;
    @Parameter(property = "anypoint.serverid", defaultValue = "anypoint-exchange-v2")
    private String serverId;
    @Parameter(property = "anypoint.snapshotversionsuffix",required = false)
    private String snapshotVersionSuffic;
    @Parameter(defaultValue = "true", property = "anypoint.prepare.updateVersionIfSnapshot")
    private boolean updateVersionIfSnapshot;
    @Parameter(defaultValue = "false", property = "anypoint.prepare-publish.skip")
    private boolean skip;
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

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
        if (updateVersionIfSnapshot && project.getArtifact() != null && project.getArtifact().isSnapshot()) {
            if( snapshotVersionSuffic == null ) {
                snapshotVersionSuffic = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            }
            snapshotVersionSuffic = project.getVersion() + "-" + snapshotVersionSuffic;
            project.setVersion(snapshotVersionSuffic);
            if (project.getArtifact() != null) {
                project.getArtifact().setVersion(snapshotVersionSuffic);
                project.getArtifact().setVersionRange(VersionRange.createFromVersion(snapshotVersionSuffic));
            }
        }
        if (project.getDistributionManagement() == null && addDistributionManagement) {
            MavenArtifactRepository repo = new MavenArtifactRepository();
            repo.setAuthentication(new org.apache.maven.artifact.repository.Authentication(username, password));
            repo.setId(serverId);
            repo.setUrl(emClient.getExchangeMavenUrl());
            repo.setLayout(new DefaultRepositoryLayout());
            project.setReleaseArtifactRepository(repo);
        }
    }
}
