/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.maven;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.EMConfig;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;

public abstract class AbstractEMTMojo extends AbstractMojo {
    @Parameter(property = "emt.profile")
    private String profile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            EMConfig configFile = EMConfig.findConfigFile();
            execute( configFile, configFile.getOrCreateProfile(profile) );
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(),e);
        }
    }

    protected abstract void execute( EMConfig configFile, ConfigProfile profile) throws MojoExecutionException;
}
