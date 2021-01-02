/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.util;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtils {
    public static boolean isNull(JsonNode node) {
        return node == null || node.isNull();
    }

    public static boolean isNotNull(JsonNode node) {
        return ! isNull(node);
    }
}
