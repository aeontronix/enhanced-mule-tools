/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

public class DescriptorHelper {
    public static void override(ObjectNode obj, ObjectNode override) {
        if( override == null ) {
            return;
        }
        final Iterator<Map.Entry<String, JsonNode>> fields = override.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> fieldEntry = fields.next();
            final JsonNode fieldEntryValue = fieldEntry.getValue();
            if( fieldEntryValue instanceof ArrayNode ) {
                final JsonNode existingArray = obj.get(fieldEntry.getKey());
                if (existingArray != null && !existingArray.isNull()) {
                    ((ArrayNode) existingArray).addAll((ArrayNode) fieldEntryValue);
                } else {
                    obj.replace(fieldEntry.getKey(), fieldEntryValue);
                }
            } else if( fieldEntryValue instanceof ObjectNode ) {
                final JsonNode fobj = obj.get(fieldEntry.getKey());
                if(fobj == null || fobj.isNull() ) {
                    obj.replace(fieldEntry.getKey(), fieldEntryValue);
                } else {
                    override((ObjectNode) fobj,(ObjectNode) fieldEntryValue);
                }
            } else if( fieldEntryValue != null && ! fieldEntryValue.isNull()) {
                obj.replace(fieldEntry.getKey(), fieldEntryValue);
            }
        }
    }
}
