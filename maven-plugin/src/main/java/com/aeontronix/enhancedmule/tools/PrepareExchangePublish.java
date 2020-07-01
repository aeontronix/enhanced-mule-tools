/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.util.MavenUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.repository.AuthenticationSelector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Prepare a project for deployment to anypoint exchange maven.
 */
@Mojo(name = "prepare-exchange-publish", defaultPhase = LifecyclePhase.VALIDATE)
public class PrepareExchangePublish extends AbstractOrganizationalMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;
    /**
     * Maven exchange domain (change to '<pre>maven.eu1.anypoint.mulesoft.com</pre>' for europe anypoint)
     */
    @Parameter(defaultValue = "maven.anypoint.mulesoft.com")
    private String mavenExchangeDomain;
    @Parameter(defaultValue = "false", property = "anypoint.prepare.updateVersionIfSnapshot")
    private boolean updateVersionIfSnapshot;
    @Parameter(defaultValue = "false")
    private boolean addDistributionManagement;
    @Parameter(defaultValue = "false")
    private boolean addUserOrgsRepos;
    @Parameter(defaultValue = "false", property = "anypoint.prepare.skip")
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
            if (addDistributionManagement) {
                MavenArtifactRepository repo = new MavenArtifactRepository();
                repo.setAuthentication(new org.apache.maven.artifact.repository.Authentication(username, password));
                repo.setId("exchange-maven-" + organization.getId());
                repo.setUrl("https://" + mavenExchangeDomain + "/api/v1/organizations/" + organization.getId() + "/maven");
                repo.setLayout(new DefaultRepositoryLayout());
                project.setReleaseArtifactRepository(repo);
            }
            if (addUserOrgsRepos) {
                AuthenticationSelector authenticationSelector = session.getRepositorySession().getAuthenticationSelector();
                List<Organization> orgs = getClient().getUser().getMemberOfOrganizations();
                if (orgs != null) {
                    for (Organization org : orgs) {
                        MavenUtils.addRepositoryUsernamePassword(authenticationSelector, "exchange-maven-" + org.getId(), username, password);
                    }
                }
            }
        }
    }
}
