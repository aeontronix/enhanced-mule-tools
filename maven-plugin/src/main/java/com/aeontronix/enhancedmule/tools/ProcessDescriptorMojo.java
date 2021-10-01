/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationEnhancer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;

import java.io.File;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Process an anypoint descriptor file and attach resulting file to project
 */
@Mojo(name = "process-descriptor", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class ProcessDescriptorMojo extends AbstractMojo {
    private static final Logger logger = getLogger(ProcessDescriptorMojo.class);
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(property = "anypoint.descriptor", required = false)
    private String descriptor;
    @Parameter(property = "anypoint.descriptor.parent", required = false)
    private String parentDescriptor;
    @Parameter(property = "anypoint.descriptor.inheritNameAndDesc", required = false, defaultValue = "true")
    private boolean inheritNameAndDesc;
    @Parameter(property = "muleplugin.compat")
    private boolean mulePluginCompatibility;
    @Parameter(property = "anypoint.descriptor.autodiscovery.excludeIgnoreBasePath")
    private boolean excludeIgnoreBasePath;
    @Parameter(property = "emt.asset.pages", defaultValue = "${project.basedir}${file.separator}src${file.separator}main${file.separator}pages")
    private File assetPagesDir;
    @Parameter(property = "emt.apispecdir", defaultValue = "${project.basedir}${file.separator}src${file.separator}main${file.separator}resources${file.separator}api")
    private File apiSpecDir;

    public ProcessDescriptorMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if( true ) {
            throw new MojoExecutionException("Please remove process-descriptor mojo which isn't used in Enhance Mule Tools 2.x");
        }
        try {
            final File generateDescriptorFile = new File(project.getBuild().getDirectory(), "anypoint.json");
//            final ApplicationSourceMetadataProjectSourceImpl applicationSourceMetadata = new ApplicationSourceMetadataProjectSourceImpl(project, assetPagesDir, apiSpecDir);
//            ApplicationDescriptorProcessor processor = new ApplicationDescriptorProcessorImpl(descriptor, project,
//                    assetPagesDir, apiSpecDir, applicationSourceMetadata);
//            processor.legacyConvert(); // WONT MIGRATE
//            processor.setDefaultValues(inheritNameAndDesc); // MIGRATED
//            processor.writeToFile(generateDescriptorFile, true);
            if (!mulePluginCompatibility) {
                DefaultArtifact descriptorArtifactor = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getVersion(),
                        "compile", "json", "anypoint-descriptor", new DefaultArtifactHandler("json"));
                descriptorArtifactor.setFile(generateDescriptorFile);
                project.addAttachedArtifact(descriptorArtifactor);
            }
            Artifact artifact = findAppArtifact("mule-application");
            boolean light = false;
            if (artifact == null) {
                artifact = findAppArtifact("mule-application-light-package");
                if (artifact != null) {
                    light = true;
                }
            }
            if (artifact != null) {
                final File file = artifact.getFile();
                if (file == null || !file.exists()) {
                    throw new IllegalStateException("Mule artifact not found");
                }
//                ApplicationEnhancer.enhanceApplicationArchive(file, generateDescriptorFile,  // MIGRATED
//                        processor.getAnypointDescriptor(), light, excludeIgnoreBasePath);    // MIGRATED
            } else {
                logger.warn("No mule application attached, skipping archive enhancement");
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private Artifact findAppArtifact(String classifier) {
        for (Artifact attachedArtifact : project.getAttachedArtifacts()) {
            final String cl = attachedArtifact.getClassifier();
            logger.info("attached artifact: " + cl + " / " + attachedArtifact.getType());
            if (cl.equals(classifier)) {
                return attachedArtifact;
            }
        }
        return null;
    }
}
