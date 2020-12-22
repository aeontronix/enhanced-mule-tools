/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.FileUtils;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.exchange.ExchangeAssetDescriptor;
import com.aeontronix.enhancedmule.tools.legacy.deploy.Deployer;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.ClientApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.IconDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.PropertyDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.portal.PortalPageDescriptor;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
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

    public static ApplicationDescriptor parseAndProcess(@Nullable String descriptor, @NotNull MavenProject project,
                                                        @Nullable File writeToFile, boolean addWriteToFileToProject,
                                                        boolean inheritNameAndDesc) throws IOException {
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
        setDefaultValues(applicationDescriptor, project, inheritNameAndDesc);
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

    private static void setDefaultValues(ApplicationDescriptor applicationDescriptor, MavenProject project, boolean inheritNameAndDesc) throws IOException {
        String apiArtifactId = project.getArtifactId();
        String version = project.getVersion();
        if (applicationDescriptor.getId() == null) {
            applicationDescriptor.setId(apiArtifactId);
        }
        if( applicationDescriptor.getDescription() == null && inheritNameAndDesc && StringUtils.isNotBlank(project.getDescription() )  ) {
            applicationDescriptor.setDescription(project.getDescription());
        }
        if (applicationDescriptor.getName() == null && inheritNameAndDesc) {
            applicationDescriptor.setName(project.getName());
        }
        if (applicationDescriptor.getVersion() == null) {
            applicationDescriptor.setVersion(version);
        }
        APIDescriptor api = applicationDescriptor.getApi();
        if (api != null) {
            final ExchangeAssetDescriptor asset = api.getAsset();
            if( asset == null ) {
                throw new IllegalStateException("api missing asset descriptor");
            }
            if( api.getApiIdProperty() == null ) {
                api.setApiIdProperty("anypoint.api.id");
            }
            final String apiIdProperty = api.getApiIdProperty();
            final HashMap<String, PropertyDescriptor> properties = applicationDescriptor.getProperties();
            getOrCreateProperty(properties, apiIdProperty, "Anypoint API identifier", false);
            getOrCreateProperty(properties, Deployer.ANYPOINT_PLATFORM_CLIENT_ID, "Anypoint platform client id", false);
            getOrCreateProperty(properties, Deployer.ANYPOINT_PLATFORM_CLIENT_SECRET, "Anypoint platform client secret", true);
            IconDescriptor icon = asset.getIcon();
            if( icon == null ) {
                File iconFile = ExchangeAssetDescriptor.findIcon(project.getBasedir());
                if( iconFile != null ) {
                    icon = new IconDescriptor(iconFile.getPath());
                    asset.setIcon(icon);
                }
            }

            Dependency dep = findRAMLDependency(project);
            if (api.getAssetId() == null) {
                api.setAssetId(dep != null ? dep.getArtifactId() : apiArtifactId + "-spec");
            }
            if( asset.getDescription() == null && inheritNameAndDesc ) {
                asset.setDescription(applicationDescriptor.getDescription());
            }
            if( api.getAssetVersion() == null ) {
                if( dep != null ) {
                    api.setAssetVersion(dep.getVersion());
                } else {
                    api.setAssetVersion(version);
                }
            }
            if( asset.getCreate() == null || asset.getAssetMainFile() == null ) {
                String apiSpecFile = findAPISpecFile(project,api.getAssetId());
                if( asset.getCreate() == null ) {
                    asset.setCreate(apiSpecFile != null);
                }
                if( asset.getAssetMainFile() == null ) {
                    asset.setAssetMainFile(apiSpecFile);
                }
            }
            if( asset.getName() == null && inheritNameAndDesc ) {
                asset.setName(applicationDescriptor.getName());
            }
            if (asset.getPortal() != null && asset.getPortal().getPages() != null) {
                for (PortalPageDescriptor page : asset.getPortal().getPages()) {
                    if (page.getContent() == null) {
                        try (FileInputStream fis = new FileInputStream(project.getBasedir() + separator + page.getPath().replace("/", separator))) {
                            page.setPath(null);
                            page.setContent(IOUtils.toString(fis));
                        }
                    }
                }
            }
            if (asset.getVersion() == null) {
                asset.setVersion("1.0.0");
            }
            if (asset.getApiVersion() == null) {
                if (dep != null) {
                    final String majorVersion = ExchangeAssetDescriptor.getMajorVersion(dep.getVersion());
                    if (dep.getClassifier().equalsIgnoreCase("oas")) {
                        asset.setApiVersion(majorVersion+".0.0");
                    } else {
                        asset.setApiVersion("v"+majorVersion);
                    }
                } else {
                    asset.setApiVersion("v"+asset.getMajorVersion());
                }
            }
        }
        final ClientApplicationDescriptor client = applicationDescriptor.getClient();
        if( client != null ) {
            if( client.getClientIdProperty() == null ) {
                client.setClientIdProperty("anypoint.api.client.id");
            }
            if( client.getClientSecretProperty() == null ) {
                client.setClientSecretProperty("anypoint.api.client.secret");
            }
            getOrCreateProperty(applicationDescriptor.getProperties(), Deployer.ANYPOINT_PLATFORM_CLIENT_ID, "Anypoint platform client id", false);
            getOrCreateProperty(applicationDescriptor.getProperties(), Deployer.ANYPOINT_PLATFORM_CLIENT_SECRET, "Anypoint platform client secret", true);
            getOrCreateProperty(applicationDescriptor.getProperties(), client.getClientIdProperty(), "API Client Id", false);
            getOrCreateProperty(applicationDescriptor.getProperties(), client.getClientSecretProperty(), "API Client Secret", true);
        }
    }

    private static PropertyDescriptor getOrCreateProperty(HashMap<String, PropertyDescriptor> properties, String id, String name, boolean secure) {
        PropertyDescriptor prop = properties.get(id);
        if(prop == null ) {
            prop = new PropertyDescriptor(id, name, secure);
            properties.put(id,prop);
        }
        return prop;
    }

    @Nullable
    private static String findAPISpecFile(MavenProject project, String assetId) {
        for (Resource resource : project.getResources()) {
            final File dir = new File(resource.getDirectory(),"api");
            String apiFile = findAPISpecFile(assetId, dir);
            if (apiFile != null) return apiFile;
        }
        return null;
    }

    @Nullable
    public static String findAPISpecFile(String assetId, File dir) {
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
            final Object assetId = api.remove("assetId");
            final Object assetVersion = api.remove("assetVersion");
            Map<String,Object> asset = (Map<String, Object>) api.get("asset");
            if( asset == null ) {
                asset = new HashMap<>();
                api.put("asset",asset);
            }
            if( assetId != null || assetVersion != null ) {
                logger.warn("api->assetId and api->assetVersion are deprecated, use api->asset->id and api->asset->version instead");
                asset.put("id",assetId);
                asset.put("version",assetVersion);
            }
            final Object exchangeTags = api.remove("exchangeTags");
            if( exchangeTags != null ) {
                asset.put("tags",exchangeTags);
                logger.warn("api->exchangeTags is deprecated, use api->asset->tags instead");
            }
            final Object name = api.remove("name");
            if( name != null ) {
                asset.put("name",name);
                logger.warn("api->name deprecated, use api->asset->name instead");
            }
            final Object create = api.remove("create");
            if( create != null ) {
                asset.put("create",name);
                logger.warn("api->create deprecated, use api->asset->create instead");
            }
            final Object type = api.remove("type");
            if( type != null ) {
                asset.put("type",name);
                logger.warn("api->type deprecated, use api->asset->type instead");
            }
            final Object apiVersion = api.remove("apiVersion");
            if( apiVersion != null && api.get("version") == null ) {
                logger.warn("'api->apiVersion' is deprecated, use api->asset->apiVersion instead");
                asset.put("apiVersion",apiVersion);
            }
            final Object version = api.remove("version");
            if( version != null ) {
                asset.put("apiVersion",name);
                logger.warn("api->version deprecated, use api->asset->apiVersion instead");
            }
            final Object description = api.remove("description");
            if( description != null ) {
                asset.put("description",description);
                logger.warn("api->description deprecated, use api->asset->description instead");
            }
            final Object assetMainFile = api.remove("assetMainFile");
            if( assetMainFile != null ) {
                asset.put("assetMainFile",description);
                logger.warn("api->assetMainFile deprecated, use api->asset->assetMainFile instead");
            }
            final Object assetCreate = api.remove("assetCreate");
            if( assetCreate != null ) {
                asset.put("create",assetCreate);
                logger.warn("api->assetCreate deprecated, use api->asset->create instead");
            }
            final Object endpoint = api.remove("endpoint");
            if( endpoint != null ) {
                logger.warn("'endpoint' is deprecated, please use implementationUrl and/or consumerUrl instead");
                api.put("implementationUrl",endpoint);
                api.put("consumerUrl",endpoint);
            }
            final Object endpointJson = api.remove("endpointJson");
            if( endpointJson != null ) {
                logger.warn("'endpointJson' is deprecated, please use consumerUrlJson instead");
                api.put("consumerUrlJson",endpointJson);
            }
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

    public static File findAnypointFile(File basedir) {
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
