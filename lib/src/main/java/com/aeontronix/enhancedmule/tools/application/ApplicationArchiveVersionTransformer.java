/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.enhancedmule.tools.util.AbstractEMTTransformer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kloudtek.unpack.Destination;
import com.kloudtek.unpack.Source;
import com.kloudtek.unpack.UnpackException;
import com.kloudtek.util.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ApplicationArchiveVersionTransformer extends AbstractEMTTransformer {
    private final ApplicationIdentifier appId;
    private final String orgId;
    private final String newVersion;

    public ApplicationArchiveVersionTransformer(ApplicationIdentifier appId, String orgId, String newVersion) {
        this.appId = appId;
        this.orgId = orgId;
        this.newVersion = newVersion;
    }

    @Override
    public void apply(Source source, Destination destination) throws UnpackException {
        addFileTransform(source, destination, "classloader-model.json", "META-INF/mule-artifact/classloader-model.json",
                new ClassLoaderModelFileTransformer(), false);
        addFileTransform(source, destination, "mule-artifact.json", "META-INF/mule-artifact/mule-artifact.json",
                new MuleArtifactFileTransformer(), false);
        addFileTransform(source, destination, "pom.xml", ApplicationArchiveHelper.pomPath(appId, appId.getGroupId()),
                new POMFileTransformer(), false);
        addFileTransform(source, destination, "pom.properties", ApplicationArchiveHelper.mavenMetaPath(appId, appId.getGroupId(),"pom.properties"),
                new PomPropertiesTransformer(), false);
    }

    class ClassLoaderModelFileTransformer extends JsonFileTransformer<ObjectNode> {
        @Override
        protected ObjectNode transformJson(ObjectNode root) throws UnpackException {
            final ObjectNode artifactCoordinates = (ObjectNode) root.get("artifactCoordinates");
            if (artifactCoordinates == null) {
                throw new UnpackException("artifactCoordinates not found");
            }
            artifactCoordinates.put("groupId", orgId);
            artifactCoordinates.put("version", newVersion);
            return root;
        }
    }

    class MuleArtifactFileTransformer extends JsonFileTransformer<ObjectNode> {
        @Override
        protected ObjectNode transformJson(ObjectNode root) throws UnpackException {
            root.put("name",orgId+":"+appId.getArtifactId()+":"+newVersion);
            return root;
        }
    }

    class POMFileTransformer extends XMLFileTranformer {
        @Override
        protected Document transformXml(Document root) {
            final Element project = XmlUtils.getChildElement(root, "project", false);
            XmlUtils.getChildElement(project, "groupId", true).setTextContent(orgId);
            XmlUtils.getChildElement(project, "version", true).setTextContent(newVersion);
            return root;
        }
    }

    class PomPropertiesTransformer implements FileTransformer {
        @Override
        public byte[] transform(byte[] data) throws UnpackException {
            try {
                Properties props = new Properties();
                props.load(new ByteArrayInputStream(data));
                props.put("groupId", orgId);
                props.put("version", newVersion);
                final ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                props.store(tmp, "");
                tmp.close();
                return tmp.toByteArray();
            } catch (IOException e) {
                throw new UnpackException(e);
            }
        }
    }
}
