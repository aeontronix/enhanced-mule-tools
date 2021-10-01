/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.FileUtils;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.ApplicationSourceMetadata;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.slf4j.LoggerFactory.getLogger;

public class ApplicationDescriptorProcessorImpl implements ApplicationDescriptorProcessor {
    private static final Logger logger = getLogger(ApplicationDescriptorProcessorImpl.class);
    public static final String DESCRIPTION = "description";
    public static final String ID = "id";
    public static final String API = "api";
    private final MavenProject project;
    private final ObjectMapper objectMapper;
    private ObjectNode applicationDescriptor;

    public ApplicationDescriptorProcessorImpl(@Nullable String descriptor, @NotNull MavenProject project,
                                              File assetPagesDir, File apiSpecDir, ApplicationSourceMetadata applicationSourceMetadata) throws IOException {
        this.project = project;
        objectMapper = JsonHelper.createMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        File descriptorFile;
        if (StringUtils.isNotBlank(descriptor)) {
            descriptorFile = new File(descriptor);
        } else {
            descriptorFile = ApplicationDescriptor.findAnypointFile(project.getBasedir());
        }
        if (descriptorFile == null) {
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
            if (client == null) {
                client = (ObjectNode) applicationDescriptor.get("client");
            }
            if (client != null) {
                access = client.get("access");
                if (access != null) {
                    for (JsonNode jsonNode : access) {
                        final JsonNode envId = jsonNode.get("envId");
                        if (envId != null) {
                            logger.warn("client->access->envId is deprecated, use client->access->env instead");
                            ((ObjectNode) jsonNode).set("env", envId);
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
