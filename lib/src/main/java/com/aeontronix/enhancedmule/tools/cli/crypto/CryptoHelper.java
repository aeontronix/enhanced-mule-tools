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
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.UnexpectedException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CryptoHelper {
    public static final Pattern ENCRYPTED_PROP_REGEX = Pattern.compile("^\\{\\{encrypted:(.*)}}$");

    public static String crypt(Key key, String value, boolean encrypt) throws EncryptionException, DecryptionException {
        if (encrypt) {
            return encrypt(key, value, false);
        } else {
            return decrypt(key, value);
        }
    }

    public static String encrypt(Key key, String value, boolean noExpression) throws EncryptionException {
        if (value.startsWith("{{encrypted:") && value.endsWith("}}")) {
            if (noExpression) {
                return value.substring(12, value.length() - 2);
            } else {
                return value;
            }
        } else {
            String eval = CryptoUtils.encrypt((EncryptionKey) key, value);
            if (!noExpression) {
                eval = "{{encrypted:" + eval + "}}";
            }
            return eval;
        }
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

    public static void encryptProperties(Key key, File propertiesFile) throws IOException, EncryptionException {
        try {
            cryptProperties(key, propertiesFile, true);
        } catch (DecryptionException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    public static void decryptProperties(Key key, File propertiesFile) throws IOException, DecryptionException {
        try {
            cryptProperties(key, propertiesFile, false);
        } catch (EncryptionException e) {
            throw new UnexpectedException(e.getMessage(), e);
        }
    }

    private static void cryptProperties(Key key, File propertiesFile, boolean encrypt) throws IOException, EncryptionException, DecryptionException {
        final Set<String> sensitiveProperties = findSensitiveProperties(propertiesFile);
        if (!sensitiveProperties.isEmpty()) {
            String basePath = propertiesFile.getName();
            final String lc = basePath.toLowerCase();
            if (lc.endsWith(".yaml") || lc.endsWith(".json")) {
                basePath = basePath.substring(0, basePath.length() - 5);
            } else if (lc.endsWith(".yml")) {
                basePath = basePath.substring(0, basePath.length() - 4);
            } else {
                throw new IllegalArgumentException("Invalid descriptor file path, must end in .json, .yaml or .yml");
            }
            final String pattern = "^" + basePath + "-(local|env-.+|envtype-.+).(properties|yaml|yml|json)";
            final File[] files = propertiesFile.getParentFile().listFiles((dir, name) -> name.toLowerCase().matches(pattern));
            for (File file : files) {
                final String fn = file.getName().toLowerCase();
                if (fn.endsWith(".properties")) {
                    Properties props = new Properties();
                    try (FileInputStream fs = new FileInputStream(file)) {
                        props.load(fs);
                    }
                    cryptProperties(key, encrypt, sensitiveProperties, props);
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        props.store(fos, "");
                    }
                } else if (fn.endsWith(".yaml") || fn.endsWith(".yml")) {
                    final Properties props = readYamlProperties(file);
                    cryptProperties(key, encrypt, sensitiveProperties, props);
                }
            }
        }
    }

    public static Properties readYamlProperties(File file) throws IOException {
        final ObjectMapper om = new ObjectMapper(new YAMLFactory());
        return readJacksonProperties(file, om);
    }

    public static Properties readJsonProperties(File file) throws IOException {
        final ObjectMapper om = new ObjectMapper();
        return readJacksonProperties(file, om);
    }

    private static Properties readJacksonProperties(File file, ObjectMapper om) throws IOException {
        final JsonNode propsTree = om.readTree(file);
        final JavaPropsMapper mapper = new JavaPropsMapper();
        return mapper.writeValueAsProperties(propsTree);
    }

    private static void cryptProperties(Key key, boolean encrypt, Set<String> sensitiveProperties, Properties props) throws EncryptionException, DecryptionException {
        for (String propKey : sensitiveProperties) {
            final String value = props.getProperty(propKey);
            if (props.containsKey(propKey)) {
                props.setProperty(propKey, crypt(key, value, encrypt));
            }
        }
    }
}
