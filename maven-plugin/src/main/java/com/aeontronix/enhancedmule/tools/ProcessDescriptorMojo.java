/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.api.provision.APIDescriptor;
import com.aeontronix.enhancedmule.tools.api.provision.AnypointDescriptor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.kloudtek.util.StringUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * Process an anypoint descriptor file and attach resulting file to project
 */
@Mojo(name = "process-descriptor", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ProcessDescriptorMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;
    @Parameter(property = "anypoint.descriptor", required = false)
    private String descriptor;
    @Parameter(property = "anypoint.descriptor.attach")
    private boolean attachDescriptor = true;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            AnypointDescriptor anypointDescriptor = loadDescriptor();

            processDescriptor(anypointDescriptor);

            File generateDescriptorFile = writeDescriptor(objectMapper, anypointDescriptor);

            // attach generated file to project
            Resource resource = new Resource();
            resource.setDirectory(project.getBuild().getDirectory());
            resource.setIncludes(Collections.singletonList(generateDescriptorFile.getName()));
            project.addResource(resource);
            if(attachDescriptor) {
                DefaultArtifact artifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getVersion(),
                        "compile", "json", "anypoint-descriptor", new DefaultArtifactHandler("json"));
                artifact.setFile(generateDescriptorFile);
                project.addAttachedArtifact(artifact);
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    @NotNull
    private File writeDescriptor(ObjectMapper objectMapper, AnypointDescriptor anypointDescriptor) throws IOException {
        File generateDescriptorFile = new File(project.getBuild().getDirectory(), "anypoint.json");
        objectMapper.writeValue(generateDescriptorFile, anypointDescriptor);
        return generateDescriptorFile;
    }

    private void processDescriptor(AnypointDescriptor anypointDescriptor) {
        String apiName = project.getArtifactId();
        String version = project.getVersion();
        String versionNS = version.toLowerCase().endsWith("-snapshot") ? version.substring(0, version.length() - 9) : version;
        if (anypointDescriptor.getId() == null) {
            anypointDescriptor.setId(apiName);
        }
        APIDescriptor api = anypointDescriptor.getApi();
        if (api != null) {
            if (api.getAssetId() == null) {
                Dependency dep = findRAMLDependency();
                if( dep != null ) {
                    api.setAssetId(dep.getArtifactId());
                    api.setAssetVersion(dep.getVersion());
                } else {
                    api.setAssetId(apiName + "-api");
                    if (api.getAssetVersion() == null) {
                        api.setAssetVersion(versionNS);
                    }
                }
            }
        }
    }

    private Dependency findRAMLDependency() {
        Dependency dependency = null;
        for (Dependency d : project.getDependencies()) {
            String classifier = d.getClassifier();
            if( classifier != null ) {
                if( classifier.equalsIgnoreCase("raml") || classifier.equalsIgnoreCase("oas") ) {
                    if( dependency != null ) {
                        getLog().warn("Found more than one raml/oas dependencies in pom, ignoring all");
                        return null;
                    } else {
                        dependency = d;
                    }
                }
            }
        }
        return dependency;
    }

    @NotNull
    private AnypointDescriptor loadDescriptor() throws IOException {
        AnypointDescriptor anypointDescriptor = null;
        if (StringUtils.isNotBlank(descriptor)) {
            File descriptorFile = new File(descriptor);
            anypointDescriptor = readFile(descriptorFile);
        } else {
            File descriptorFile = findAnypointFile(project.getBasedir());
            if (descriptorFile != null) {
                anypointDescriptor = readFile(descriptorFile);
            }
        }
        if (anypointDescriptor == null) {
            anypointDescriptor = new AnypointDescriptor();
        }
        return anypointDescriptor;
    }

    private AnypointDescriptor readFile(File descriptorFile) throws java.io.IOException {
        if (descriptorFile.exists()) {
            String fname = descriptorFile.getName().toLowerCase();
            ObjectMapper om;
            if (fname.endsWith(".yml") || fname.endsWith(".yaml")) {
                om = new YAMLMapper();
            } else {
                om = new ObjectMapper();
            }
            om.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
            return om.readValue(descriptorFile, AnypointDescriptor.class);
        } else {
            return null;
        }
    }

    private File findAnypointFile(File basedir) {
        File file = new File(basedir, "anypoint.yml");
        if (file.exists()) {
            return file;
        }
        file = new File(basedir, "anypoint.yaml");
        if (file.exists()) {
            return file;
        }
        file = new File(basedir, "anypoint.json");
        if (file.exists()) {
            return file;
        }
        return null;
    }
}
