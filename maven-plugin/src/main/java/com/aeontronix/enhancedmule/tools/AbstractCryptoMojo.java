/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.EMConfig;
import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.Key;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

public abstract class AbstractCryptoMojo extends AbstractMojo {
    @Parameter(property = "anypoint.descriptor")
    private String descriptor;
    @Parameter(property = "key")
    private String keyStr;
    @Parameter(property = "org")
    private String org;
    @Parameter(property = "profile")
    private String profile;
    @Parameter(property = "value")
    private String value;
    private File anypointFile;
    protected ObjectMapper objectMapper;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (keyStr == null) {
                final EMConfig configFile = EMConfig.findConfigFile();
                final ConfigProfile configProfile = configFile.getProfileByProfileName(profile);
                keyStr = configProfile.getCryptoKey();
                if (keyStr == null) {
                    throw new MojoExecutionException("Key must be set or exist in configuration profile");
                }
            }
            final Key key = CryptoUtils.readKey(keyStr);
            execute(key, value);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected abstract void execute(Key key, String value) throws Exception;
}
