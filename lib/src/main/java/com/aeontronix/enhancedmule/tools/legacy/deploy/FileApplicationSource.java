/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy;

import com.aeontronix.commons.exception.UnexpectedException;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.slf4j.LoggerFactory.getLogger;

public class FileApplicationSource extends ApplicationSource {
    private static final Logger logger = getLogger(FileApplicationSource.class);
    private File file;

    public FileApplicationSource(LegacyAnypointClient client, File file) {
        super(client);
        this.file = file;
    }

    public FileApplicationSource(LegacyAnypointClient client, File file, ApplicationIdentifier applicationIdentifier) {
        this(client, file);
        this.applicationIdentifier = applicationIdentifier;
    }

    @Override
    public String getFileName() {
        return file.getName();
    }

    @Override
    public File getLocalFile() throws IOException {
        return file;
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public ObjectNode getAnypointDescriptor() throws IOException {
        return readDescriptorFromZip(file);
    }

    @Override
    public ApplicationIdentifier getApplicationIdentifier() {
        if (applicationIdentifier == null) {
            logger.debug("no application identifier found, reading from pom");
            try {
                ZipFile zipFile = new ZipFile(file);
                ZipEntry artJson = zipFile.getEntry("META-INF/mule-artifact/mule-artifact.json");
                if (artJson != null) {
                    try (InputStream is = zipFile.getInputStream(artJson)) {
                        final ObjectNode jsonNode = (ObjectNode) client.getJsonHelper().getJsonMapper().readTree(is);
                        final JsonNode nameJson = jsonNode.get("name");
                        if (nameJson != null && !nameJson.isNull()) {
                            final String[] name = nameJson.textValue().split(":");
                            if (name.length == 3) {
                                applicationIdentifier = new ApplicationIdentifier(name[0], name[1], name[2]);
                                logger.debug("Loaded ApplicationIdentifier from archive: "+applicationIdentifier);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new UnexpectedException(e);
            }
        }
        return applicationIdentifier;
    }

    @Override
    public Map<String, Object> getSourceJson(JsonHelper jsonHelper) {
        throw new UnsupportedOperationException("getSourceJson() not supported for file source");
    }

    @Override
    public void close() throws IOException {
    }
}
