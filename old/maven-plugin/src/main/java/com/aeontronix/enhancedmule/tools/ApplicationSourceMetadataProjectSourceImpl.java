/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.commons.xml.XmlUtils;
import com.aeontronix.enhancedmule.tools.application.ApplicationSourceMetadata;
import com.aeontronix.enhancedmule.tools.application.api.apikit.APIKitSpec;
import com.aeontronix.enhancedmule.tools.util.APISpecHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.io.File.separator;
import static org.slf4j.LoggerFactory.getLogger;

public class ApplicationSourceMetadataProjectSourceImpl extends ApplicationSourceMetadata {
    private static final Logger logger = getLogger(ApplicationSourceMetadataProjectSourceImpl.class);
    private final MavenProject project;
    private final File assetPagesDir;
    private final File apiSpecDir;

    public ApplicationSourceMetadataProjectSourceImpl(MavenProject project, File assetPagesDir, File apiSpecDir) {
        this.project = project;
        this.assetPagesDir = assetPagesDir;
        this.apiSpecDir = apiSpecDir;
    }

    @Override
    public String getArtifactId() {
        return project.getArtifactId();
    }

    @Override
    public String getName() {
        return project.getName();
    }

    @Override
    public String getVersion() {
        return project.getVersion();
    }

    @Override
    public String getDescription() {
        return project.getDescription();
    }

    @Override
    public File findIcon() {
        for (String fn : Arrays.asList("icon.svg", "icon.png", "icon.jpeg", "icon.jpg", "icon.gif")) {
            final File f = new File(project.getBasedir(), fn);
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }


    @Nullable
    public String findAPISpecFile(String... names) {
        if (apiSpecDir != null && apiSpecDir.exists()) {
            return findAPISpecFile(names != null ? Arrays.asList(names) : Collections.emptyList(), apiSpecDir);
        }
        return null;
    }

    @Override
    public APISpecHelper.APISpecVersion findAPISpecVersion(String assetMainFile) throws IOException {
        final File apiSpecFile = new File(apiSpecDir, assetMainFile);
        return APISpecHelper.findVersion(apiSpecFile);
    }

    @Nullable
    public static String findAPISpecFile(List<String> names, File dir) {
        if (dir.exists()) {
            final ArrayList<String> filenames = new ArrayList<>(names);
            filenames.add("api");
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

    private File findDependencyFile(String groupId, String artifactId, String version) {
        final Set<Artifact> artifacts = project.getArtifacts();
        if (artifacts != null) {
            for (Artifact artifact : artifacts) {
                if (artifact.getArtifactId().equals(groupId) &&
                        artifact.getGroupId().equals(artifactId) &&
                        artifact.getVersion().equals(version)) {
                    final File file = artifact.getFile();
                    if (file.exists()) {
                        return file;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getFileStrContent(String path) throws IOException {
        try (FileInputStream fis = new FileInputStream(project.getBasedir() + separator + path.replace("/", separator))) {
            return IOUtils.toString(fis);
        }
    }

    @Override
    @NotNull
    public Map<String, String> getPortalPages() throws IOException {
        HashMap<String, String> pages = new HashMap<>();
        if (assetPagesDir != null && assetPagesDir.exists()) {
            final File[] files = assetPagesDir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isFile()) {
                        final String fileName = file.getName();
                        int idx = fileName.indexOf(".");
                        if (idx != -1) {
                            pages.put(fileName.substring(0, idx), IOUtils.toString(file));
                        }
                    }
                }
            }
        }
        return pages;
    }

    @Override
    public JsonNode getJsonContentFromDependencyArchive(String groupId, String artifactId, String version, String path) throws IOException {
        final File file = findDependencyFile(groupId, artifactId, version);
        if (file == null) {
            throw new IOException("Dependency file not found");
        }
        final ZipFile zipFile = new ZipFile(file);
        final ZipEntry entry = zipFile.getEntry("exchange.json");
        if (entry != null) {
            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                return new ObjectMapper().readTree(inputStream);
            }
        } else {
            throw new IOException("File not found in dependency archive");
        }
    }

    @NotNull
    @Override
    public List<APIKitSpec> findAPIKitSpecs() throws IOException {
        ArrayList<APIKitSpec> list = new ArrayList<>();
        final File outputDir = new File(project.getBuild().getOutputDirectory());
        final File[] files = outputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
        if (files != null) {
            for (File file : files) {
                try {
                    final Document xmlDoc = XmlUtils.parse(file, true);
                    list.addAll(findAPIKitSpec(xmlDoc));
                } catch (SAXException e) {
                    throw new IOException(e);
                }
            }
        }
        return list;
    }
}
