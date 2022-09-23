/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.crypto;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.Key;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.util.concurrent.Callable;

public abstract class AbstractCryptoCmd implements Callable<Integer> {
    @ParentCommand
    private EMTCli cli;
    @ArgGroup(exclusive = true, multiplicity = "0..1")
    private EncryptCmd.KeyOptions keyOptions;
    @Option(names = {"-v", "--value"}, description = "Text to encrypt", arity = "1")
    private String value;
    //    @Option(names = {"-p", "--path"}, description = "Property definition file path", defaultValue = "src/main/resources/properties.yaml", showDefaultValue = ALWAYS)
    private File path;

    @Override
    public Integer call() throws Exception {
        if (keyOptions == null) {
            keyOptions = new EncryptCmd.KeyOptions();
        }
        final String cryptoKey = CryptoHelper.findCryptoKey(keyOptions.key, keyOptions.keyFile, cli.getProfile());
        if (StringUtils.isBlank(cryptoKey)) {
            throw new IllegalArgumentException("No cryptography key found in profile or parameters");
        }
        final Key key = CryptoUtils.readKey(cryptoKey);
        run(key, value);
        return null;
    }

    public abstract int run(Key key, String value) throws Exception;

}