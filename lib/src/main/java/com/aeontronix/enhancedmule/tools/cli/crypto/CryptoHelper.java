/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.crypto;

import com.aeontronix.commons.file.FileUtils;
import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.DecryptionException;
import com.aeontronix.kryptotek.EncryptionException;
import com.aeontronix.kryptotek.Key;
import com.aeontronix.kryptotek.key.DecryptionKey;
import com.aeontronix.kryptotek.key.EncryptionKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CryptoHelper {
    public static final Pattern ENCRYPTED_PROP_REGEX = Pattern.compile("^\\{\\{encrypted:(.*)}}$");

    public static String encrypt(Key key, String value, boolean noExpression) throws EncryptionException {
        String eval = CryptoUtils.encrypt((EncryptionKey) key, value);
        if (!noExpression) {
            eval = "{{encrypted:" + eval + "}}";
        }
        return eval;
    }

    public static String decrypt(Key key, String value) throws DecryptionException {
        final Matcher matcher = ENCRYPTED_PROP_REGEX.matcher(value);
        if (matcher.find()) {
            value = matcher.group(1);
        }
        return CryptoUtils.decrypt((DecryptionKey) key, value);
    }

    static String findCryptoKey(String key, File keyFile, ConfigProfile profile) throws IOException {
        if (key != null) {
            return key;
        } else if (keyFile != null) {
            return FileUtils.toString(keyFile);
        }
        return profile.getCryptoKey();
    }

    @NotNull
    public static Set<String> findSensitiveProperties(@NotNull File file) throws IOException {
        final HashSet<String> files = new HashSet<>();
        final String nlc = file.getName().toLowerCase();
        final ObjectMapper objectMapper;
        if (nlc.endsWith(".json")) {
            objectMapper = new ObjectMapper();
        } else {
            objectMapper = new ObjectMapper(new YAMLFactory());
        }
        final JsonNode jsonNode = objectMapper.readTree(file);
        findSensitivePropertiesInPropertyMap(jsonNode, null, files);
        return files;
    }

    private static void findSensitivePropertiesInPropertyMap(JsonNode jsonNode, String path, HashSet<String> files) {
        final Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            final String key = entry.getKey();
            parse(entry.getValue(), path != null ? path + "." + key : key, files);
        }
    }

    private static void parse(JsonNode jsonNode, String path, HashSet<String> files) {
        if (isSecure(jsonNode)) {
            files.add(path);
        } else {
            final JsonNode type = jsonNode.get("type");
            if (type != null && type.isTextual() && type.asText().equalsIgnoreCase("group")) {
                final ObjectNode properties = (ObjectNode) jsonNode.get("properties");
                findSensitivePropertiesInPropertyMap(properties, path, files);
            }
        }
    }

    private static boolean isSecure(JsonNode jsonNode) {
        final JsonNode secure = jsonNode.get("secure");
        if (secure != null && secure.isBoolean()) {
            return secure.booleanValue();
        }
        return false;
    }
}
