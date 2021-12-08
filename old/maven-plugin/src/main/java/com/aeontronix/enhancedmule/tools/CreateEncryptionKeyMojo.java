/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.EncodedKey;
import com.aeontronix.kryptotek.InvalidKeyEncodingException;
import com.aeontronix.kryptotek.key.AESKeyLen;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Mojo(name = "create-encryption-key", requiresProject = false)
public class CreateEncryptionKeyMojo extends AbstractMojo {
    @Parameter(property = "file")
    private File file;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final String key = CryptoUtils.generateAESKey(AESKeyLen.AES192).getEncoded(EncodedKey.Format.JSON).getEncodedKeyString();
            if (file != null) {
                try (final FileWriter fw = new FileWriter(file)) {
                    fw.write(key);
                }
            } else {
                if( getLog().isInfoEnabled() ) {
                    getLog().info("Mule Encryption Key: "+key);
                } else {
                    System.out.println(key);
                }
            }
        } catch (InvalidKeyEncodingException | IOException e) {
            throw new MojoExecutionException(e.getMessage(),e);
        }
    }
}
