/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.maven;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.EMConfig;
import com.aeontronix.enhancedmule.tools.cli.LoginCmd;
import com.aeontronix.enhancedmule.tools.client.EMTClient;
import com.aeontronix.enhancedmule.tools.client.LoginHelper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;

import java.io.File;
import java.net.URI;

import static java.io.File.separator;
import static org.slf4j.LoggerFactory.getLogger;

@Mojo(name = "login", requiresProject = false)
public class LoginMojo extends AbstractMojo {
    private static final Logger logger = getLogger(LoginCmd.class);
    @Parameter(property = "emt.server.url")
    private URI serverUrl;
    @Parameter(property = "emt.maven.settings.file")
    private File mvnSettingsFile;
    @Parameter(property = "emt.maven.settings")
    private String mavenSettings;
    @Parameter(property = "emt.profile")
    private String profile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (mvnSettingsFile == null) {
                mvnSettingsFile = new File(System.getProperty("user.home") + separator + ".m2" + separator + "settings.xml");
            }
            EMConfig configFile = EMConfig.findConfigFile();
            final ConfigProfile configProfile = configFile.getOrCreateProfile(profile);
            configProfile.setServerUrl(serverUrl);
            final EMTClient emtClient = new EMTClient(configProfile);
            LoginHelper.login(mvnSettingsFile, mavenSettings, emtClient, configProfile);
            configFile.save();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
