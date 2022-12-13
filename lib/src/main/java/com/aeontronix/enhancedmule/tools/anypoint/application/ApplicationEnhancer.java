/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.application;

import com.aeontronix.commons.file.FileUtils;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.APIDescriptor;
import com.aeontronix.unpack.FileType;
import com.aeontronix.unpack.SourceFile;
import com.aeontronix.unpack.UnpackException;
import com.aeontronix.unpack.Unpacker;
import com.aeontronix.unpack.transformer.FileMatcher;
import com.aeontronix.unpack.transformer.JacksonTransformer;
import com.aeontronix.unpack.transformer.Transformer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.aeontronix.commons.Required.CREATE;
import static com.aeontronix.commons.Required.REQUIRED;
import static org.slf4j.LoggerFactory.getLogger;

public class ApplicationEnhancer {
    public static final String META_INF_MULE_ARTIFACT_MULE_ARTIFACT_JSON = "META-INF/mule-artifact/mule-artifact.json";
    public static final String ENHANCED_MULE_TOOLS_FLOW_XML = "enhanced-mule-tools-flow.xml";
    public static final String ANYPOINT_JSON = "anypoint.json";
    private static final Logger logger = getLogger(ApplicationEnhancer.class);

    public static void enhanceApplicationArchive(File file, File descriptorFile, ApplicationDescriptor applicationDescriptor,
                                                 boolean deletePreWeave, boolean excludeIgnoreBasePath) throws IOException, UnpackException {
        if (logger.isDebugEnabled()) {
            logger.debug(System.getProperties().toString());
        }
        boolean mulestudio = checkProperty("eclipse.product", "mulestudio") || checkProperty("eclipse.commands", "studio");
        File oldArtifactFile = new File(file.getPath() + ".preweaving");
        if (oldArtifactFile.exists()) {
            FileUtils.delete(oldArtifactFile);
        }
        if (!file.renameTo(oldArtifactFile)) {
            throw new IOException("Unable to move " + file.getPath() + " to " + oldArtifactFile.getPath());
        }
        Unpacker unpacker = new Unpacker(oldArtifactFile, FileType.ZIP, file, FileType.ZIP);
        final ArrayList<Transformer> transformers = new ArrayList<>();
        transformers.add(new Transformer(new FileMatcher(ANYPOINT_JSON, CREATE)) {
            @Override
            public byte[] transform(SourceFile sourceFile) throws Exception {
                return FileUtils.toByteArray(descriptorFile);
            }
        });
        APIDescriptor api = applicationDescriptor.getApi();
        boolean autoDiscovery = api != null && api.isAddAutoDiscovery();
        transformers.add(new Transformer(new FileMatcher(ENHANCED_MULE_TOOLS_FLOW_XML, CREATE)) {
            @Override
            public byte[] transform(SourceFile sourceFile) throws Exception {
                StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<mule xmlns:api-gateway=\"http://www.mulesoft.org/schema/mule/api-gateway\" xmlns=\"http://www.mulesoft.org/schema/mule/core\" xmlns:doc=\"http://www.mulesoft.org/schema/mule/documentation\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd\">\n");
                if (autoDiscovery) {
                    if (mulestudio) {
                        logger.warn("Skipped adding autodiscovery since running on studio");
                    } else {
                        logger.info("Added autodiscovery using flow: " + api.getAutoDiscoveryFlow());
                        xml.append("    <api-gateway:autodiscovery apiId=\"${anypoint.api.id}\" ");
                        if (!excludeIgnoreBasePath) {
                            xml.append("ignoreBasePath=\"true\" ");
                        }
                        xml.append("flowRef=\"").append(api.getAutoDiscoveryFlow()).append("\" />\n");
                    }
                }
                xml.append("</mule>");
                return xml.toString().getBytes();
            }
        });
        transformers.add(new JacksonTransformer<ObjectNode>(new FileMatcher(META_INF_MULE_ARTIFACT_MULE_ARTIFACT_JSON, REQUIRED), ObjectNode.class, new ObjectMapper()) {
            @Override
            public JsonNode transform(ObjectNode root) throws Exception {
                final ObjectNode clm = getObject(root, "classLoaderModelLoaderDescriptor", true);
                final ObjectNode clmAttr = getObject(clm, "attributes", true);
                final ArrayNode exportedResources = getOrCreateArray(clmAttr, "exportedResources");
                if (!contains(exportedResources, ENHANCED_MULE_TOOLS_FLOW_XML)) {
                    exportedResources.add(ENHANCED_MULE_TOOLS_FLOW_XML);
                }
                final ArrayNode configs = getOrCreateArray(root, "configs");
                if (!contains(configs, ENHANCED_MULE_TOOLS_FLOW_XML)) {
                    configs.add(ENHANCED_MULE_TOOLS_FLOW_XML);
                }
                final ArrayNode secureProperties = getOrCreateArray(root, "secureProperties");
                addSecureProperty(secureProperties, "anypoint.platform.client_secret");
                addSecureProperty(secureProperties, "anypoint.api.client.secret");
//                HashMap<String, PropertyDescriptor> propDesc = applicationDescriptor.getProperties();
//                HashMap<String, PropertyDescriptor> propDesc = new HashMap<>();
//                propDesc.put("anypoint.platform.client_secret",true);
//                if (propDesc != null) {
//                    for (Map.Entry<String, PropertyDescriptor> prop : propDesc.entrySet()) {
//                        if (prop.getValue().isSecure()) {
//                            addSecureProperty(secureProperties, prop);
//                        }
//                    }
//                }
                return root;
            }

            private void addSecureProperty(ArrayNode secureProperties, String propertyKey) {
                if (!contains(secureProperties, propertyKey)) {
                    secureProperties.add(propertyKey);
                }
            }
        });
        unpacker.addTransformers(transformers);
        unpacker.unpack();
        if (deletePreWeave) {
            oldArtifactFile.delete();
        }
    }

    private static boolean checkProperty(String key, String value) {
        final String eclipse = System.getProperty(key);
        return eclipse != null && eclipse.toLowerCase().contains(value);
    }

    public byte[] getFile(File archive, String path) throws IOException {
        final ZipFile zipFile = new ZipFile(archive);
        final ZipEntry entry = zipFile.getEntry(path);
        if (entry != null) {
            return IOUtils.toByteArray(zipFile.getInputStream(entry));
        } else {
            return null;
        }
    }
}
