/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.commons.io.InMemInputFilterStream;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.PropertyDescriptor;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kloudtek.unpack.*;
import com.kloudtek.unpack.transformer.Transformer;
import com.kloudtek.util.FileUtils;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class ApplicationEnhancementCoreTransformer extends Transformer {
    private static final Logger logger = getLogger(ApplicationEnhancementCoreTransformer.class);
    public static final String META_INF_MULE_ARTIFACT_MULE_ARTIFACT_JSON = "META-INF/mule-artifact/mule-artifact.json";
    public static final String ENHANCED_MULE_TOOLS_FLOW_XML = "enhanced-mule-tools-flow.xml";
    public static final String ANYPOINT_JSON = "anypoint.json";
    private final boolean autoDiscovery;
    private final APIDescriptor api;
    private final ObjectMapper om = JsonHelper.createMapper();
    private final ApplicationDescriptor applicationDescriptor;
    private final File descriptorFile;

    public ApplicationEnhancementCoreTransformer(File descriptorFile, ApplicationDescriptor applicationDescriptor) {
        this.applicationDescriptor = applicationDescriptor;
        api = this.applicationDescriptor.getApi();
        this.descriptorFile = descriptorFile;
        this.autoDiscovery = api != null && api.isAddAutoDiscovery();
    }

    @Override
    public void apply(Source source, Destination destination) throws UnpackException {
        SourceFile anypointDescFile = (SourceFile) source.getFile("anypoint.json");
        try {
            source.add(new InMemSourceFile(ANYPOINT_JSON, ANYPOINT_JSON, FileUtils.toByteArray(descriptorFile)));
        } catch (IOException e) {
            throw new UnpackException(e);
        }
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<mule xmlns:api-gateway=\"http://www.mulesoft.org/schema/mule/api-gateway\" xmlns=\"http://www.mulesoft.org/schema/mule/core\" xmlns:doc=\"http://www.mulesoft.org/schema/mule/documentation\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd\">\n");
        if (autoDiscovery) {
            logger.info("Added autodiscovery using flow: " + api.getAutoDiscoveryFlow());
            xml.append("    <api-gateway:autodiscovery apiId=\"${anypoint.api.id}\" ignoreBasePath=\"true\" flowRef=\"").append(api.getAutoDiscoveryFlow()).append("\" />\n");
        }
        xml.append("</mule>");
        byte[] data = xml.toString().getBytes();
        SourceFile emFlowFile = (SourceFile) source.getFile(ENHANCED_MULE_TOOLS_FLOW_XML);
        if (emFlowFile != null) {
            emFlowFile.setInputStream(new ByteArrayInputStream(data));
        } else {
            source.add(new InMemSourceFile(ENHANCED_MULE_TOOLS_FLOW_XML, ENHANCED_MULE_TOOLS_FLOW_XML, data));
        }
        SourceFile file = (SourceFile) source.getFile(META_INF_MULE_ARTIFACT_MULE_ARTIFACT_JSON);
        if (file == null) {
            throw new UnpackException(META_INF_MULE_ARTIFACT_MULE_ARTIFACT_JSON + " not found");
        }
        file.setInputStream(new InMemInputFilterStream(file.getInputStream()) {
            @SuppressWarnings("unchecked")
            @Override
            protected byte[] transform(byte[] data) throws IOException {
                return IOUtils.toByteArray(os -> {
                    Map<String, Object> json;
                    json = om.readValue(data, Map.class);
                    Map<String, Object> clm = (Map<String, Object>) json.computeIfAbsent("classLoaderModelLoaderDescriptor", key -> new HashMap<>());
                    Map<String, Object> clmAttr = (Map<String, Object>) clm.computeIfAbsent("attributes", key -> new HashMap<>());
                    List<String> exportedResources = (List<String>) clmAttr.computeIfAbsent("exportedResources", key -> new ArrayList<String>());
                    List<String> configs = (List<String>) json.computeIfAbsent("configs", key -> new ArrayList<String>());
                    List<String> secureProperties = (List<String>) json.computeIfAbsent("secureProperties", key -> new ArrayList<>());

                    if (!configs.contains(ENHANCED_MULE_TOOLS_FLOW_XML)) {
                        configs.add(ENHANCED_MULE_TOOLS_FLOW_XML);
                    }
                    if (!exportedResources.contains(ENHANCED_MULE_TOOLS_FLOW_XML)) {
                        exportedResources.add(ENHANCED_MULE_TOOLS_FLOW_XML);
                    }

                    addSecureProperties(secureProperties, applicationDescriptor);

                    // write
                    om.writeValue(os, json);
                });
            }

            private void addSecureProperties(List<String> secureProperties, ApplicationDescriptor applicationDescriptor) {
                HashMap<String, PropertyDescriptor> propDesc = applicationDescriptor.getProperties();
                if (propDesc != null) {
                    for (PropertyDescriptor propertyDescriptor : propDesc.values()) {
                        if (propertyDescriptor.isSecure()) {
                            String name = propertyDescriptor.getName();
                            if (!secureProperties.contains(name)) {
                                secureProperties.add(name);
                            }
                        }
                    }
                }
            }
        });
    }
}
