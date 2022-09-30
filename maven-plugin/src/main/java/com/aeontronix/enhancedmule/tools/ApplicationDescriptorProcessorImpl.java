/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.file.FileUtils;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.DeploymentOperation;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.exchange.ExchangeAssetDescriptor;
import com.aeontronix.enhancedmule.tools.util.APISpecHelper;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.aeontronix.enhancedmule.tools.util.JsonHelper.isNotNull;
import static com.aeontronix.enhancedmule.tools.util.JsonHelper.isNull;
import static java.io.File.separator;
import static org.slf4j.LoggerFactory.getLogger;

public class ApplicationDescriptorProcessorImpl implements ApplicationDescriptorProcessor {
    private static final String[] apiExts = {".raml", ".yml", ".yaml", ".json"};
    private static final Logger logger = getLogger(ApplicationDescriptorProcessorImpl.class);
    public static final String DESCRIPTION = "description";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String API_ID_PROPERTY = "apiIdProperty";
    public static final String PROPERTIES = "properties";
    public static final String ICON = "icon";
    public static final String CREATE = "create";
    public static final String ASSET_MAIN_FILE = "assetMainFile";
    public static final String PORTAL = "portal";
    public static final String PAGES = "pages";
    public static final String CONTENT = "content";
    public static final String PATH = "path";
    public static final String API_VERSION = "apiVersion";
    public static final String ASSET = "asset";
    public static final String CLIENT = "client";
    public static final String CLIENT_ID_PROPERTY = "clientIdProperty";
    public static final String CLIENT_SECRET_PROPERTY = "clientSecretProperty";
    public static final String TYPE = "type";
    public static final String API = "api";
    private final MavenProject project;
    private File assetPagesDir;
    private File apiSpecDir;
    private boolean apikit;
    private final ObjectMapper objectMapper;
    private ObjectNode applicationDescriptor;

