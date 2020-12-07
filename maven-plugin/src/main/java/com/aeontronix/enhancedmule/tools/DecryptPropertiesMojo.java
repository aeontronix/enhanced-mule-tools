/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.DecryptionException;
import com.aeontronix.kryptotek.key.AESKey;
import com.aeontronix.kryptotek.key.EncryptionKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "props-decrypt", requiresProject = false)
public class DecryptPropertiesMojo extends AbstractCryptoMojo {
    @Override
    protected JsonNode processValue(AESKey key, JsonNode value) throws DecryptionException {
        if (value instanceof ObjectNode) {
            final JsonNode encrypted = value.get("encrypted");
            final JsonNode encryptedValue = value.get("value");
            if (encrypted != null && encryptedValue != null && encrypted.booleanValue()) {
                final byte[] encryptedData = StringUtils.base64Decode(encryptedValue.textValue());
                final String decryptedData = StringUtils.utf8(CryptoUtils.decrypt(key, encryptedData));
                return new TextNode(decryptedData);
            }
        }
        return null;
    }

    @Override
    protected boolean isProcessingRequired(JsonNode property) {
        return true;
    }
}
