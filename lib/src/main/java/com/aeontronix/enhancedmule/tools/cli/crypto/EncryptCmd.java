/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.crypto;

import com.aeontronix.kryptotek.Key;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;

@Command(name = "encrypt", description = "Encrypt properties")
public class EncryptCmd extends AbstractCryptoCmd {
    @Option(names = {"-n", "--no-expression"}, description = "if this flag is used, only the encrypted value will be returned rather than the whole property expression", arity = "1")
    private boolean noExpression;

    @Override
    public int run(Key key, String value) throws Exception {
        if (value != null) {
            System.out.println(CryptoHelper.encrypt(key, value, noExpression));
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
