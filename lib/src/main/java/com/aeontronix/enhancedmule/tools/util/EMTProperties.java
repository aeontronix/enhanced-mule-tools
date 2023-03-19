/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;

import java.util.HashMap;
import java.util.Map;

public class EMTProperties {
    public static final String EMT_ENV = "emt.env";
    public static final String EMT_ENVTYPE = "emt.envtype";
    private final Map<String, String> properties = new HashMap<>();

    public EMTProperties(Map<String, String> props) {
        this(props, null, null, null);
    }

    public EMTProperties(Map<String, String> props, String envId, String envName, Environment.Type envType) {
        Map<String, String> envNameOv = new HashMap<>();
        Map<String, String> envIdOv = new HashMap<>();
        Map<String, String> envTypeOv = new HashMap<>();
        for (Map.Entry<String, String> e : props.entrySet()) {
            String key = e.getKey();
            String lkey = e.getKey().toLowerCase();
            String val = e.getValue();
            if (lkey.startsWith(EMT_ENV) || lkey.startsWith(EMT_ENVTYPE)) {
                addIfMatch(key, val, EMT_ENVTYPE, envType != null ? envType.name() : null, envTypeOv);
                addIfMatch(key, val, EMT_ENV, envName, envNameOv);
                addIfMatch(key, val, EMT_ENV, envId, envIdOv);
            } else {
                properties.put(key, val);
            }
        }
        properties.putAll(envTypeOv);
        properties.putAll(envNameOv);
        properties.putAll(envIdOv);
    }

    private static void addIfMatch(String key, String val, String prefix, String txt, Map<String, String> props) {
        if (txt != null) {
            String fullPrefix = prefix + "." + txt.toLowerCase().replace(" ", "_") + ".";
            if (key.toLowerCase().startsWith(fullPrefix)) {
                props.put("emt." + key.substring(fullPrefix.length()), val);
            }
        }
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getProperty(String key, String defValue) {
        return properties.getOrDefault(key, defValue);
    }

    public Map<String, String> getPrefixedProperties(String prefix, boolean stripPrefix) {
        HashMap<String, String> results = new HashMap<>();
        for (Map.Entry<String, String> e : properties.entrySet()) {
            String key = e.getKey();
            if (key.toLowerCase().startsWith(prefix.toLowerCase())) {
                results.put(stripPrefix ? key.substring(prefix.length()) : key, e.getValue());
            }
        }
        return results;
    }
}