    public ApplicationDescriptorProcessorImpl(@Nullable String descriptor, @NotNull MavenProject project,
                                              File assetPagesDir, File apiSpecDir, boolean apikit) throws IOException {
        this.project = project;
        this.assetPagesDir = assetPagesDir;
        this.apiSpecDir = apiSpecDir;
        this.apikit = apikit;
        objectMapper = JsonHelper.createMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        File descriptorFile;
        if (StringUtils.isNotBlank(descriptor)) {
            descriptorFile = new File(descriptor);
        } else {
            descriptorFile = ApplicationDescriptor.findAnypointFile(project.getBasedir());
        }
        if (descriptorFile == null) {
            logger.warn("No anypoint.json file found, using defaults");
            applicationDescriptor = objectMapper.createObjectNode();
        } else {
            applicationDescriptor = readFile(descriptorFile);
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setApplicationDescriptor(String json) throws JsonProcessingException {
        applicationDescriptor = (ObjectNode) objectMapper.readTree(json);
    }

    @Override
    public void writeToFile(File file, boolean addToProject) throws IOException {
        final File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            FileUtils.mkdir(parentFile);
        }
        objectMapper.writeValue(file, applicationDescriptor);
        if (addToProject) {
            Resource resource = new Resource();
            resource.setDirectory(parentFile.getPath());
            resource.setIncludes(Collections.singletonList(file.getName()));
            project.addResource(resource);
        }
    }

    @Override
    public ApplicationDescriptor getAnypointDescriptor() throws IOException {
        return objectMapper.readerFor(ApplicationDescriptor.class).readValue(applicationDescriptor);
    }

    @Override
    public ObjectNode getApplicationDescriptorJson() {
        return applicationDescriptor;
    }

    @Override
    public void setDefaultValues(boolean inheritNameAndDesc) throws IOException {
        String artifactId = project.getArtifactId();
        String version = project.getVersion();
        if (isNull(applicationDescriptor.get(ID))) {
            applicationDescriptor.set(ID, new TextNode(artifactId));
        }
        if (isNull(applicationDescriptor.get(DESCRIPTION)) && inheritNameAndDesc && StringUtils.isNotBlank(project.getDescription())) {
            applicationDescriptor.set(DESCRIPTION, new TextNode(project.getDescription()));
        }
        if (isNull(applicationDescriptor.get(NAME)) && inheritNameAndDesc) {
            applicationDescriptor.set(NAME, new TextNode(project.getName()));
        }
        if (isNull(applicationDescriptor.get(VERSION))) {
            applicationDescriptor.set(VERSION, new TextNode(version));
        } else {
            version = applicationDescriptor.get(VERSION).textValue();
        }
        // deployment params
        if (isNull(applicationDescriptor.get(""))) {
            applicationDescriptor.set(ID, new TextNode(artifactId));
        }
        // properties
        ObjectNode properties = (ObjectNode) applicationDescriptor.get(PROPERTIES);
        if (isNull(properties)) {
            properties = objectMapper.createObjectNode();
            applicationDescriptor.set(PROPERTIES, properties);
        }
        getOrCreateProperty(objectMapper, properties, DeploymentOperation.ANYPOINT_PLATFORM_CLIENT_ID, "Anypoint platform client id", false);
        getOrCreateProperty(objectMapper, properties, DeploymentOperation.ANYPOINT_PLATFORM_CLIENT_SECRET, "Anypoint platform client secret", true);
        // api
        ObjectNode api = (ObjectNode) applicationDescriptor.get(API);
        if (isNull(api) && apikit) {
            api = objectMapper.createObjectNode();
            applicationDescriptor.set(API, api);
        }
        if (isNotNull(api)) {
            TextNode apiIdProperty = (TextNode) api.get(API_ID_PROPERTY);
            if (isNull(apiIdProperty)) {
                apiIdProperty = new TextNode("anypoint.api.id");
                api.set(API_ID_PROPERTY, apiIdProperty);
            }
            getOrCreateProperty(objectMapper, properties, apiIdProperty.textValue(), "Anypoint API identifier", false);
            // asset
            ObjectNode asset = (ObjectNode) api.get(ASSET);
            if (isNull(asset)) {
                asset = objectMapper.createObjectNode();
                api.set(ASSET, asset);
            }
            ObjectNode icon = (ObjectNode) asset.get(ICON);
            if (isNull(icon)) {
                File iconFile = ExchangeAssetDescriptor.findIcon(project.getBasedir());
                if (iconFile != null && iconFile.exists()) {
                    icon = objectMapper.createObjectNode();
                    if (!iconFile.exists()) {
                        throw new IOException("Unable to find icon file: " + iconFile.getPath());
                    }
                    final Path path = iconFile.toPath();
                    String mimeType = Files.probeContentType(path);
                    if (StringUtils.isBlank(mimeType)) {
                        String fpath = path.toString().toLowerCase();
                        if (fpath.endsWith(".png")) {
                            mimeType = "image/png";
                        } else if (fpath.endsWith(".svg")) {
                            mimeType = "image/svg+xml";
                        } else if (fpath.endsWith(".gif")) {
                            mimeType = "image/gif";
                        } else if (fpath.endsWith(".jpg") || fpath.endsWith(".jpeg")) {
                            mimeType = "image/jpg";
                        }
                    }
                    if (StringUtils.isNotBlank(mimeType)) {
                        icon.set("mimeType", new TextNode(mimeType));
                    } else {
                        throw new IOException("Unable to identity mime-Type of icon image, please specify mimeType in descriptor: " + iconFile.getPath());
                    }
                    icon.set(CONTENT, new TextNode(StringUtils.base64Encode(FileUtils.toByteArray(iconFile))));
                    asset.set(ICON, icon);
                }
            }
            Dependency dep = findRAMLDependency(project);
            TextNode assetId = (TextNode) asset.get(ID);
            if (isNull(assetId)) {
                assetId = new TextNode(dep != null ? dep.getArtifactId() : artifactId + "-spec");
                asset.set(ID, assetId);
            }
            if (isNull(asset.get(DESCRIPTION)) && inheritNameAndDesc && applicationDescriptor.get(DESCRIPTION) != null) {
                asset.set(DESCRIPTION, applicationDescriptor.get(DESCRIPTION));
            }
            BooleanNode assetCreate = (BooleanNode) asset.get(CREATE);
            JsonNode assetMainFile = asset.get(ASSET_MAIN_FILE);
            if (isNull(assetCreate) || isNull(assetMainFile)) {
                String apiSpecFile = findAPISpecFile(assetId.textValue());
                if( apiSpecFile == null ) {
                    apiSpecFile = findAPISpecFile(artifactId);
                }
                if (isNull(assetCreate)) {
                    assetCreate = BooleanNode.valueOf(apiSpecFile != null);
                    asset.set(CREATE, assetCreate);
                }
                if (assetCreate.asBoolean() && isNull(assetMainFile) && apiSpecFile != null) {
                    assetMainFile = new TextNode(apiSpecFile);
                    asset.set(ASSET_MAIN_FILE, assetMainFile);
                }
            }
            boolean restAPI = isNotNull(assetMainFile) || dep != null;
            boolean raml = isNotNull(assetMainFile) && assetMainFile.textValue().toLowerCase().endsWith(".raml");
            APISpecHelper.APISpecVersion apiSpecVersion = null;
            if( isNotNull(assetMainFile) ) {
                final File apiSpecFile = new File(apiSpecDir, assetMainFile.textValue());
                apiSpecVersion = APISpecHelper.findVersion(apiSpecFile);
            }
            if (isNull(asset.get(NAME)) && inheritNameAndDesc) {
                asset.set(NAME, applicationDescriptor.get(NAME));
            }
            ObjectNode portal = (ObjectNode) asset.get(PORTAL);
            ArrayNode pages = portal != null ? (ArrayNode) portal.get(PAGES) : null;
            if (isNotNull(pages)) {
                for (JsonNode page : pages) {
                    if (page.get(CONTENT) == null) {
                        final JsonNode pagePath = page.get(PATH);
                        if (pagePath != null) {
                            try (FileInputStream fis = new FileInputStream(project.getBasedir() + separator + pagePath.textValue().replace("/", separator))) {
                                ((ObjectNode) page).remove(PATH);
                                ((ObjectNode) page).set(CONTENT, new TextNode(IOUtils.toString(fis)));
                            }
                        }
                    }
                }
            }
            if (assetPagesDir != null && assetPagesDir.exists()) {
                final File[] files = assetPagesDir.listFiles();
                if (files != null && files.length > 0) {
                    if (portal == null) {
                        portal = objectMapper.createObjectNode();
                        asset.set(PORTAL, portal);
                    }
                    if (pages == null) {
                        pages = objectMapper.createArrayNode();
                        portal.set(PAGES, pages);
                    }
                    for (File file : files) {
                        if (file.isFile()) {
                            final String fileName = file.getName();
                            int idx = fileName.indexOf(".");
                            if (idx != -1) {
                                final ObjectNode p = objectMapper.createObjectNode();
                                p.set(CONTENT, new TextNode(FileUtils.toString(file)));
                                p.set(NAME, new TextNode(fileName.substring(0, idx)));
                                pages.add(p);
                            }
                        }
                    }
                }
            }
            JsonNode apiVersion = asset.get(API_VERSION);
            if (isNull(apiVersion)) {
                String assetAPIVersion = null;
                if (dep != null) {
                    File depFile = findDependencyFile(dep);
                    if (depFile != null && depFile.exists()) {
                        final ZipFile zipFile = new ZipFile(depFile);
                        final ZipEntry entry = zipFile.getEntry("exchange.json");
                        if (entry != null) {
                            final JsonNode exchangeJson;
                            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                                exchangeJson = objectMapper.readTree(IOUtils.toString(inputStream));
                            }
                            final JsonNode depAPIVersion = exchangeJson.get(API_VERSION);
                            if (depAPIVersion != null && !depAPIVersion.isNull()) {
                                assetAPIVersion = depAPIVersion.textValue();
                            }
                        }
                    }
                    if (assetAPIVersion == null) {
                        assetAPIVersion = dep.getVersion();
                    }
                } else if( apiSpecVersion != null ) {
                    assetAPIVersion = apiSpecVersion.getNonSnapshotVersion();
                }
                if (assetAPIVersion == null) {
                    assetAPIVersion = version != null ? version : "1.0.0";
                }
                apiVersion = new TextNode(assetAPIVersion);
                asset.set(API_VERSION, apiVersion);
            }
            JsonNode assetVersion = asset.get(VERSION);
            if (isNull(assetVersion)) {
                if (dep != null) {
                    assetVersion = new TextNode(dep.getVersion());
                } else {
                    assetVersion = new TextNode(version);
                }
                asset.set(VERSION, assetVersion);
            }
            JsonNode assetType = asset.get(TYPE);
            if (isNull(assetType) && restAPI) {
                assetType = new TextNode("rest");
                asset.set(TYPE, assetType);
            }
        }
        final ObjectNode client = (ObjectNode) applicationDescriptor.get(CLIENT);
        if (isNotNull(client)) {
            JsonNode clientIdProperty = client.get(CLIENT_ID_PROPERTY);
            if (isNull(clientIdProperty)) {
                clientIdProperty = new TextNode("anypoint.api.client.id");
                client.set(CLIENT_ID_PROPERTY, clientIdProperty);
            }
            JsonNode clientSecretProperty = client.get(CLIENT_SECRET_PROPERTY);
            if (isNull(clientSecretProperty)) {
                clientSecretProperty = new TextNode("anypoint.api.client.secret");
                client.set(CLIENT_SECRET_PROPERTY, clientSecretProperty);
            }
            getOrCreateProperty(objectMapper, properties, DeploymentOperation.ANYPOINT_PLATFORM_CLIENT_ID, "Anypoint platform client id", false);
            getOrCreateProperty(objectMapper, properties, DeploymentOperation.ANYPOINT_PLATFORM_CLIENT_SECRET, "Anypoint platform client secret", true);
            getOrCreateProperty(objectMapper, properties, clientIdProperty.textValue(), "API Client Id", false);
            getOrCreateProperty(objectMapper, properties, clientSecretProperty.textValue(), "API Client Secret", true);
        }
    }

