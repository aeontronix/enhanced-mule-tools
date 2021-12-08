/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy;

import com.aeontronix.commons.FileUtils;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.application.ApplicationSourceMetadata;
import com.aeontronix.enhancedmule.tools.application.ArchiveApplicationSourceMetadata;
import com.aeontronix.enhancedmule.tools.exchange.APISpecSource;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class ApplicationSource implements APISpecSource, Closeable {
    private final ObjectMapper objectMapper = JsonHelper.createMapper();
    protected AnypointClient client;
    protected ApplicationIdentifier applicationIdentifier;

    public ApplicationSource(AnypointClient client) {
        this.client = client;
    }

    public static ApplicationSource create(String orgId, AnypointClient client, String path) throws IOException {
        if (path.startsWith("exchange://")) {
            return new ExchangeApplicationSource(orgId, client, path);
        } else {
            return new FileApplicationSource(client, new File(path));
        }
    }

    public ApplicationSourceMetadata getApplicationSourceMetadata() throws IOException {
        return new ArchiveApplicationSourceMetadata(getLocalFile());
    }

    @Nullable
    protected ObjectNode readDescriptorFromZip(File file) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        ZipEntry anypointJson = zipFile.getEntry("anypoint.json");
        if (anypointJson != null) {
            try (InputStream is = zipFile.getInputStream(anypointJson)) {
                return (ObjectNode) objectMapper.readTree(is);
            }
        } else {
            return null;
        }
    }

    public String getArtifactId() {
        final ApplicationIdentifier applicationIdentifier = getApplicationIdentifier();
        if (applicationIdentifier != null) {
            return applicationIdentifier.getArtifactId();
        } else {
            return null;
        }
    }

    @Override
    public Set<String> listAPISpecFiles() throws IOException {
        HashSet<String> files = new HashSet<>();
        Enumeration<? extends ZipEntry> entries = new ZipFile(getLocalFile()).entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().toLowerCase().startsWith("api/")) {
                String name = zipEntry.getName().substring(4);
                if (StringUtils.isNotBlank(name)) {
                    files.add(name);
                }
            }
        }
        return files;
    }

    @Override
    public void writeAPISpecFile(String name, OutputStream os) throws IOException {
        try (final ZipFile zipFile = new ZipFile(getLocalFile())) {
            final ZipEntry entry = zipFile.getEntry("api/" + name);
            IOUtils.copy(zipFile.getInputStream(entry), os);
        }
    }

    public void replaceFile(File file) throws IOException {
        FileUtils.copy(getLocalFile(), file);
    }

    public abstract String getFileName();

    public abstract File getLocalFile() throws IOException;

    public abstract boolean exists();

    public abstract ObjectNode getAnypointDescriptor() throws IOException, HttpException;

    public abstract ApplicationIdentifier getApplicationIdentifier();

    public abstract Map<String, Object> getSourceJson(JsonHelper jsonHelper);
}
