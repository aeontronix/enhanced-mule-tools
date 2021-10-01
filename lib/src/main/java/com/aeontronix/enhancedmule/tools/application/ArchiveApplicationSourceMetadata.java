/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.commons.xml.XPathUtils;
import com.aeontronix.commons.xml.XmlUtils;
import com.aeontronix.enhancedmule.tools.application.api.apikit.APIKitSpec;
import com.aeontronix.enhancedmule.tools.util.APISpecHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ArchiveApplicationSourceMetadata extends ApplicationSourceMetadata {
    public static final String MULE_ARTIFACT_FILE = "META-INF/mule-artifact/mule-artifact.json";
    public static final String CLASSLOADER_MODEL_FILE = "META-INF/mule-artifact/classloader-model.json";
    private final String artifactId;
    private final ZipFile zipFile;
    private final List<? extends ZipEntry> zipFileEntries;
    private final String groupId;
    private final String name;
    private final String version;
    private final Set<String> zipFileEntryNames;

    public ArchiveApplicationSourceMetadata(File archiveFile) throws IOException {
        try {
            zipFile = new ZipFile(archiveFile);
            zipFileEntries = zipFile.stream().collect(Collectors.toList());
            zipFileEntryNames = zipFileEntries.stream().map(ZipEntry::getName).collect(Collectors.toSet());
            final ObjectMapper objectMapper = new ObjectMapper();
            final JsonNode clModel = getJsonEntry(zipFile, objectMapper, CLASSLOADER_MODEL_FILE);
            final ObjectNode artifactCoordinates = (ObjectNode) clModel.get("artifactCoordinates");
            if (artifactCoordinates == null) {
                throw new IOException(CLASSLOADER_MODEL_FILE + " is missing artifactCoordinates");
            }
            groupId = Objects.requireNonNull(artifactCoordinates.get("groupId"), "groupId missing from " + CLASSLOADER_MODEL_FILE).textValue();
            artifactId = Objects.requireNonNull(artifactCoordinates.get("artifactId"), "artifactId missing from " + CLASSLOADER_MODEL_FILE).textValue();
            version = Objects.requireNonNull(artifactCoordinates.get("version"), "version missing from " + CLASSLOADER_MODEL_FILE).textValue();
            final String pomPath = "META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml";
            final Document pom = getXmlEntry(pomPath);
            final Node nameNode = XPathUtils.evalXPathNode("/project/name", pom);
            name = nameNode != null ? nameNode.getTextContent() : null;
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    private Document getXmlEntry(String pomPath) throws IOException {
        try {
            final ZipEntry pomZipEntry = zipFile.getEntry(pomPath);
            try (final InputStream inputStream = zipFile.getInputStream(pomZipEntry)) {
                return XmlUtils.parse(inputStream, false);
            }
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    private JsonNode getJsonEntry(ZipFile zipFile, ObjectMapper objectMapper, String filename) throws IOException {
        final ZipEntry entry = zipFile.getEntry(filename);
        if (entry == null) {
            throw new IOException(filename + " not found");
        }
        return objectMapper.readTree(zipFile.getInputStream(entry));
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
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
    public String findAPISpecFile(String... assetId) throws IOException {
        final List<String> filenames = new ArrayList<>();
        if( assetId != null ) {
            Collections.addAll(filenames, assetId);
        }
        filenames.add("api");
        for (String filename : filenames) {
            for (String apiExt : apiExts) {
                final String path = "api/" + filename + apiExt;
                if (zipFileEntryNames.contains(path)) {
                    return filename + apiExt;
                }
            }
        }
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
    public Map<String, String> getPortalPages() throws IOException {
        return zipFileEntries.stream().filter(e -> {
            final String name = e.getName().toLowerCase();
            return name.startsWith("exchange/") && name.endsWith(".md");
        }).collect(Collectors.toMap((Function<ZipEntry, String>) ZipEntry::getName, (Function<ZipEntry, String>) e -> {
            try {
                try (InputStream is = zipFile.getInputStream(e)) {
                    return IOUtils.toString(is);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }));
    }

    @Override
    public JsonNode getJsonContentFromDependencyArchive(String groupId, String artifactId, String version, String path) throws IOException {
        return null;
    }

    @NotNull
    @Override
    public List<APIKitSpec> findAPIKitSpecs() throws IOException {
        try {
            ArrayList<APIKitSpec> list = new ArrayList<>();
            for (ZipEntry e : zipFileEntries) {
                if (e.getName().toLowerCase().endsWith(".xml")) {
                    try (InputStream is = zipFile.getInputStream(e)) {
                        final Document xmlDoc;
                        xmlDoc = XmlUtils.parse(is, true);
                        list.addAll(findAPIKitSpec(xmlDoc));
                    }
                }
            }
            return list;
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
    }
}
