/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.provisioning.AnypointDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.TempFile;
import com.aeontronix.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class ApplicationSource implements Closeable {
    protected AnypointClient client;

    public ApplicationSource(AnypointClient client) {
        this.client = client;
    }

    public abstract String getFileName();

    public abstract File getLocalFile();

    public abstract boolean exists();

    public abstract AnypointDescriptor getAnypointDescriptor(APIProvisioningConfig apiProvisioningConfig) throws IOException, HttpException;

    @Nullable
    protected AnypointDescriptor readDescriptorFromZip(File file, APIProvisioningConfig apiProvisioningConfig) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        ZipEntry anypointJson = zipFile.getEntry("anypoint.json");
        if (anypointJson != null) {
            try (InputStream is = zipFile.getInputStream(anypointJson)) {
                return AnypointDescriptor.read(apiProvisioningConfig, is);
            }
        } else {
            return null;
        }
    }

    public abstract String getArtifactId();

    public static ApplicationSource create(String orgId, AnypointClient client, String path) {
        if (path.startsWith("exchange://")) {
            return new ExchangeApplicationSource(orgId, client, path);
        } else {
            return new FileApplicationSource(client, new File(path));
        }
    }

    public abstract Map<String, Object> getSourceJson(JsonHelper jsonHelper);

    public void getAPISpecificationFiles(String assetId, String assetVersion, String assetMainFile, String assetClassifier, TempFile apiSpecFile) throws IOException {
        try (ZipOutputStream os = new ZipOutputStream(new FileOutputStream(apiSpecFile))) {
            if (assetVersion.toLowerCase().endsWith("-snapshot")) {
                os.putNextEntry(new ZipEntry(assetMainFile));
                String ext = assetClassifier.toLowerCase();
                if (ext.endsWith(".raml")) {
                    os.write(("#%RAML 1.0\ntitle: " + assetId).getBytes());
                } else if (ext.endsWith(".yaml") || ext.endsWith(".yml") ) {
                    os.write(("swagger: \"2.0\"\n" +
                            "info:\n" +
                            "  version: 1.0.0\n" +
                            "  title: "+assetId+"\n" +
                            "paths: {}").getBytes());
                } else {
                    os.write(("{\n" +
                            "  \"swagger\": \"2.0\",\n" +
                            "  \"info\": {\n" +
                            "    \"version\": \"1.0.0\",\n" +
                            "    \"title\": \""+assetId+"\"\n" +
                            "  },\n" +
                            "  \"paths\": {}\n" +
                            "}").getBytes());
                }
            }
            ZipFile zipFile = new ZipFile(getLocalFile());
            if (zipFile.getEntry("api/" + assetMainFile) == null) {
                throw new IOException("asset main file not found: " + assetMainFile);
            }
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.getName().toLowerCase().startsWith("api/")) {
                    String name = zipEntry.getName().substring(4);
                    if (StringUtils.isNotBlank(name)) {
                        os.putNextEntry(new ZipEntry(name));
                        IOUtils.copy(zipFile.getInputStream(zipEntry), os);
                        os.closeEntry();
                    }
                }
            }
        }
    }
}
