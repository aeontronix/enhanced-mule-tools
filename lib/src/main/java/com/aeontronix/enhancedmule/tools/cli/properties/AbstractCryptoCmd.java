/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.cli.properties;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.cli.AbstractCommand;
import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.Key;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

public abstract class AbstractCryptoCmd extends AbstractCommand implements Callable<Integer> {
    @ArgGroup(exclusive = true, multiplicity = "0..1")
    protected EncryptCmd.KeyOptions keyOptions;
    @Option(names = {"-v", "--value"}, description = "Text to encrypt", arity = "1")
    protected String value;
    @Option(names = {"-d", "--descriptor-path"}, description = "Property definition file path", defaultValue = "src/main/resources/properties.yaml", showDefaultValue = ALWAYS)
    protected File descPath;
    @Option(names = {"-p", "--file-path"}, description = "Property file path", defaultValue = "src/main/resources/properties-local.yaml", showDefaultValue = ALWAYS)
    protected File filePath;

    @Override
    public Integer call() throws Exception {
        if (keyOptions == null) {
            keyOptions = new EncryptCmd.KeyOptions();
        }
        final String cryptoKey = CryptoHelper.findCryptoKey(keyOptions.key, keyOptions.keyFile, getCli().getActiveProfile());
        if (StringUtils.isBlank(cryptoKey)) {
            throw new IllegalArgumentException("No cryptography key found in profile or parameters");
        }
        final Key key = CryptoUtils.readKey(cryptoKey);
        run(key, value);
        return null;
    }

    public abstract int run(Key key, String value) throws Exception;
}
