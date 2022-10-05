/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Prepare a project for deployment to anypoint exchange maven by changing group ids to org ids
 */
@Mojo(name = "prepare-publish", defaultPhase = LifecyclePhase.VALIDATE)
public class PrepareExchangePublish extends AbstractOrganizationalMojo {
    private static final Logger logger = getLogger(PrepareExchangePublish.class);
    @Parameter(property = "anypoint.adddistmngmt", defaultValue = "true")
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
        Organization organization = null;
        try {
            organization = getOrganization();
        } catch (NotFoundException | IOException e) {
            logger.warn("Unable to login to exchange to retrieve org id, skipping prepare-publish");
            return;
        }
        final Artifact projectArtifact = project.getArtifact();
        if (!skip) {
            String groupId = organization.getId();
            logger.debug("Changing project groupId to " + groupId);
            project.setGroupId(groupId);
            if (project.getAttachedArtifacts() != null) {
                for (Artifact attachedArtifact : project.getAttachedArtifacts()) {
                    try {
                        logger.debug("Changing attached artifact type {}:{} groupId to {}", attachedArtifact.getType(), attachedArtifact.getClassifier(), groupId);
                        attachedArtifact.setGroupId(groupId);
                    } catch (UnsupportedOperationException e) {
                        logger.debug("Unable to change attached artifact groupId", e);
                    }
                }
            }
            if (projectArtifact != null) {
                logger.debug("Changing project artifact type {}:{} groupId to {}", projectArtifact.getType(), projectArtifact.getClassifier(), groupId);
                projectArtifact.setGroupId(groupId);
            }
        }
        if (updateVersionIfSnapshot && projectArtifact != null && projectArtifact.isSnapshot()) {
            if (snapshotVersionSuffic == null) {
                snapshotVersionSuffic = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            }
            snapshotVersionSuffic = project.getVersion() + "-" + snapshotVersionSuffic;
            project.setVersion(snapshotVersionSuffic);
            projectArtifact.setVersion(snapshotVersionSuffic);
            projectArtifact.setVersionRange(VersionRange.createFromVersion(snapshotVersionSuffic));
        }
        if (project.getDistributionManagement() != null && addDistributionManagement) {
            MavenArtifactRepository repo = new MavenArtifactRepository();
            repo.setAuthentication(new org.apache.maven.artifact.repository.Authentication("~~~Token~~~", emClient.getAnypointBearerToken()));
            repo.setId(serverId);
            repo.setUrl(emClient.getExchangeMavenUrl());
            repo.setLayout(new DefaultRepositoryLayout());
            project.setReleaseArtifactRepository(repo);
        }
    }
}
