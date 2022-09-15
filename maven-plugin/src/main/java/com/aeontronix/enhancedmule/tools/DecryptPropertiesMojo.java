/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.cli.crypto.CryptoHelper;
import com.aeontronix.kryptotek.Key;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@Mojo(name = "decrypt", requiresProject = false)
public class DecryptPropertiesMojo extends AbstractCryptoMojo {
    private static final Logger logger = getLogger(DecryptPropertiesMojo.class);
    @Override
    protected void execute(Key key, String value) throws Exception {
        logger.info("Decrypted Value: " + CryptoHelper.decrypt(key, value));
    }
}
