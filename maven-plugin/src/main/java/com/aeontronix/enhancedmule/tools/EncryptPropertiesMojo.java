/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.EncryptionException;
import com.aeontronix.kryptotek.key.AESKey;
import com.aeontronix.kryptotek.key.EncryptionKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "props-encrypt", requiresProject = false)
public class EncryptPropertiesMojo extends AbstractCryptoMojo {
    @Override
    protected JsonNode processValue(AESKey key, JsonNode value) throws Exception {
        if (value.isObject()) {
            final JsonNode encrypted = value.get("encrypted");
            if (encrypted == null || !encrypted.booleanValue()) {
                value = value.get("value");
                if (value != null) {
                    ((ObjectNode) value).set("value", new TextNode(encrypt(key, value.textValue())));
                }
            }
            return value;
        } else if (value.isTextual()) {
            final ObjectNode v = objectMapper.createObjectNode();
            v.set("encrypted",BooleanNode.TRUE);
            v.set("value",new TextNode(encrypt(key, value.textValue())));
            return v;
        } else {
            return null;
        }
    }

    private String encrypt(EncryptionKey key, String value) throws EncryptionException {
        return StringUtils.base64Encode(CryptoUtils.encrypt(key, StringUtils.utf8(value)));
    }

    @Override
    protected boolean isProcessingRequired(JsonNode property) {
        final JsonNode secure = property.get("secure");
        return secure != null && secure.booleanValue();
    }
}
