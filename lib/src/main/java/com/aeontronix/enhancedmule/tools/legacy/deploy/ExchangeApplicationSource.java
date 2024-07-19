/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy;

import com.aeontronix.commons.file.TempFile;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;

public class ExchangeApplicationSource extends ApplicationSource {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeApplicationSource.class);
    public static final String PREFIX = "exchange://";
    private ObjectNode apiProvisioningDescriptor;
    private String orgId;
    private String groupId;
    private String artifactId;
    private String version;
    private TempFile tempFile;

    ExchangeApplicationSource(String orgId, LegacyAnypointClient client, String url) throws IllegalArgumentException, IOException {
        super(client);
        if (!url.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Invalid exchange url ( must start with exchange:// ): " + url);
        }
        String[] els = url.substring(PREFIX.length()).split(":");
        if (els.length < 2 || els.length > 4) {
            throw new IllegalArgumentException("Invalid exchange url: " + url);
        }
        if (els.length == 2) {
            this.orgId = orgId;
            this.groupId = this.orgId;
            artifactId = els[0];
            version = els[1];
        } else if (els.length == 3) {
            this.orgId = els[0];
            groupId = this.orgId;
            artifactId = els[1];
            version = els[2];
        } else {
            this.orgId = els[0];
            groupId = els[1];
            artifactId = els[2];
            version = els[3];
        }
    }

    public ExchangeApplicationSource(LegacyAnypointClient client, String orgId, String groupId, String artifactId, String version) {
        super(client);
        this.client = client;
        this.orgId = orgId;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getOrgId() {
        return orgId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public ApplicationIdentifier getApplicationIdentifier() {
        if( applicationIdentifier == null ) {
            applicationIdentifier = new ApplicationIdentifier(groupId,artifactId,version);
        }
        return applicationIdentifier;
    }

    @Override
    public String getFileName() {
        return artifactId + "-" + version + ".jar";
    }

    @Override
    public File getLocalFile() throws IOException {
        if (tempFile == null) {
            tempFile = new TempFile("anyp-apparch");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                client.getHttpHelper().httpGetBasicAuth("https://maven.anypoint.mulesoft.com/api/v2/maven/" + groupId +
                        "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + "-mule-application.jar", fos);
            }
        }
        return tempFile;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public ObjectNode getAnypointDescriptorObjects() throws IOException, HttpException {
        if (apiProvisioningDescriptor == null) {
            final ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try {
                client.getHttpHelper().httpGetBasicAuth("https://maven.anypoint.mulesoft.com/api/v2/maven/" + groupId +
                        "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + "-anypoint-descriptor.json", buf);
                buf.close();
                apiProvisioningDescriptor = (ObjectNode) client.getJsonHelper().getJsonMapper().readTree(buf.toString());
            } catch (HttpException e) {
                if (e.getStatusCode() == 404) {
                    InputStream inputStream = readDescriptorFileFromZip(getLocalFile());
                    if (inputStream == null) {
                        return null;
                    } else {
                        return (ObjectNode) client.getJsonHelper().getJsonMapper().readTree(inputStream);
                    }
                } else {
                    throw e;
                }
            }
        }
        return apiProvisioningDescriptor;
    }

    @Override
    public Map<String, Object> getSourceJson(JsonHelper jsonHelper) {
        return jsonHelper.buildJsonMap()
                .set("source", "EXCHANGE")
                .set("groupId", groupId)
                .set("artifactId", artifactId)
                .set("version", version)
                .set("organizationId", orgId)
                .toMap();
    }

    @Override
    public com.aeontronix.anypointsdk.amc.application.ApplicationSource toSDKSource() {
        return new com.aeontronix.anypointsdk.amc.application.ExchangeApplicationSource(groupId, artifactId, version);
    }

    @Override
    public void close() throws IOException {
        IOUtils.close(tempFile);
    }
}
