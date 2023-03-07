/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.cli.properties;

import com.aeontronix.commons.exception.UnexpectedException;
import com.aeontronix.commons.file.FileUtils;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.EncodedKey;
import com.aeontronix.kryptotek.InvalidKeyEncodingException;
import com.aeontronix.kryptotek.key.AESKeyLen;
import com.aeontronix.kryptotek.key.RSAKeyPair;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.aeontronix.kryptotek.EncodedKey.Format.B64;

@Command(name = "keygen", description = "Generate a property encryption key")
public class KeyGenCmd implements Callable<Integer> {
    @CommandLine.ParentCommand
    private EMTCli cli;
    @Parameters(description = "File to write key to", arity = "0..1")
    private File file;
    @Option(names = {"-s", "--save"}, description = "Save key to active profile", defaultValue = "false")
    private boolean save;
    @Option(names = {"-r", "--rsa"}, description = "Generate an RSA key pair instead of an AES key", defaultValue = "false")
    private boolean rsa;

    @Override
    public Integer call() throws Exception {
        String key;
        try {
            if (rsa) {
                final RSAKeyPair rsaKeyPair = CryptoUtils.generateRSAKeyPair(4096);
                final EncodedKey encodedKey = rsaKeyPair.getEncoded(B64);
                key = encodedKey.getEncodedKeyString();
                if (file != null) {
                    FileUtils.write(file, encodedKey.getEncodedKeyData());
                } else {
                    System.out.println("Private Key: " + encodedKey.getEncodedKeyString());
                }
                System.out.println("Public key: " + rsaKeyPair.getPublicKey().getEncoded(B64).getEncodedKeyString());
            } else {
                final EncodedKey encodedKey = CryptoUtils.generateAESKey(AESKeyLen.AES256).getEncoded(B64);
                key = encodedKey.getEncodedKeyString();
                if (file != null) {
                    FileUtils.write(file, encodedKey.getEncodedKeyData());
                } else {
                    System.out.println(key);
                }
            }
        } catch (IOException | InvalidKeyEncodingException e) {
            throw new UnexpectedException(e);
        }
        if (save) {
            cli.getActiveProfile().setCryptoKey(key);
            cli.saveConfig();
        }
        return 0;
    }

}
