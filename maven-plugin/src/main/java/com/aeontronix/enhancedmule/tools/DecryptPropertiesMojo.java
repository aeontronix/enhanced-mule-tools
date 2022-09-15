/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.cli.crypto.CryptoHelper;
import com.aeontronix.kryptotek.Key;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "decrypt", requiresProject = false)
public class DecryptPropertiesMojo extends AbstractCryptoMojo {
    @Override
    protected void execute(Key key, String value) throws Exception {
        System.out.println(CryptoHelper.decrypt(key, value));
    }
}
