/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.xml.XmlUtils;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

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
        try {
            boolean apikit = isAPIKitUsed();
            final File generateDescriptorFile = new File(project.getBuild().getDirectory(), "anypoint.json");
            ApplicationDescriptorProcessor processor = new ApplicationDescriptorProcessorImpl(descriptor, project, assetPagesDir, apiSpecDir, apikit);
            processor.legacyConvert();
            processor.setDefaultValues(inheritNameAndDesc);
            processor.writeToFile(generateDescriptorFile, true);
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
                ApplicationEnhancer.enhanceApplicationArchive(file, generateDescriptorFile, processor.getAnypointDescriptor(), light, excludeIgnoreBasePath);
            } else {
                logger.warn("No mule application attached, skipping archive enhancement");
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private boolean isAPIKitUsed() throws IOException, SAXException {
        final File outputDir = new File(project.getBuild().getOutputDirectory());
        @SuppressWarnings("Convert2Lambda") final File[] files = outputDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".xml");
            }
        });
        if (files != null) {
            for (File file : files) {
                final Document xmlDoc = XmlUtils.parse(file,true);
                final Element rootEl = xmlDoc.getDocumentElement();
                if( "http://www.mulesoft.org/schema/mule/core".equals(rootEl.getNamespaceURI()) && "mule".equals(rootEl.getLocalName())) {
                    if( rootEl.getElementsByTagNameNS("http://www.mulesoft.org/schema/mule/mule-apikit","config").getLength() > 0 ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Artifact findAppArtifact(String classifier) {
        final Artifact artifact = project.getArtifact();
        if (classifier.equals(artifact.getClassifier())) {
            return artifact;
        }
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
