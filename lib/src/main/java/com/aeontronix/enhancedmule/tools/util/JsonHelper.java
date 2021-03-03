/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointObject;
import com.aeontronix.enhancedmule.tools.anypoint.InvalidJsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class JsonHelper implements Serializable {
    private ObjectMapper jsonMapper = createMapper();
    private AnypointClient client;

    public JsonHelper() {
    }

    public JsonHelper(AnypointClient client) {
        this.client = client;
    }

    public static void processVariables(ObjectNode json, HashMap<String, String> vars) {
        LinkedList<JsonNode> nodes = new LinkedList<>();
        nodes.add(json);
        while (!nodes.isEmpty()) {
            final JsonNode node = nodes.removeFirst();
            if (node != null && !node.isNull()) {
                if (node instanceof ObjectNode) {
                    final Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        final JsonNode n = field.getValue();
                        if (n instanceof TextNode) {
                            ((ObjectNode) node).replace(field.getKey(), new TextNode(StringUtils.substituteVariables(n.textValue(), vars)));
                        } else if (n instanceof ArrayNode) {
                            for (JsonNode children : node) {
                                nodes.addLast(children);
                            }
                        } else if (n instanceof ObjectNode) {
                            nodes.addLast(n);
                        }
                    }
                } else if (node instanceof ArrayNode) {
                    final int size = node.size();
                    for (int i = 0; i < size; i++) {
                        JsonNode child = node.get(i);
                        if (child instanceof TextNode) {
                            final TextNode replacement = new TextNode(StringUtils.substituteVariables(child.textValue(), vars));
                            ((ArrayNode) node).remove(i);
                            ((ArrayNode) node).insert(i, replacement);
                        } else {
                            nodes.addLast(child);
                        }
                    }
                    for (JsonNode children : node) {
                        nodes.addLast(children);
                    }
                }
            }
        }
    }

    public static Object getCaseInsensitive(ObjectNode node, String name) {
        final Iterator<String> names = node.fieldNames();
        while (names.hasNext()) {
            String n = names.next();
            if(n.equalsIgnoreCase(name)) {
                return node.get(n);
            }
        }
        return null;
    }

    public AnypointClient getClient() {
        return client;
    }

    public void setClient(AnypointClient client) {
        this.client = client;
    }

    public ObjectMapper getJsonMapper() {
        return jsonMapper;
    }

    public byte[] toJson(Object obj) {
        try (ByteArrayOutputStream tmp = new ByteArrayOutputStream()) {
            jsonMapper.writeValue(tmp, obj);
            return tmp.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> toJsonMap(String json) {
        try {
            return jsonMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> toJsonMap(JsonNode node) {
        try {
            return jsonMapper.treeToValue(node, Map.class);
        } catch (JsonProcessingException e) {
            throw new InvalidJsonException(e);
        }
    }

    public MapBuilder buildJsonMap() {
        return new MapBuilder();
    }

    public MapBuilder buildJsonMap(Map<String, Object> data) {
        return new MapBuilder(null, data);
    }

    public JsonNode readJsonTree(String json) {
        try {
            return jsonMapper.readTree(json);
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    public <X> X readJson(X obj, String json, String jsonPath) {
        try {
            return readJson(obj, jsonMapper.readerForUpdating(obj).readTree(json).at(jsonPath));
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    public <X> X readJson(X obj, String json) {
        return readJson(obj, json, (AnypointObject) null);
    }

    @SuppressWarnings("unchecked")
    public <X> X readJson(X obj, String json, AnypointObject<?> parent) {
        try {
            jsonMapper.readerForUpdating(obj).readValue(json);
            if (obj instanceof AnypointObject) {
                ((AnypointObject) obj).setJson(json);
                if (parent != null) {
                    ((AnypointObject) obj).setParent(parent);
                }
            }
            return obj;
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <X> X readJson(Class<X> objClass, JsonNode node, AnypointObject<?> parent) {
        try {
            Object obj = jsonMapper.treeToValue(node, objClass);
            if (obj instanceof AnypointObject) {
                ((AnypointObject) obj).setJson(node.toString());
                ((AnypointObject) obj).setParent(parent);
            }
            return (X) obj;
        } catch (JsonProcessingException e) {
            throw new InvalidJsonException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <X> X readJson(Class<X> objClass, JsonNode node, AnypointClient client) {
        try {
            Object obj = jsonMapper.treeToValue(node, objClass);
            if (obj instanceof AnypointObject) {
                ((AnypointObject) obj).setJson(node.toString());
                ((AnypointObject) obj).setClient(client);
            }
            return (X) obj;
        } catch (JsonProcessingException e) {
            throw new InvalidJsonException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <X> List<X> readJsonList(Class<X> objClass, String json, AnypointObject<?> parent) {
        return readJsonList(objClass, json, parent, null);
    }

    @SuppressWarnings("unchecked")
    public <X> List<X> readJsonList(Class<X> objClass, String json, AnypointObject<?> parent, String path) {
        try {
            ArrayList<X> list = new ArrayList<>();
            JsonNode node = jsonMapper.readTree(json);
            if (path != null) {
                node = node.at(path);
            }
            for (JsonNode n : node) {
                list.add(readJson(objClass, n, parent));
            }
            return list;
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    public <X> X readJson(X obj, JsonNode node) {
        try {
            jsonMapper.readerForUpdating(obj).readValue(node);
            if (obj instanceof AnypointObject) {
                ((AnypointObject) obj).setJson(node.toString());
                ((AnypointObject) obj).setClient(client);
            }
            return obj;
        } catch (IOException e) {
            throw new InvalidJsonException(e);
        }
    }

    public class MapBuilder {
        private MapBuilder parent;
        private Map<String, Object> request;

        public MapBuilder() {
            request = new HashMap<>();
        }

        public MapBuilder(MapBuilder parent, Map<String, Object> request) {
            this.parent = parent;
            this.request = request;
        }

        public MapBuilder set(String key, Object value) {
            request.put(key, value);
            return this;
        }

        public MapBuilder setNested(String nestKey, String key, Object value) {
            HashMap<String, Object> nestedMap = new HashMap<>();
            nestedMap.put(key, value);
            request.put(nestKey, nestedMap);
            return this;
        }

        public Map<String, Object> toMap() {
            if (parent != null) {
                return parent.toMap();
            } else {
                return request;
            }
        }

        public MapBuilder addMap(String name) {
            HashMap<String, Object> subMap = new HashMap<>();
            request.put(name, subMap);
            return new MapBuilder(this, subMap);
        }
    }

    public static ObjectMapper createMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    public static boolean isNull(JsonNode node) {
        return node == null || node.isNull();
    }

    public static boolean isNotNull(JsonNode node) {
        return !isNull(node);
    }

    public static String getText(JsonNode node, String name) {
        if( isNotNull(node) ) {
            final JsonNode val = node.get(name);
            if( isNotNull(val) ) {
                return val.textValue();
            }
        }
        return null;
    }

}
