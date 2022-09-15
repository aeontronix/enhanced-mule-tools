/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.crypto;

import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.DecryptionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class CryptoHelperTest {
    public static final String KEY = "aes:dKYWnOOcU8BpjyfSMs1zL8fGYiwPn4zpRSWs7wxN96k";

    @Test
    public void decryptPlain() throws InvalidKeyException, DecryptionException {
        final String encryptedValue = "{{encrypted:AJLhk8VNA8atOvlUD8EZmd3wzwOF1FkLpw0CurYx3dM}}";
        final String decrypt = CryptoHelper.decrypt(CryptoUtils.readKey(KEY), encryptedValue);
        Assertions.assertEquals("HelloWorld", decrypt);
    }

    @Test
    public void decryptExpr() throws InvalidKeyException, DecryptionException {
        final String encryptedValue = "AJLhk8VNA8atOvlUD8EZmd3wzwOF1FkLpw0CurYx3dM";
        final String decrypt = CryptoHelper.decrypt(CryptoUtils.readKey(KEY), encryptedValue);
        Assertions.assertEquals("HelloWorld", decrypt);
    }

    @Test
    public void testFindingSensitiveProperties() throws URISyntaxException, IOException {
        final Set<String> sensitiveProperties = CryptoHelper.findSensitiveProperties(new File(Objects.requireNonNull(
                getClass().getResource("/crypto/application.yaml")).toURI()));
        Assertions.assertEquals(new HashSet<>(Arrays.asList("foo.bla", "bla.ble", "foo.ra.xxx")), sensitiveProperties);
    }
}
