/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.maven;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.EMConfig;
import com.aeontronix.enhancedmule.tools.cli.LoginCmd;
import com.aeontronix.enhancedmule.tools.utils.ConfigHelper;
import com.aeontronix.enhancedmule.tools.utils.MavenSettingsUpdater;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;

import java.io.File;
import java.net.URI;

import static org.slf4j.LoggerFactory.getLogger;

@Mojo(name = "config", requiresProject = false)
public class ConfigMojo extends AbstractEMTMojo {
    private static final Logger logger = getLogger(LoginCmd.class);
    @Parameter(property = "emt.server.url")
    private URI serverUrl;
    @Parameter(property = "emt.maven.settings.file")
    private File mvnSettingsFile;
    @Parameter(property = "emt.maven.settings.file")
    private String mvnSettingsId;

    @Override
    protected void execute(EMConfig configFile, ConfigProfile profile) throws MojoExecutionException {
        try {
            ConfigHelper.updateConfig(configFile, profile, serverUrl, mvnSettingsFile, mvnSettingsId);
            logger.info("Settings updated");
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
