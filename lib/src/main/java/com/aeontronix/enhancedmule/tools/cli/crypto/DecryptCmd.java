/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.crypto;

import com.aeontronix.kryptotek.Key;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;

@CommandLine.Command(name = "decrypt", description = "Decrypt properties")
public class DecryptCmd extends AbstractCryptoCmd {
    @Override
    public int run(Key key, String value) throws Exception {
        if (value != null) {
            try {
                System.out.println(CryptoHelper.decrypt(key, value));
            } catch (ClassCastException e) {
                if (e.getMessage().contains("PublicKey")) {
                    throw new IllegalArgumentException("Public key can only be used for encryption, decryption not allowed (private key required for that)");
                } else {
                    throw e;
                }
            }
        } else {
            CryptoHelper.decryptProperties(key, descPath, filePath);
        }
        return 0;
    }

    static class KeyOptions {
        @Option(names = {"-s", "--key-string"}, description = "Encryption key as text", required = true)
        String key;
        @Option(names = {"-f", "--key-file"}, description = "Encryption key file", required = true)
        File keyFile;
    }
}
