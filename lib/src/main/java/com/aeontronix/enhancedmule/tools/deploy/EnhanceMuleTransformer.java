/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.deploy;

import com.aeontronix.enhancedmule.tools.provisioning.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.PropertyDescriptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloudtek.unpack.*;
import com.kloudtek.unpack.transformer.Transformer;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.commons.io.InMemInputFilterStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class EnhanceMuleTransformer extends Transformer {
    public static final String META_INF_MULE_ARTIFACT_MULE_ARTIFACT_JSON = "META-INF/mule-artifact/mule-artifact.json";
    public static final String ENHANCED_MULE_TOOLS_FLOW_XML = "enhanced-mule-tools-flow.xml";
    private final boolean autoDiscovery;
    private ApplicationDescriptor apiProvisioningDescriptor;
    private final APIDescriptor api;

    public EnhanceMuleTransformer(@NotNull ApplicationDescriptor apiProvisioningDescriptor) {
        this.apiProvisioningDescriptor = apiProvisioningDescriptor;
        api = apiProvisioningDescriptor.getApi();
        this.autoDiscovery = api != null && api.isAddAutoDescovery();
    }

    @Override
    public void apply(Source source, Destination destination) throws UnpackException {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<mule xmlns:api-gateway=\"http://www.mulesoft.org/schema/mule/api-gateway\" xmlns=\"http://www.mulesoft.org/schema/mule/core\" xmlns:doc=\"http://www.mulesoft.org/schema/mule/documentation\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd\">\n");
        if (autoDiscovery) {
            xml.append("    <api-gateway:autodiscovery apiId=\"${anypoint.api.id}\" ignoreBasePath=\"true\" flowRef=\"" + api.getAutoDiscoveryFlow() + "\" />\n");
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
                    ObjectMapper om = new ObjectMapper();
                    json = om.readValue(data, Map.class);
                    Map<String,Object> clm = (Map<String, Object>) json.computeIfAbsent("classLoaderModelLoaderDescriptor", key -> new HashMap<>());
                    Map<String,Object> clmAttr = (Map<String, Object>) clm.computeIfAbsent("attributes", key -> new HashMap<>());
                    List<String> exportedResources = (List<String>) clmAttr.computeIfAbsent("exportedResources", key -> new ArrayList<String>());
                    List<String> configs = (List<String>) json.computeIfAbsent("configs", key -> new ArrayList<String>());
                    List<String> secureProperties = (List<String>) json.computeIfAbsent("secureProperties", key -> new ArrayList<>());

                    configs.add(ENHANCED_MULE_TOOLS_FLOW_XML);
                    exportedResources.add(ENHANCED_MULE_TOOLS_FLOW_XML);

                    addSecureProperties(secureProperties);

                    // write
                    om.writeValue(os, json);
                });
            }

            private void addSecureProperties(List<String> secureProperties) {
                HashMap<String, PropertyDescriptor> propDesc = apiProvisioningDescriptor.getProperties();
                if (propDesc != null) {
                    for (PropertyDescriptor propertyDescriptor : propDesc.values()) {
                        if (propertyDescriptor.isSecure()) {
                            String name = propertyDescriptor.getName();
                            if( !secureProperties.contains(name)) {
                                secureProperties.add(name);
                            }
                        }
                    }
                }
            }
        });

    }
}
