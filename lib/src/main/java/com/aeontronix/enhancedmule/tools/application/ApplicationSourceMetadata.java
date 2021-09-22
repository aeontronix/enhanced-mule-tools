/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.application.api.apikit.APIKitSpec;
import com.aeontronix.enhancedmule.tools.util.APISpecHelper;
import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ApplicationSourceMetadata {
    String getArtifactId();

    String getName();

    String getVersion();

    String getDescription();

    File findIcon();

    String findAPISpecFile(String path);

    APISpecHelper.APISpecVersion findAPISpecVersion(String textValue) throws IOException;

    String getFileStrContent(String path) throws IOException;

    @NotNull
    Map<String, String> listPortalPages() throws IOException;

    JsonNode getJsonContentFromDependencyArchive(String groupId, String artifactId, String version, String path) throws IOException;

    @NotNull
    List<APIKitSpec> findAPIKitSpecs() throws IOException;
}
