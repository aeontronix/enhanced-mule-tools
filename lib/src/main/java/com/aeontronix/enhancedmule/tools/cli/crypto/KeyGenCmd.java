/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.crypto;

import com.aeontronix.commons.exception.UnexpectedException;
import com.aeontronix.commons.file.FileUtils;
import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.EncodedKey;
import com.aeontronix.kryptotek.InvalidKeyEncodingException;
import com.aeontronix.kryptotek.key.AESKeyLen;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.aeontronix.kryptotek.EncodedKey.Format.B64;

@Command(name = "keygen", description = "Generate a property encryption key")
public class KeyGenCmd implements Callable<Integer> {
    @Parameters(description = "File to write key to", arity = "0..1")
    private File file;

    @Override
    public Integer call() throws Exception {
        final String key = genKey(file);
        if (key != null) {
            System.out.println(key);
        }
        return 0;
    }

    public static String genKey(File file) {
        try {
            final EncodedKey encodedKey = CryptoUtils.generateAESKey(AESKeyLen.AES256).getEncoded(B64);
            if (file != null) {
                FileUtils.write(file, encodedKey.getEncodedKeyData());
                return null;
            } else {
                return encodedKey.getEncodedKeyString();
            }
        } catch (IOException | InvalidKeyEncodingException e) {
            throw new UnexpectedException(e);
        }
    }
}
