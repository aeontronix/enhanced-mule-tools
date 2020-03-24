/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.api.provision.AnypointDescriptor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.kloudtek.util.StringUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Collections;

/**
 * Prepare a project for deployment to anypoint exchange maven.
 */
@Mojo(name = "prepare-deploy", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class PrepareDeploy extends AbstractOrganizationalMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;
    @Parameter(property = "anypoint.descriptor", required = false)
    private String descriptor;

    @Override
    protected void doExecute() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
        File generateDescriptorFile = new File(project.getBuild().getDirectory(),"anypoint.json");
        objectMapper.writeValue(generateDescriptorFile, anypointDescriptor);
        Resource resource = new Resource();
        resource.setDirectory(project.getBasedir().getPath());
        resource.setIncludes(Collections.singletonList(generateDescriptorFile.getName()));
        project.addResource(resource);
        DefaultArtifact artifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getVersion(),
                "compile", "json", "anypoint-descriptor", new DefaultArtifactHandler("json"));
        artifact.setFile(generateDescriptorFile);
        project.addAttachedArtifact(artifact);
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
