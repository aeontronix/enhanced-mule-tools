/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.cli.crypto.CryptoHelper;
import com.aeontronix.kryptotek.Key;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@Mojo(name = "encrypt", requiresProject = false)
public class EncryptPropertiesMojo extends AbstractCryptoMojo {
    private static final Logger logger = getLogger(EncryptPropertiesMojo.class);

    @Override
    protected void execute(Key key, String value) throws Exception {
        logger.info("Encrypted value: " + CryptoHelper.encrypt(key, value, false));
    }
}
