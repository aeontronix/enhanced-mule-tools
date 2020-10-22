/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.deploy.ApplicationDeployerScriptBuilder;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

import static com.aeontronix.enhancedmule.tools.deploy.ApplicationDeployerScriptBuilder.DEFAULT_MVN_OPTIONS;

@Mojo(name = "create-deploy-script", requiresProject = true, defaultPhase = LifecyclePhase.PACKAGE)
public class CreateDeployScriptMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    @Parameter(property = "emt.deployscript.projectbasedir", defaultValue = "${project.basedir}")
    private File projectBasedir;
    @Parameter(property = "emt.deployscript.emtversion")
    private String emtVersion;
    @Parameter(property = "emt.deployscript.zipfilename", defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}-deployer.zip")
    private File deployerZip;
    @Parameter(property = "emt.deployscript.mvnopts", defaultValue = DEFAULT_MVN_OPTIONS)
    private String mvnOptions;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            new ApplicationDeployerScriptBuilder(project.getGroupId(), project.getArtifactId(),
                    project.getVersion()).withMvnWrapperDir(projectBasedir).withEMTVersion(emtVersion).buildZipArchive(deployerZip);
            DefaultArtifact zipDeployArtifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getVersion(),
                    "compile", "zip", "deployer", new DefaultArtifactHandler("zip"));
            zipDeployArtifact.setFile(deployerZip);
            project.addAttachedArtifact(zipDeployArtifact);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(),e);
        }
    }
}
