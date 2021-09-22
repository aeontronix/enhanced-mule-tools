/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.commons.FileUtils;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.DeploymentOperation;
import com.aeontronix.enhancedmule.tools.application.api.apikit.APIKitSpec;
import com.aeontronix.enhancedmule.tools.application.api.apikit.DependencyAPIKitSpec;
import com.aeontronix.enhancedmule.tools.application.api.apikit.InlineAPIKitSpec;
import com.aeontronix.enhancedmule.tools.util.APISpecHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.aeontronix.enhancedmule.tools.util.JsonHelper.isNotNull;
import static com.aeontronix.enhancedmule.tools.util.JsonHelper.isNull;

/**
 * Used to process an application archive to set default values and to perform code weaving
 */
public class ApplicationArchiveProcessor {
    private static final String[] apiExts = {".raml", ".yml", ".yaml", ".json"};
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

    public static void process(ApplicationSourceMetadata src, ObjectNode applicationDescriptor, ObjectMapper objectMapper) throws IOException {
        String artifactId = src.getArtifactId();
        String version = src.getVersion();
        if (isNull(applicationDescriptor.get(ID))) {
            applicationDescriptor.set(ID, new TextNode(artifactId));
        }
        if (isNull(applicationDescriptor.get(DESCRIPTION)) && StringUtils.isNotBlank(src.getDescription())) {
            applicationDescriptor.set(DESCRIPTION, new TextNode(src.getDescription()));
        }
        if (isNull(applicationDescriptor.get(NAME))) {
            applicationDescriptor.set(NAME, new TextNode(src.getName()));
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
        final List<APIKitSpec> apiKitSpecs = src.findAPIKitSpecs();
        APIKitSpec apiSpec = apiKitSpecs.isEmpty() ? null : apiKitSpecs.get(0);
        ObjectNode api = (ObjectNode) applicationDescriptor.get(API);
        if (isNull(api) && apiSpec != null) {
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
                File iconFile = src.findIcon();
                try {
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
                } finally {
                    if (iconFile instanceof Closeable) {
                        IOUtils.close((Closeable) iconFile);
                    }
                }
            }
            TextNode assetId;
            APISpecHelper.APISpecVersion apiSpecVersion = null;
            JsonNode apiVersion = asset.get(API_VERSION);
            if (apiSpec instanceof DependencyAPIKitSpec) {
                assetId = (TextNode) asset.get(ID);
                if (isNull(assetId)) {
                    assetId = new TextNode(apiSpec.getAssetId());
                    asset.set(ID, assetId);
                }
                BooleanNode assetCreate = (BooleanNode) asset.get(CREATE);
                if (isNull(assetCreate)) {
                    asset.set(CREATE, BooleanNode.valueOf(false));
                }
            } else if (apiSpec instanceof InlineAPIKitSpec) {
                assetId = (TextNode) asset.get(ID);
                if (isNull(assetId)) {
                    assetId = new TextNode(artifactId + "-spec");
                    asset.set(ID, assetId);
                }
                BooleanNode assetCreate = (BooleanNode) asset.get(CREATE);
                JsonNode assetMainFile = asset.get(ASSET_MAIN_FILE);
                if (isNull(assetCreate) || isNull(assetMainFile)) {
                    String apiSpecFile = src.findAPISpecFile(assetId.textValue());
                    if (apiSpecFile == null) {
                        apiSpecFile = src.findAPISpecFile(artifactId);
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
//                boolean restAPI = isNotNull(assetMainFile) || dep != null;
//                boolean raml = isNotNull(assetMainFile) && assetMainFile.textValue().toLowerCase().endsWith(".raml");
                if (isNotNull(assetMainFile)) {
                    apiSpecVersion = src.findAPISpecVersion(assetMainFile.textValue());
                }
            }
            if (isNull(asset.get(DESCRIPTION)) && applicationDescriptor.get(DESCRIPTION) != null) {
                asset.set(DESCRIPTION, applicationDescriptor.get(DESCRIPTION));
            }
            if (isNull(asset.get(NAME))) {
                asset.set(NAME, applicationDescriptor.get(NAME));
            }
            ObjectNode portal = (ObjectNode) asset.get(PORTAL);
            ArrayNode pages = portal != null ? (ArrayNode) portal.get(PAGES) : null;
            if (isNotNull(pages)) {
                for (JsonNode page : pages) {
                    if (page.get(CONTENT) == null) {
                        final JsonNode pagePath = page.get(PATH);
                        if (pagePath != null) {
                            String content = src.getFileStrContent(pagePath.textValue());
                            ((ObjectNode) page).remove(PATH);
                            ((ObjectNode) page).set(CONTENT, new TextNode(content));
                        }
                    }
                }
            }
            Map<String, String> portalPages = src.listPortalPages();
            if (!portalPages.isEmpty()) {
                if (portal == null) {
                    portal = objectMapper.createObjectNode();
                    asset.set(PORTAL, portal);
                }
                if (pages == null) {
                    pages = objectMapper.createArrayNode();
                    portal.set(PAGES, pages);
                }
                for (Map.Entry<String, String> e : portalPages.entrySet()) {
                    final ObjectNode p = objectMapper.createObjectNode();
                    p.set(NAME, new TextNode(e.getKey()));
                    p.set(CONTENT, new TextNode(e.getValue()));
                    pages.add(p);
                }
            }
            if (isNull(apiVersion)) {
                String assetAPIVersion = null;
                if (apiSpec instanceof DependencyAPIKitSpec) {
                    final String apiSpecVersionStr = ((DependencyAPIKitSpec) apiSpec).getVersion();
                    final JsonNode exchangeJson = src.getJsonContentFromDependencyArchive(apiSpec.getGroupId(),
                            apiSpec.getAssetId(),
                            apiSpecVersionStr,
                            "exchange.json");
                    final JsonNode depAPIVersion = exchangeJson.get(API_VERSION);
                    if (depAPIVersion != null && !depAPIVersion.isNull()) {
                        assetAPIVersion = depAPIVersion.textValue();
                    }
                    if (assetAPIVersion == null) {
                        assetAPIVersion = apiSpecVersionStr;
                    }
                } else if (apiSpecVersion != null) {
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
                if (apiSpec instanceof DependencyAPIKitSpec) {
                    assetVersion = new TextNode(((DependencyAPIKitSpec) apiSpec).getVersion());
                } else {
                    assetVersion = new TextNode(version);
                }
                asset.set(VERSION, assetVersion);
            }
            JsonNode assetType = asset.get(TYPE);
            if (isNull(assetType) && apiSpec != null) {
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
}
