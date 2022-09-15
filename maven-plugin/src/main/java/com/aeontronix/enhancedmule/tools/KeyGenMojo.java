/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

import static com.aeontronix.enhancedmule.tools.cli.crypto.KeyGenCmd.genKey;

@Mojo(name = "keygen", requiresProject = false)
public class KeyGenMojo extends AbstractMojo {
    @Parameter(property = "file")
    private File file;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String key = genKey(file);
        if (key != null) {
            if (getLog().isInfoEnabled()) {
                getLog().info("Mule Encryption Key: " + key);
            } else {
                System.out.println(key);
            }
        }
    }
}
