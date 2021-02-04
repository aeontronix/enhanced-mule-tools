/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.descriptor;

import com.aeontronix.enhancedmule.tools.util.DescriptorHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationDescriptorTest {
    @Test
    public void testOverride() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final JsonNode json = objectMapper.valueToTree(ApplicationDescriptor.createDefault());
        ObjectNode ov1 = (ObjectNode) objectMapper.readTree(ApplicationDescriptorTest.class.getResource("/descriptor/merge1.json"));
        DescriptorHelper.override((ObjectNode) json, ov1);
        ObjectNode ov2 = (ObjectNode) objectMapper.readTree(ApplicationDescriptorTest.class.getResource("/descriptor/merge2.json"));
        DescriptorHelper.override((ObjectNode) json, ov2);
        assertEquals(objectMapper.readTree(ApplicationDescriptorTest.class.getResource("/descriptor/merge1-expected.json")),
                json);
    }
}
