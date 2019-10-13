/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.anypoint.deploy;

import com.aeontronix.anypoint.AnypointClient;
import com.aeontronix.anypoint.HttpException;
import com.aeontronix.anypoint.api.provision.APIProvisioningDescriptor;
import com.aeontronix.anypoint.util.JsonHelper;
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

    public abstract APIProvisioningDescriptor getAPIProvisioningDescriptor() throws IOException, HttpException;

    @Nullable
    protected APIProvisioningDescriptor readDescriptorFromZip(File file) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        ZipEntry anypointJson = zipFile.getEntry("anypoint.json");
        if (anypointJson != null) {
            try (InputStream is = zipFile.getInputStream(anypointJson)) {
                return client.getJsonHelper().getJsonMapper().readValue(is, APIProvisioningDescriptor.class);
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
