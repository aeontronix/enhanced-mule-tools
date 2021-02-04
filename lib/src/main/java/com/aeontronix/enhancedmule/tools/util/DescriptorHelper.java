/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.ApplicationDescriptor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

public class DescriptorHelper {
    public static void override(ObjectNode obj, ObjectNode override) {
        final Iterator<Map.Entry<String, JsonNode>> fields = override.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> fieldEntry = fields.next();
            if( fieldEntry.getValue() instanceof ArrayNode ) {
                final JsonNode existingArray = obj.get(fieldEntry.getKey());
                if (existingArray != null && !existingArray.isNull()) {
                    ((ArrayNode) existingArray).addAll((ArrayNode) fieldEntry.getValue());
                } else {
                    obj.replace(fieldEntry.getKey(), fieldEntry.getValue());
                }
            } else if( fieldEntry.getValue() instanceof ObjectNode ) {
                final JsonNode fobj = obj.get(fieldEntry.getKey());
                if(fobj == null || fobj.isNull() ) {
                    obj.replace(fieldEntry.getKey(),fieldEntry.getValue());
                } else {
                    override((ObjectNode) fobj,(ObjectNode) fieldEntry.getValue());
                }
            } else {
                obj.replace(fieldEntry.getKey(),fieldEntry.getValue());
            }
        }
    }
}
