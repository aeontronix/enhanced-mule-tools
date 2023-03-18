/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.commons.exception.UnexpectedException;
import com.aeontronix.enhancedmule.tools.util.EMTProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;

import java.io.IOException;
import java.util.Map;

public class DescriptorLayers {
    private ObjectNode defaults;
    private ObjectNode overrides;

    public DescriptorLayers(EMTProperties properties) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JavaPropsMapper propsMapper = new JavaPropsMapper();
        defaults = getLayer("emt.defaults.", properties, objectMapper, propsMapper);
        overrides = getLayer("emt.overrides.", properties, objectMapper, propsMapper);
    }

    public ObjectNode getDefaults() {
        return defaults;
    }

    public ObjectNode getOverrides() {
        return overrides;
    }

    public static ObjectNode getLayer(String prefix, EMTProperties properties, ObjectMapper objectMapper, JavaPropsMapper propsMapper) {
        try {
            Map<String, String> props = properties.getPrefixedProperties(prefix, true);
            return objectMapper.valueToTree(propsMapper.readMapAs(props, Map.class));
        } catch (IllegalArgumentException | IOException e) {
            throw new UnexpectedException(e);
        }
    }
}
