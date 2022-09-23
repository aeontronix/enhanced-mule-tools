/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.crypto;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.exception.UnexpectedException;
import com.aeontronix.commons.file.FileUtils;
import com.aeontronix.commons.file.TempDir;
import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.DecryptionException;
import com.aeontronix.kryptotek.EncryptionException;
import com.aeontronix.kryptotek.Key;
import com.aeontronix.kryptotek.key.DecryptionKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.*;

class CryptoHelperTest {
    public static final String KEY_STR = "aes:dKYWnOOcU8BpjyfSMs1zL8fGYiwPn4zpRSWs7wxN96k";
    private File propertiesFile;
    private Key key;

    public CryptoHelperTest() throws URISyntaxException, InvalidKeyException {
        propertiesFile = new File(Objects.requireNonNull(getClass().getResource("/crypto/properties.yaml")).toURI());
        key = CryptoUtils.readKey(KEY_STR);
    }

    @Test
    public void decryptPlain() throws DecryptionException {
        final String encryptedValue = "{{encrypted:AJLhk8VNA8atOvlUD8EZmd3wzwOF1FkLpw0CurYx3dM}}";
        final String decrypt = CryptoHelper.decrypt(key, encryptedValue);
        Assertions.assertEquals("HelloWorld", decrypt);
    }

    @Test
    public void decryptExpr() throws InvalidKeyException, DecryptionException {
        final String encryptedValue = "AJLhk8VNA8atOvlUD8EZmd3wzwOF1FkLpw0CurYx3dM";
        final String decrypt = CryptoHelper.decrypt(CryptoUtils.readKey(KEY_STR), encryptedValue);
        Assertions.assertEquals("HelloWorld", decrypt);
    }

    @Test
    public void testFindingSensitiveProperties() throws URISyntaxException, IOException {
        final Set<String> sensitiveProperties = CryptoHelper.findSensitiveProperties(propertiesFile);
        Assertions.assertEquals(new HashSet<>(Arrays.asList("foo.bla", "bla.ble", "foo.ra.xxx")), sensitiveProperties);
    }

    @Test
    public void testEncryptProperties() throws IOException, EncryptionException, DecryptionException {
        try (TempDir tempDir = new TempDir("tmp")) {
            FileUtils.copyDirectory(propertiesFile.getParentFile(), tempDir);
            CryptoHelper.encryptProperties(key, propertiesFile);
            validateEncryptedProperties(loadProperties("local.properties"));
            validateEncryptedProperties(loadProperties("env-dev.yaml"));
        }
    }

    private void validateEncryptedProperties(Map<String, String> props) throws DecryptionException {
        assertProperty(props, "foo.bla", "hello", true);
        assertProperty(props, "bla.ble", "plane", true);
        assertProperty(props, "foo.ra.xxx", "bird", true);
        assertProperty(props, "foo.foo", "train", false);
    }

    private void assertProperty(Map<String, String> props, String pkey, String expected, boolean encrypted) throws DecryptionException {
        final String val = props.get(pkey);
        if (encrypted) {
            Assertions.assertTrue(val.startsWith("{{encrypted:"));
            Assertions.assertTrue(val.endsWith("}}"));
            Assertions.assertEquals(expected, new String(CryptoUtils.decrypt((DecryptionKey) key,
                    StringUtils.base64Decode(val.substring(12, val.length() - 2))), StandardCharsets.UTF_8));
        } else {
            Assertions.assertEquals(expected, val);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, String> loadProperties(String ext) throws IOException {
        final File file = new File(propertiesFile.getParentFile(), "properties-" + ext);
        if (ext.endsWith(".properties")) {
            final Properties properties = new Properties();
            try (FileInputStream is = new FileInputStream(file)) {
                properties.load(is);
            }
            return new HashMap<String, String>((Map) properties);
        } else if (ext.endsWith(".yaml")) {
//            new ObjectMapper(new YAMLFactory()).readTree()
        }
        throw new UnexpectedException("invalid ext:" + ext);
    }
}
