/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.FileUtils;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.deploy.EnhanceMuleTransformer;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.portal.PortalPageDescriptor;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.kloudtek.unpack.FileType;
import com.kloudtek.unpack.UnpackException;
import com.kloudtek.unpack.Unpacker;
import com.kloudtek.unpack.transformer.Transformer;
import com.kloudtek.util.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Process an anypoint descriptor file and attach resulting file to project
 */
@Mojo(name = "process-descriptor", defaultPhase = LifecyclePhase.PACKAGE)
public class ProcessDescriptorMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;
    @Parameter(property = "anypoint.descriptor", required = false)
    private String descriptor;
    @Parameter(property = "muleplugin.compat")
    private boolean mulePluginCompatibility;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            ObjectMapper objectMapper = JsonHelper.createMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            Map<String, Object> anypointDescriptorJson = loadDescriptor();

            legacyConvert(anypointDescriptorJson);

            ApplicationDescriptor applicationDescriptor = objectMapper.convertValue(anypointDescriptorJson, ApplicationDescriptor.class);

            processDescriptor(applicationDescriptor);

            File buildDir = new File(project.getBuild().getDirectory());
            if (!buildDir.exists()) {
                FileUtils.mkdirs(buildDir);
            }
            File generateDescriptorFile = new File(buildDir, "anypoint.json");
            objectMapper.writeValue(generateDescriptorFile, applicationDescriptor);

            enhanceAppArchive(applicationDescriptor, generateDescriptorFile);

            if (!mulePluginCompatibility) {
                DefaultArtifact descriptorArtifactor = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getVersion(),
                        "compile", "json", "anypoint-descriptor", new DefaultArtifactHandler("json"));
                descriptorArtifactor.setFile(generateDescriptorFile);
                project.addAttachedArtifact(descriptorArtifactor);
            }
        } catch (IOException | UnpackException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void enhanceAppArchive(ApplicationDescriptor applicationDescriptor, File generateDescriptorFile) throws IOException, UnpackException {
        final Artifact artifact = findAppArtifact(project);
        if (artifact.getFile() == null || !artifact.getFile().exists()) {
            throw new IllegalStateException("Mule artifact not found");
        }
        File artifactFile = artifact.getFile();
        File oldArtifactFile = new File(artifactFile.getPath() + ".preweaving");
        if (oldArtifactFile.exists()) {
            FileUtils.delete(oldArtifactFile);
        }
        if (!artifactFile.renameTo(oldArtifactFile)) {
            throw new IOException("Unable to move " + artifactFile.getPath() + " to " + oldArtifactFile.getPath());
        }
        Unpacker unpacker = new Unpacker(oldArtifactFile, FileType.ZIP, artifactFile, FileType.ZIP);
        final ArrayList<Transformer> transformers = new ArrayList<>();
        transformers.add(new EnhanceMuleTransformer(applicationDescriptor, generateDescriptorFile));
        unpacker.addTransformers(transformers);
        unpacker.unpack();
    }

    private Artifact findAppArtifact(MavenProject project) {
        for (Artifact attachedArtifact : project.getAttachedArtifacts()) {
            if (attachedArtifact.getClassifier().equals("mule-application")) {
                return attachedArtifact;
            }
        }
        throw new IllegalStateException("Unable to find attached mule application jar file");
    }

    @SuppressWarnings("unchecked")
    private void legacyConvert(Map<String, Object> anypointDescriptor) {
        Map<String, Object> api = (Map<String, Object>) anypointDescriptor.get("api");
        if (api != null) {
            Map<String, Object> client = (Map<String, Object>) api.remove("clientApp");
            if (client != null) {
                anypointDescriptor.put("client", client);
            }
            Object access = api.remove("access");
            if (access != null) {
                if (client == null) {
                    client = new HashMap<>();
                    anypointDescriptor.put("client", client);
                }
                client.put("access", access);
            }
        }
    }

    private void processDescriptor(ApplicationDescriptor applicationDescriptor) throws MojoFailureException {
        try {
            String apiName = project.getArtifactId();
            String version = project.getVersion();
            if (applicationDescriptor.getId() == null) {
                applicationDescriptor.setId(apiName);
            }
            if (applicationDescriptor.getName() == null) {
                applicationDescriptor.setName(project.getName());
            }
            if (applicationDescriptor.getVersion() == null) {
                applicationDescriptor.setVersion(version);
            }
            APIDescriptor api = applicationDescriptor.getApi();
            if (api != null) {
                Dependency dep = findRAMLDependency();
                if (api.getAssetId() == null) {
                    if (dep != null) {
                        api.setAssetId(dep.getArtifactId());
                        api.setAssetVersion(dep.getVersion());
                    } else {
                        api.setAssetId(apiName + "-spec");
                    }
                }
                if (api.getPortal() != null && api.getPortal().getPages() != null) {
                    for (PortalPageDescriptor page : api.getPortal().getPages()) {
                        if (page.getContent() == null) {
                            try (FileInputStream fis = new FileInputStream(project.getBasedir() + File.separator + page.getPath().replace("/", File.separator))) {
                                page.setPath(null);
                                page.setContent(IOUtils.toString(fis));
                            }
                        }
                    }
                }
                if (api.getAssetVersion() == null) {
                    api.setAssetVersion(version);
                }
                if (api.getVersion() == null) {
                    if (dep != null) {
                        if (dep.getClassifier().equalsIgnoreCase("oas")) {
                            api.setVersion(api.getAssetVersion().replaceFirst("\\.\\d\\.\\d", ".0.0"));
                        } else {
                            api.setVersion("v1");
                        }
                    } else {
                        api.setVersion("v1");
                    }
                }
            }
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private Dependency findRAMLDependency() {
        Dependency dependency = null;
        for (Dependency d : project.getDependencies()) {
            String classifier = d.getClassifier();
            if (classifier != null) {
                if (classifier.equalsIgnoreCase("raml") || classifier.equalsIgnoreCase("oas")) {
                    if (dependency != null) {
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
    private Map<String, Object> loadDescriptor() throws IOException {
        Map<String, Object> anypointDescriptor = null;
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
    private Map<String, Object> readFile(File descriptorFile) throws java.io.IOException {
        if (descriptorFile.exists()) {
            String fname = descriptorFile.getName().toLowerCase();
            ObjectMapper om;
            if (fname.endsWith(".yml") || fname.endsWith(".yaml")) {
                om = new YAMLMapper();
            } else {
                om = new ObjectMapper();
            }
            om.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
            return (Map<String, Object>) om.readValue(descriptorFile, Map.class);
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
