/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class JacksonHelper {
    public static JsonNode readTree(File file) throws IOException {
        String fname = file.getName().toLowerCase();
        if (fname.endsWith(".json")) {
            return new ObjectMapper().readTree(file);
        } else if (fname.endsWith(".yaml") || fname.endsWith(".yml")) {
            return new ObjectMapper(new YAMLFactory()).readTree(file);
        } else if (fname.endsWith(".properties")) {
            return new JavaPropsMapper().readTree(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + file);
        }
    }
}
