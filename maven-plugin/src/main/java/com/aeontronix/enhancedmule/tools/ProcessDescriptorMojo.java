/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.application.ApplicationEnhancer;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.aeontronix.unpack.UnpackException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Process an anypoint descriptor file and attach resulting file to project
 */
@Mojo(name = "process-descriptor", defaultPhase = LifecyclePhase.PACKAGE)
public class ProcessDescriptorMojo extends AbstractMojo {
    private static final Logger logger = getLogger(ProcessDescriptorMojo.class);
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(property = "anypoint.descriptor", required = false)
    private String descriptor;
    @Parameter(property = "anypoint.descriptor.inheritNameAndDesc", required = false, defaultValue = "true")
    private boolean inheritNameAndDesc;
    @Parameter(property = "muleplugin.compat")
    private boolean mulePluginCompatibility;

    public ProcessDescriptorMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final File generateDescriptorFile = new File(project.getBuild().getDirectory(), "anypoint.json");
            final ApplicationDescriptor applicationDescriptor = ApplicationDescriptorParser.parse(descriptor, project,
                    generateDescriptorFile, true, inheritNameAndDesc);
            if (!mulePluginCompatibility) {
                DefaultArtifact descriptorArtifactor = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getVersion(),
                        "compile", "json", "anypoint-descriptor", new DefaultArtifactHandler("json"));
                descriptorArtifactor.setFile(generateDescriptorFile);
                project.addAttachedArtifact(descriptorArtifactor);
            }
            try {
                final Artifact artifact = findAppArtifact(project);
                if (artifact != null) {
                    final File file = artifact.getFile();
                    if (file == null || !file.exists()) {
                        throw new IllegalStateException("Mule artifact not found");
                    }
                    ApplicationEnhancer.enhanceApplicationArchive(file, generateDescriptorFile, applicationDescriptor);
                } else {
                    logger.warn("No mule application attached, skipping archive enhancement");
                }
            } catch (IOException | UnpackException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private Artifact findAppArtifact(MavenProject project) {
        for (Artifact attachedArtifact : project.getAttachedArtifacts()) {
            if (attachedArtifact.getClassifier().equals("mule-application")) {
                return attachedArtifact;
            }
        }
        return null;
    }
}
