/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.enhancedmule.tools.application.api.apikit.APIKitSpec;
import com.aeontronix.enhancedmule.tools.util.APISpecHelper;
import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ArchiveApplicationSourceMetadata implements ApplicationSourceMetadata {
    public ArchiveApplicationSourceMetadata(File archiveFile) {

    }

    @Override
    public String getArtifactId() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public File findIcon() {
        return null;
    }

    @Override
    public String findAPISpecFile(String path) {
        return null;
    }

    @Override
    public APISpecHelper.APISpecVersion findAPISpecVersion(String textValue) throws IOException {
        return null;
    }

    @Override
    public String getFileStrContent(String path) throws IOException {
        return null;
    }

    @NotNull
    @Override
    public Map<String, String> listPortalPages() throws IOException {
        return null;
    }

    @Override
    public JsonNode getJsonContentFromDependencyArchive(String groupId, String artifactId, String version, String path) throws IOException {
        return null;
    }

    @NotNull
    @Override
    public List<APIKitSpec> findAPIKitSpecs() throws IOException {
        return null;
    }
}
