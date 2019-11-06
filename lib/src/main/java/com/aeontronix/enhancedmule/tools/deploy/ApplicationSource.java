/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.deploy;

import com.aeontronix.enhancedmule.tools.AnypointClient;
import com.aeontronix.enhancedmule.tools.HttpException;
import com.aeontronix.enhancedmule.tools.api.provision.AnypointConfigFileDescriptor;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class ApplicationSource implements Closeable {
    protected AnypointClient client;

    public ApplicationSource(AnypointClient client) {
        this.client = client;
    }

    public abstract String getFileName();

    public abstract File getLocalFile();

    public abstract boolean exists();

    public abstract AnypointConfigFileDescriptor getAPIProvisioningDescriptor() throws IOException, HttpException;

    @Nullable
    protected AnypointConfigFileDescriptor readDescriptorFromZip(File file) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        ZipEntry anypointJson = zipFile.getEntry("anypoint.json");
        if (anypointJson != null) {
            try (InputStream is = zipFile.getInputStream(anypointJson)) {
                return client.getJsonHelper().getJsonMapper().readValue(is, AnypointConfigFileDescriptor.class);
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
}