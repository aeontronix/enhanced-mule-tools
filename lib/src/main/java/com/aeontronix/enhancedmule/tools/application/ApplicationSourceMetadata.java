/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.enhancedmule.tools.application.api.apikit.APIKitSpec;
import com.aeontronix.enhancedmule.tools.util.APISpecHelper;
import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ApplicationSourceMetadata {
    public static final String[] apiExts = {".raml", ".yml", ".yaml", ".json"};

    public abstract String getArtifactId();

    public abstract String getName();

    public abstract String getVersion();

    public abstract String getDescription();

    public abstract File findIcon();

    public abstract String findAPISpecFile(String... names) throws IOException;

    public abstract APISpecHelper.APISpecVersion findAPISpecVersion(String textValue) throws IOException;

    public abstract String getFileStrContent(String path) throws IOException;

    @NotNull
    public abstract Map<String, String> getPortalPages() throws IOException;

    public abstract JsonNode getJsonContentFromDependencyArchive(String groupId, String artifactId, String version, String path) throws IOException;

    @NotNull
    public abstract List<APIKitSpec> findAPIKitSpecs() throws IOException;

    protected List<APIKitSpec> findAPIKitSpec(Document xmlDoc) {
        ArrayList<APIKitSpec> list = new ArrayList<>();
        final Element rootEl = xmlDoc.getDocumentElement();
        if ("http://www.mulesoft.org/schema/mule/core".equals(rootEl.getNamespaceURI()) &&
                "mule".equals(rootEl.getLocalName())) {
            final NodeList nodeList = rootEl.getElementsByTagNameNS("http://www.mulesoft.org/schema/mule/mule-apikit", "config");
            final int totalTags = nodeList.getLength();
            for (int i = 0; i < totalTags; i++) {
                final Element el = (Element) nodeList.item(i);
                final String name = el.getAttribute("name");
                String api = el.getAttribute("api");
                if (api != null) {
                    api = el.getAttribute("raml");
                }
                if (api != null) {
                    list.add(APIKitSpec.create(name, api, getArtifactId()));
                }
            }
        }
        return list;
    }
}
