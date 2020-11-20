/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.portal.PortalPageDescriptor;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.kloudtek.util.FileUtils;
import com.kloudtek.util.io.IOUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static java.io.File.separator;
import static org.slf4j.LoggerFactory.getLogger;

public class ApplicationDescriptorParser {
    private static final String[] apiExts = {".raml",".yml",".yaml",".json"};
    private static final Logger logger = getLogger(ApplicationDescriptorParser.class);

    public static ApplicationDescriptor parse(@Nullable String descriptor, @NotNull MavenProject project,
                                              @Nullable File writeToFile, boolean addWriteToFileToProject) throws IOException {
        ObjectMapper objectMapper = JsonHelper.createMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Map<String, Object> anypointDescriptorJson = null;
        if (StringUtils.isNotBlank(descriptor)) {
            File descriptorFile = new File(descriptor);
            anypointDescriptorJson = readFile(descriptorFile);
        } else {
            File descriptorFile = findAnypointFile(project.getBasedir());
            if (descriptorFile != null) {
                anypointDescriptorJson = readFile(descriptorFile);
            }
        }
        if (anypointDescriptorJson == null) {
            anypointDescriptorJson = new HashMap<>();
        }
        legacyConvert(anypointDescriptorJson);
        ApplicationDescriptor applicationDescriptor = objectMapper.convertValue(anypointDescriptorJson, ApplicationDescriptor.class);
        setDefaultValues(applicationDescriptor, project);
        if (writeToFile != null) {
            final File parentFile = writeToFile.getParentFile();
            if (!parentFile.exists()) {
                FileUtils.mkdir(parentFile);
            }
            objectMapper.writeValue(writeToFile, applicationDescriptor);
            if (addWriteToFileToProject) {
                Resource resource = new Resource();
                resource.setDirectory(parentFile.getPath());
                resource.setIncludes(Collections.singletonList(writeToFile.getName()));
                project.addResource(resource);
            }
        }
        return applicationDescriptor;
    }

    private static void setDefaultValues(ApplicationDescriptor applicationDescriptor, MavenProject project) throws IOException {
        String apiArtifactId = project.getArtifactId();
        String version = project.getVersion();
        if (applicationDescriptor.getId() == null) {
            applicationDescriptor.setId(apiArtifactId);
        }
        if( applicationDescriptor.getDescription() == null && StringUtils.isNotBlank(project.getDescription())  ) {
            applicationDescriptor.setDescription(project.getDescription());
        }
        if (applicationDescriptor.getName() == null) {
            applicationDescriptor.setName(project.getName());
        }
        if (applicationDescriptor.getVersion() == null) {
            applicationDescriptor.setVersion(version);
        }
        APIDescriptor api = applicationDescriptor.getApi();
        if (api != null) {
            Dependency dep = findRAMLDependency(project);
            if (api.getAssetId() == null) {
                api.setAssetId(dep != null ? dep.getArtifactId() : apiArtifactId + "-spec");
            }
            if( api.getDescription() == null ) {
                api.setDescription(applicationDescriptor.getDescription());
            }
            if( api.getAssetVersion() == null ) {
                api.setAssetVersion(dep != null ? dep.getVersion() : "1.0.0");
            }
            if( api.isAssetCreate() == null || api.getAssetMainFile() == null ) {
                String apiSpecFile = findAPISpecFile(project,api.getAssetId());
                if( api.isAssetCreate() == null ) {
                    api.setAssetCreate(apiSpecFile != null);
                }
                if( api.getAssetMainFile() == null ) {
                    api.setAssetMainFile(apiSpecFile);
                }
            }
            if( api.getName() == null ) {
                api.setName(applicationDescriptor.getName());
            }
            if (api.getPortal() != null && api.getPortal().getPages() != null) {
                for (PortalPageDescriptor page : api.getPortal().getPages()) {
                    if (page.getContent() == null) {
                        try (FileInputStream fis = new FileInputStream(project.getBasedir() + separator + page.getPath().replace("/", separator))) {
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
    }

    @Nullable
    private static String findAPISpecFile(MavenProject project, String assetId) {
        for (Resource resource : project.getResources()) {
            final File dir = new File(resource.getDirectory(),"api");
            if( dir.exists() ) {
                final List<String> filenames = Arrays.asList("api", assetId);
                for (String apiExt : apiExts) {
                    for (String filename : filenames) {
                        String apiFile = filename+apiExt;
                        if( new File(dir,apiFile).exists() ) {
                            return apiFile;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static Dependency findRAMLDependency(MavenProject project) {
        Dependency dependency = null;
        for (Dependency d : project.getDependencies()) {
            String classifier = d.getClassifier();
            if (classifier != null) {
                if (classifier.equalsIgnoreCase("raml") || classifier.equalsIgnoreCase("oas")) {
                    if (dependency != null) {
                        logger.warn("Found more than one raml/oas dependencies in pom, ignoring all");
                        return null;
                    } else {
                        dependency = d;
                    }
                }
            }
        }
        return dependency;
    }

    @SuppressWarnings("unchecked")
    private static void legacyConvert(Map<String, Object> anypointDescriptor) {
        Map<String, Object> api = (Map<String, Object>) anypointDescriptor.get("api");
        if (api != null) {
            final Object addAutoDiscovery = api.remove("addAutoDescovery");
            if (addAutoDiscovery != null) {
                logger.warn("'addAutoDescovery' is deprecated, please use 'addAutoDiscovery' instead");
                api.put("addAutoDiscovery", addAutoDiscovery);
            }
            Map<String, Object> client = (Map<String, Object>) api.remove("clientApp");
            if (client != null) {
                logger.warn("'clientApp' under 'api' is deprecated, please use 'client' at application descriptor level instead.");
                anypointDescriptor.put("client", client);
            }
            Object access = api.remove("access");
            if (access != null) {
                logger.warn("'access' under 'api' is deprecated, please move it inside 'client' instead.");
                if (client == null) {
                    client = new HashMap<>();
                    anypointDescriptor.put("client", client);
                }
                client.put("access", access);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readFile(File descriptorFile) throws java.io.IOException {
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

    private static File findAnypointFile(File basedir) {
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