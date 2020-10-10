/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.provisioning.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.AnypointDescriptor;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.aeontronix.commons.FileUtils;
import com.aeontronix.commons.StringUtils;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Process an anypoint descriptor file and attach resulting file to project
 */
@Mojo(name = "process-descriptor", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ProcessDescriptorMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(property = "anypoint.descriptor", required = false)
    private String descriptor;
    @Parameter(property = "muleplugin.compat")
    private boolean mulePluginCompatibility;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            ObjectMapper objectMapper = JsonHelper.createMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            Map<String,Object> anypointDescriptorJson = loadDescriptor();

            legacyConvert(anypointDescriptorJson);

            AnypointDescriptor anypointDescriptor = objectMapper.convertValue(anypointDescriptorJson, AnypointDescriptor.class);

            processDescriptor(anypointDescriptor);

            File genResDir = new File(project.getBuild().getDirectory() + File.separator+ "generated-resources");
            if(! genResDir.exists() ) {
                FileUtils.mkdirs(genResDir);
            }
            Resource resource = new Resource();
            resource.setDirectory(genResDir.getPath());
            project.addResource(resource);

            File generateDescriptorFile = new File(genResDir,"anypoint.json");
            objectMapper.writeValue(generateDescriptorFile, anypointDescriptor);

            if(!mulePluginCompatibility) {
                DefaultArtifact artifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getVersion(),
                        "compile", "json", "anypoint-descriptor", new DefaultArtifactHandler("json"));
                artifact.setFile(generateDescriptorFile);
                project.addAttachedArtifact(artifact);
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void legacyConvert(Map<String, Object> anypointDescriptor) {
        Map<String,Object> api = (Map<String, Object>) anypointDescriptor.get("api");
        if( api != null ) {
            Map<String,Object> client = (Map<String, Object>) api.remove("clientApp");
            if( client != null ) {
                anypointDescriptor.put("client",client);
            }
            Object access = api.remove("access");
            if( access != null ) {
                if( client == null ) {
                    client = new HashMap<>();
                    anypointDescriptor.put("client",client);
                }
                client.put("access",access);
            }
        }
    }

    private void processDescriptor(AnypointDescriptor anypointDescriptor) {
        String apiName = project.getArtifactId();
        String version = project.getVersion();
        if (anypointDescriptor.getId() == null) {
            anypointDescriptor.setId(apiName);
        }
        APIDescriptor api = anypointDescriptor.getApi();
        if (api != null) {
            Dependency dep = findRAMLDependency();
            if (api.getAssetId() == null) {
                if( dep != null ) {
                    api.setAssetId(dep.getArtifactId());
                    api.setAssetVersion(dep.getVersion());
                } else {
                    api.setAssetId(apiName + "-spec");
                }
            }
            if (api.getAssetVersion() == null) {
                api.setAssetVersion(version);
            }
            if(api.getVersion() == null) {
                if( dep != null ) {
                    if( dep.getClassifier().equalsIgnoreCase("oas") ) {
                        api.setVersion(api.getAssetVersion().replaceFirst("\\.\\d\\.\\d",".0.0"));
                    } else {
                        api.setVersion("v1");
                    }
                } else {
                    api.setVersion("v1");
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
    private Map<String,Object> loadDescriptor() throws IOException {
        Map<String,Object> anypointDescriptor = null;
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
            anypointDescriptor = new HashMap<>();
        }
        return anypointDescriptor;
    }

    @SuppressWarnings("unchecked")
    private Map<String,Object> readFile(File descriptorFile) throws java.io.IOException {
        if (descriptorFile.exists()) {
            String fname = descriptorFile.getName().toLowerCase();
            ObjectMapper om;
            if (fname.endsWith(".yml") || fname.endsWith(".yaml")) {
                om = new YAMLMapper();
            } else {
                om = new ObjectMapper();
            }
            om.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
            return (Map<String,Object>) om.readValue(descriptorFile, Map.class);
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
