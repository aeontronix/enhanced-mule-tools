/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.util.DescriptorHelper;
import com.aeontronix.enhancedmule.tools.util.EMTProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class DescriptorLayersTest {
    @Test
    public void testOverrides() throws IOException {
        ObjectMapper om = new ObjectMapper();
        HashMap<String, String> properties = new HashMap<>();
        properties.put("emt.defaults.def1", "mydef");
        properties.put("emt.overrides.ov1", "newov");
        properties.put("emt.overrides.foo", "raven");
        DescriptorLayers layers = new DescriptorLayers(new EMTProperties(properties, null, null, null));
        ObjectNode tree = om.createObjectNode();
        DescriptorHelper.override(tree, layers.getDefaults());
        DescriptorHelper.override(tree, (ObjectNode) om.readTree(DescriptorLayersTest.class.getResource("/layers.json")));
        DescriptorHelper.override(tree, layers.getOverrides());
        System.out.println(tree);
        Map<String, String> treeAsMap = new HashMap<>(om.treeToValue(tree, Map.class));
        HashMap<String, String> expected = new HashMap<>();
        expected.put("def1", "mydef");
        expected.put("foo", "raven");
        expected.put("color", "blue");
        expected.put("plane", "jet");
        expected.put("ov1", "newov");
        Assertions.assertEquals(new HashMap(expected), treeAsMap);
    }
}
