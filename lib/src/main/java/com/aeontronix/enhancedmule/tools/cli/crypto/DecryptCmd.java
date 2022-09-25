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
            System.out.println(CryptoHelper.decrypt(key, value));
        } else {
            CryptoHelper.encryptProperties(key, path);
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
