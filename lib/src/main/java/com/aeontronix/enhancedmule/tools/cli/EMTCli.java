/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.EMConfig;
import com.aeontronix.enhancedmule.config.ProfileNotFoundException;
import com.aeontronix.enhancedmule.tools.cli.application.ApplicationCmd;
import com.aeontronix.enhancedmule.tools.cli.config.ConfigCmd;
import com.aeontronix.enhancedmule.tools.util.VersionHelper;
import org.jline.reader.LineReader;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;

@Command(name = "emt", mixinStandardHelpOptions = true, versionProvider = VersionHelper.class, subcommands = {ApplicationCmd.class, ConfigCmd.class})
public class EMTCli {
    @CommandLine.Option(names = "-p", description = "Profile")
    private String profileName;
    private ConfigProfile configProfile;
    private File workDir = new File(".");
    private LineReader reader;
    private EMConfig config;

    public EMTCli() throws IOException, ProfileNotFoundException {
        config = EMConfig.findConfigFile();
        config.setActive(profileName);
    }

    public EMConfig getConfig() {
        return config;
    }

    public boolean isShell() {
        return reader != null;
    }

    public LineReader getReader() {
        return reader;
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public void setReader(LineReader reader) {
        this.reader = reader;
    }

    public String getProfileName() {
        return profileName != null ? profileName : "default";
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public ConfigProfile getProfile() throws IOException, ProfileNotFoundException {
        return getProfile(null, null);
    }

    public ConfigProfile getProfile(String org, String groupId) throws IOException, ProfileNotFoundException {
        return config.getProfile(null,org,groupId);
    }

    public void saveConfig() throws IOException {
        config.save();
    }
}
