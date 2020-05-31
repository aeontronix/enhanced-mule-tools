/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.deploy;

import com.kloudtek.unpack.*;
import com.kloudtek.unpack.transformer.Transformer;

import java.io.ByteArrayInputStream;

public class AddEMTFlowTransformer extends Transformer {
    public static final String ENHANCED_MULE_TOOLS_FLOW_XML = "enhanced-mule-tools-flow.xml";
    private String flowname;

    public AddEMTFlowTransformer(String flowname) {
        this.flowname = flowname;
    }

    @Override
    public void apply(Source source, Destination destination) throws UnpackException {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<mule xmlns:api-gateway=\"http://www.mulesoft.org/schema/mule/api-gateway\" xmlns=\"http://www.mulesoft.org/schema/mule/core\" xmlns:doc=\"http://www.mulesoft.org/schema/mule/documentation\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd http://www.mulesoft.org/schema/mule/api-gateway http://www.mulesoft.org/schema/mule/api-gateway/current/mule-api-gateway.xsd\">\n");
        xml.append("    <api-gateway:autodiscovery apiId=\"${anypoint.api.id}\" ignoreBasePath=\"true\" flowRef=\"" + flowname + "\" />\n");
        xml.append("</mule>");
        SourceFile file = (SourceFile) source.getFile(ENHANCED_MULE_TOOLS_FLOW_XML);
        byte[] data = xml.toString().getBytes();
        if (file != null) {
            file.setInputStream(new ByteArrayInputStream(data));
        } else {
            source.add(new InMemSourceFile(ENHANCED_MULE_TOOLS_FLOW_XML, ENHANCED_MULE_TOOLS_FLOW_XML, data));
        }
    }
}
