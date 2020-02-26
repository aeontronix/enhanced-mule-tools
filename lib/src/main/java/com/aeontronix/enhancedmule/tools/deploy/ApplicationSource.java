/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.deploy;

import com.aeontronix.enhancedmule.tools.AnypointClient;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.api.provision.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.api.provision.AnypointDescriptor;
import com.aeontronix.enhancedmule.tools.api.provision.PropertyDescriptor;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.kloudtek.util.StringUtils;
import com.kloudtek.util.io.IOUtils;
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

    public abstract AnypointDescriptor getAPIProvisioningDescriptor(APIProvisioningConfig apiProvisioningConfig) throws IOException, HttpException;

    @Nullable
    protected AnypointDescriptor readDescriptorFromZip(File file, APIProvisioningConfig apiProvisioningConfig) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        ZipEntry anypointJson = zipFile.getEntry("anypoint.json");
        if (anypointJson != null) {
            try (InputStream is = zipFile.getInputStream(anypointJson)) {
                return readDescriptor(apiProvisioningConfig, is);
            }
        } else {
            return null;
        }
    }

    @Nullable
    private AnypointDescriptor readDescriptor(APIProvisioningConfig apiProvisioningConfig, InputStream is) throws IOException {
        String json = IOUtils.toString(is);
        json = StringUtils.substituteVariables(json, apiProvisioningConfig.getVariables());
        AnypointDescriptor descriptor = client.getJsonHelper().getJsonMapper().readValue(json, AnypointDescriptor.class);
        if(descriptor.getProperties()!= null){
            for (Map.Entry<String, PropertyDescriptor> entry : descriptor.getProperties().entrySet()) {
                entry.getValue().setName(entry.getKey());
            }
        }
        return descriptor;
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
