/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.APISpecSource;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class ApplicationSource implements APISpecSource, Closeable {
    private static final Logger logger = getLogger(ApplicationSource.class);
    protected LegacyAnypointClient client;
    protected ApplicationIdentifier applicationIdentifier;

    public ApplicationSource(LegacyAnypointClient client) {
        this.client = client;
    }

    public static ApplicationSource create(String orgId, LegacyAnypointClient client, String path) throws IOException {
        if (path.startsWith("exchange://")) {
            return new ExchangeApplicationSource(orgId, client, path);
        } else {
            return new FileApplicationSource(client, new File(path));
        }
    }

    public abstract String getFileName();

    public abstract File getLocalFile() throws IOException;

    public abstract boolean exists();

    public abstract ObjectNode getAnypointDescriptor() throws IOException, HttpException;

    @Nullable
    protected ObjectNode readDescriptorFromZip(File file) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        ZipEntry anypointJson = zipFile.getEntry("anypoint.json");
        if (anypointJson != null) {
            try (InputStream is = zipFile.getInputStream(anypointJson)) {
                return (ObjectNode) client.getJsonHelper().getJsonMapper().readTree(is);
            }
        } else {
            return null;
        }
    }

    public String getArtifactId() {
        final ApplicationIdentifier applicationIdentifier = getApplicationIdentifier();
        if( applicationIdentifier != null ) {
            return applicationIdentifier.getArtifactId();
        } else {
            return null;
        }
    }

    public abstract ApplicationIdentifier getApplicationIdentifier();

    public abstract Map<String, Object> getSourceJson(JsonHelper jsonHelper);

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

    public abstract com.aeontronix.anypointsdk.amc.application.ApplicationSource toSDKSource();
}
