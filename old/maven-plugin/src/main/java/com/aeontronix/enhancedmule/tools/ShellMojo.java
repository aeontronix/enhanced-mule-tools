/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import com.aeontronix.enhancedmule.tools.cli.ShellCmd;
import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.EncryptionException;
import com.aeontronix.kryptotek.key.AESKey;
import com.aeontronix.kryptotek.key.EncryptionKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "shell", requiresProject = false)
public class ShellMojo extends AbstractMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            new ShellCmd().call();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(),e);
        }
    }
}