    private File findDependencyFile(Dependency dep) {
        final Set<Artifact> artifacts = project.getArtifacts();
        if (artifacts != null) {
            for (Artifact artifact : artifacts) {
                if (artifact.getArtifactId().equals(dep.getArtifactId()) &&
                        artifact.getGroupId().equals(dep.getGroupId()) &&
                        artifact.getVersion().equals(dep.getVersion())) {
                    return artifact.getFile();
                }
            }
        }
        return null;
    }

    private static ObjectNode getOrCreateProperty(ObjectMapper objectMapper, ObjectNode properties, String id, String name, boolean secure) {
        ObjectNode prop = (ObjectNode) properties.get(id);
        if (prop == null) {
            prop = objectMapper.createObjectNode();
            prop.set("id", new TextNode(id));
            prop.set("name", new TextNode(name));
            prop.set("secure", BooleanNode.valueOf(secure));
            properties.set(id, prop);
        }
        return prop;
    }

    @Nullable
    private String findAPISpecFile(String assetId) {
        if (apiSpecDir != null && apiSpecDir.exists()) {
            String apiFile = findAPISpecFile(assetId, apiSpecDir);
            if (apiFile != null) return apiFile;
        }
        return null;
    }

    @Nullable
    public static String findAPISpecFile(String assetId, File dir) {
        if (dir.exists()) {
            final List<String> filenames = Arrays.asList("api", assetId);
            for (String apiExt : apiExts) {
                for (String filename : filenames) {
                    String apiFile = filename + apiExt;
                    if (new File(dir, apiFile).exists()) {
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

    @Override
    public void legacyConvert() {
        ObjectNode api = (ObjectNode) applicationDescriptor.get("api");
        if (api != null) {
            final JsonNode assetId = api.remove("assetId");
            final JsonNode assetVersion = api.remove("assetVersion");
            ObjectNode asset = (ObjectNode) api.get("asset");
            if (asset == null) {
                asset = objectMapper.createObjectNode();
                api.set("asset", asset);
            }
            if (assetId != null || assetVersion != null) {
                logger.warn("api->assetId and api->assetVersion are deprecated, use api->asset->id and api->asset->version instead");
                asset.set(ID, assetId);
                asset.set("version", assetVersion);
            }
            relocate(api, "exchangeTags", "api->exchangeTags", asset, "tags", "api->asset->tags");
            final JsonNode name = relocate(api, "name", "api->name", asset, "name", "api->asset->name");
            relocate(api, "create", "api->create", asset, "create", "api->asset->create");
            relocate(api, "type", "api->type", asset, "type", "api->asset->type");
            final JsonNode apiVersion = api.remove("apiVersion");
            if (apiVersion != null && api.get("version") == null) {
                logger.warn("'api->apiVersion' is deprecated, use api->asset->apiVersion instead");
                asset.set("apiVersion", apiVersion);
            }
            final JsonNode version = api.remove("version");
            if (version != null) {
                asset.set("apiVersion", name);
                logger.warn("api->version deprecated, use api->asset->apiVersion instead");
            }
            final JsonNode description = api.remove(DESCRIPTION);
            if (description != null) {
                asset.set(DESCRIPTION, description);
                logger.warn("api->description deprecated, use api->asset->description instead");
            }
            final JsonNode assetMainFile = api.remove("assetMainFile");
            if (assetMainFile != null) {
                asset.set("assetMainFile", description);
                logger.warn("api->assetMainFile deprecated, use api->asset->assetMainFile instead");
            }
            final JsonNode assetCreate = api.remove("assetCreate");
            if (assetCreate != null) {
                asset.set("create", assetCreate);
                logger.warn("api->assetCreate deprecated, use api->asset->create instead");
            }
            final JsonNode endpoint = api.remove("endpoint");
            if (endpoint != null) {
                logger.warn("'endpoint' is deprecated, please use implementationUrl and/or consumerUrl instead");
                api.set("implementationUrl", endpoint);
                api.set("consumerUrl", endpoint);
            }
            final JsonNode endpointJson = api.remove("endpointJson");
            if (endpointJson != null) {
                logger.warn("'endpointJson' is deprecated, please use consumerUrlJson instead");
                api.set("consumerUrlJson", endpointJson);
            }
            final JsonNode addAutoDiscovery = api.remove("addAutoDescovery");
            if (addAutoDiscovery != null) {
                logger.warn("'addAutoDescovery' is deprecated, please use 'addAutoDiscovery' instead");
                api.set("addAutoDiscovery", addAutoDiscovery);
            }
            ObjectNode client = (ObjectNode) api.remove("clientApp");
            if (client != null) {
                logger.warn("'clientApp' under 'api' is deprecated, please use 'client' at application descriptor level instead.");
                applicationDescriptor.set("client", client);
            }
            JsonNode access = api.remove("access");
            if (access != null) {
                logger.warn("'access' under 'api' is deprecated, please move it inside 'client' instead.");
                if (client == null) {
                    client = objectMapper.createObjectNode();
                    applicationDescriptor.set("client", client);
                }
                client.set("access", access);
            }
            // 1.2.7
            relocate(asset, "assetId", "asset->assetId", asset, "id", "asset->id");
            // 1.3.0
            if( client == null ) {
                client = (ObjectNode) applicationDescriptor.get("client");
            }
            if( client != null) {
                access = client.get("access");
                if( access != null ) {
                    for (JsonNode jsonNode : access) {
                        final JsonNode envId = jsonNode.get("envId");
                        if( envId != null ) {
                            logger.warn("client->access->envId is deprecated, use client->access->env instead");
                            ((ObjectNode)jsonNode).set("env",envId);
                        }
                    }
                }
            }
        }
    }

    private static JsonNode relocate(ObjectNode oldParent, String oldName, String oldLocationTxt,
                                     ObjectNode newParent, String newName, String newLocationTxt) {
        final JsonNode node = oldParent.remove(oldName);
        if (node != null) {
            newParent.set(newName, node);
            logger.warn(oldLocationTxt + " is deprecated, use " + newLocationTxt + " instead");
        }
        return node;
    }

    private static ObjectNode readFile(File descriptorFile) throws java.io.IOException {
        if (descriptorFile.exists()) {
            String fname = descriptorFile.getName().toLowerCase();
            ObjectMapper om;
            if (fname.endsWith(".yml") || fname.endsWith(".yaml")) {
                om = new YAMLMapper();
            } else {
                om = new ObjectMapper();
            }
            om.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
            return (ObjectNode) om.readTree(descriptorFile);
        } else {
            return null;
        }
    }

}
