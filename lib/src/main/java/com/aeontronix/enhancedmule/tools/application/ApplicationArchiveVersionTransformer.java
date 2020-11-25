/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.aeontronix.unpack.transformer.RenameTransformer;
import com.aeontronix.unpack.transformer.SetPropertyTransformer;
import com.aeontronix.unpack.transformer.Transformer;
import com.aeontronix.unpack.transformer.XMLTransformer;
import com.kloudtek.util.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
        final String pomPath = ApplicationArchiveHelper.pomPath(appId, appId.getGroupId());
        final String pomPropsPath = ApplicationArchiveHelper.mavenMetaPath(appId, appId.getGroupId(), "pom.properties");
        HashMap<String,String> pomProps = new HashMap<>();
        pomProps.put("groupId",orgId);
        if(newVersion != null ) {
            pomProps.put("version",newVersion);
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
                new SetPropertyTransformer(pomPropsPath,REQUIRED, pomProps),
                new RenameTransformer(pomPath,ApplicationArchiveHelper.mavenMetaPath(appId, orgId,"pom.xml")),
                new RenameTransformer(pomPropsPath,ApplicationArchiveHelper.mavenMetaPath(appId, orgId,"pom.properties"))
        );
    }

//    @Override
//    public void apply(Source source, Destination destination) throws UnpackException {
//        SourceFile pom = (SourceFile) source.getFile(pomPath);
//        if (pom == null) {
//            throw new UnpackException("Unable to find pom: " + pomPath);
//        }
//        if (source.getFile("anypoint.json") != null) {
//            addFileTransform(source, destination, "anypoint.json", "anypoint.json",
//                    new AnypointDescTransformer(), false);
//        }
//        addFileTransform(source, destination, "classloader-model.json", "META-INF/mule-artifact/classloader-model.json",
//                new ClassLoaderModelFileTransformer(), false);
//        addFileTransform(source, destination, "mule-artifact.json", "META-INF/mule-artifact/mule-artifact.json",
//                new MuleArtifactFileTransformer(), false);
//        addFileTransform(source, destination, "pom.properties", ApplicationArchiveHelper.mavenMetaPath(appId, appId.getGroupId(), "pom.properties"),
//                new PomPropertiesTransformer(), false);
//    }
//
//    class AnypointDescTransformer extends JsonFileTransformer<ObjectNode> {
//        @Override
//        protected ObjectNode transformJson(ObjectNode root) throws UnpackException {
//            if (newVersion != null) {
//                root.put("version", newVersion);
//            }
//            final ObjectNode api = (ObjectNode) root.get("api");
//            if (api != null) {
//                final JsonNode assetVersion = api.get("assetVersion");
//                if (assetVersion != null) {
//                    api.put("assetVersion", assetVersion.textValue() + "-" + snapshotTimestamp);
//                }
//            }
//            return root;
//        }
//    }
//
//    class ClassLoaderModelFileTransformer extends JsonFileTransformer<ObjectNode> {
//        @Override
//        protected ObjectNode transformJson(ObjectNode root) throws UnpackException {
//            final ObjectNode artifactCoordinates = (ObjectNode) root.get("artifactCoordinates");
//            if (artifactCoordinates == null) {
//                throw new UnpackException("artifactCoordinates not found");
//            }
//            artifactCoordinates.put("groupId", orgId);
//            if (newVersion != null) {
//                artifactCoordinates.put("version", newVersion);
//            }
//            return root;
//        }
//    }
//
//    class MuleArtifactFileTransformer extends JsonFileTransformer<ObjectNode> {
//        @Override
//        protected ObjectNode transformJson(ObjectNode root) throws UnpackException {
//            root.put("name", orgId + ":" + appId.getArtifactId() + ":" + newVersion);
//            return root;
//        }
//    }
//
//    class POMFileTransformer extends XMLFileTranformer {
//        @Override
//        protected Document transformXml(Document root) {
//            final Element project = XmlUtils.getChildElement(root, "project", false);
//            XmlUtils.getChildElement(project, "groupId", true).setTextContent(orgId);
//            if (newVersion != null) {
//                XmlUtils.getChildElement(project, "version", true).setTextContent(newVersion);
//            }
//            return root;
//        }
//    }
//
//    class PomPropertiesTransformer implements FileTransformer {
//        @Override
//        public byte[] transform(byte[] data) throws UnpackException {
//            try {
//                Properties props = new Properties();
//                props.load(new ByteArrayInputStream(data));
//                props.put("groupId", orgId);
//                if (newVersion != null) {
//                    props.put("version", newVersion);
//                }
//                final ByteArrayOutputStream tmp = new ByteArrayOutputStream();
//                props.store(tmp, "");
//                tmp.close();
//                return tmp.toByteArray();
//            } catch (IOException e) {
//                throw new UnpackException(e);
//            }
//        }
//    }
}
