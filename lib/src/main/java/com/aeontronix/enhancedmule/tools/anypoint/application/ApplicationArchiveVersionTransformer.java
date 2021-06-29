/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.application;

import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.unpack.transformer.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.aeontronix.commons.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.aeontronix.commons.Required.OPTIONAL;
import static com.aeontronix.commons.Required.REQUIRED;

public class ApplicationArchiveVersionTransformer {
    private final ApplicationIdentifier appId;
    private final String orgId;
    private final String newVersion;
    private ApplicationDescriptor anypointDescriptor;
    private String snapshotTimestamp;

    public ApplicationArchiveVersionTransformer(ApplicationIdentifier appId, String orgId, String newVersion, String snapshotTimestamp) {
        this.appId = appId;
        this.orgId = orgId;
        this.newVersion = newVersion;
        this.snapshotTimestamp = snapshotTimestamp;
    }

    public static List<Transformer> getTransformers(ApplicationIdentifier appId, String orgId, String newVersion, String snapshotTimestamp) {
        final String pomPath = MavenHelper.pomPath(appId, appId.getGroupId());
        final String pomPropsPath = MavenHelper.mavenMetaPath(appId, appId.getGroupId(), "pom.properties");
        HashMap<String, String> pomProps = new HashMap<>();
        pomProps.put("groupId", orgId);
        if (newVersion != null) {
            pomProps.put("version", newVersion);
        }
        return Arrays.asList(
                new XMLTransformer(pomPath, REQUIRED, false, true) {
                    @Override
                    public void transform(Document document) throws Exception {
                        final Element project = XmlUtils.getChildElement(document, "project", false);
                        XmlUtils.getChildElement(project, "groupId", true).setTextContent(orgId);
                        if (newVersion != null) {
                            XmlUtils.getChildElement(project, "version", true).setTextContent(newVersion);
                        }
                    }
                },
                new SetPropertyTransformer(pomPropsPath, REQUIRED, pomProps),
                new RenameTransformer("META-INF/maven/" + appId.getGroupId() + "/",
                        "META-INF/maven/" + orgId + "/", true),
                new RenameTransformer("META-INF/maven/" + appId.getGroupId() + "/" + appId.getArtifactId() + "/",
                        "META-INF/maven/" + orgId + "/" + appId.getArtifactId() + "/", true),
                new RenameTransformer(pomPath, MavenHelper.mavenMetaPath(appId, orgId, "pom.xml")),
                new RenameTransformer(pomPropsPath, MavenHelper.mavenMetaPath(appId, orgId, "pom.properties")),
                new JacksonTransformer<ObjectNode>("anypoint.json", OPTIONAL, ObjectNode.class) {
                    @Override
                    public JsonNode transform(ObjectNode root) throws Exception {
                        if (newVersion != null) {
                            root.put("version", newVersion);
                        }
                        final ObjectNode api = (ObjectNode) root.get("api");
                        if (api != null) {
                            final JsonNode assetVersion = api.get("assetVersion");
                            if (assetVersion != null && assetVersion.textValue().toLowerCase().contains("-snapshot")) {
                                api.put("assetVersion", assetVersion.textValue() + "-" + snapshotTimestamp);
                            }
                        }
                        return root;
                    }
                },
                new JacksonTransformer<ObjectNode>("META-INF/mule-artifact/classloader-model.json", REQUIRED, ObjectNode.class) {
                    @Override
                    public JsonNode transform(ObjectNode root) throws Exception {
                        final ObjectNode artifactCoordinates = getObject(root, "artifactCoordinates", true);
                        artifactCoordinates.put("groupId", orgId);
                        if (newVersion != null) {
                            artifactCoordinates.put("version", newVersion);
                        }
                        return root;
                    }
                },
                new JacksonTransformer<ObjectNode>("META-INF/mule-artifact/mule-artifact.json",REQUIRED, ObjectNode.class) {
                    @Override
                    public JsonNode transform(ObjectNode root) throws Exception {
                        root.put("name", orgId + ":" + appId.getArtifactId() + ":" + newVersion);
                        return root;
                    }
                }
        );
    }
}
