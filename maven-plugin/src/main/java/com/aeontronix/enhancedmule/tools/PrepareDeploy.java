/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.util.MavenUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.ProjectArtifact;
import org.eclipse.aether.repository.AuthenticationSelector;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Prepare a project for deployment to anypoint exchange maven.
 */
@Mojo(name = "prepare-deploy", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class PrepareDeploy extends AbstractOrganizationalMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Override
    protected void doExecute() throws Exception {
        File anypointFile = findAnypointFile(project.getBasedir());

        if( anypointFile != null ) {
            String ext = anypointFile.getPath().substring(anypointFile.getPath().lastIndexOf(".") + 1);
            Resource resource = new Resource();
            resource.setDirectory(project.getBasedir().getPath());
            resource.setIncludes(Collections.singletonList(anypointFile.getName()));
            project.addResource(resource);
            DefaultArtifact artifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getVersion(), "compile", ext, "anypoint-descriptor",
                    new DefaultArtifactHandler(ext));
            artifact.setFile(anypointFile);
            project.addAttachedArtifact(artifact);
        }
    }

    private File findAnypointFile(File basedir) {
        File file = new File(basedir, "anypoint.yml");
        if( file.exists() ) {
            return file;
        }
        file = new File(basedir, "anypoint.yaml");
        if( file.exists() ) {
            return file;
        }
        file = new File(basedir, "anypoint.json");
        if( file.exists() ) {
            return file;
        }
        return null;
    }
}
