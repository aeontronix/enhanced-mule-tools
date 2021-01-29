/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.ApplicationDescriptor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class JsonHelperTest {
    @Test
    public void testVariableReplacement() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode node = objectMapper.readTree(getClass().getResource("/descriptor/descriptor-var-replacement.json"));
        final HashMap<String, String> vars = new HashMap<>();
        vars.put("foo","bar");
        JsonHelper.processVariables((ObjectNode) node,vars);
        final JsonNode expected = objectMapper.readTree(getClass().getResource("/descriptor/descriptor-var-replacement-expected.json"));
        assertEquals(expected,node);
    }
}
