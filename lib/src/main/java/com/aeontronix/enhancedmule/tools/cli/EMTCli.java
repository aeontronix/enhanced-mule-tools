/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.config.ConfigFile;
import com.aeontronix.enhancedmule.config.ConfigProfile;
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
    private String activeProfile;
    private ConfigProfile configProfile;
    private File workDir = new File(".");
    private LineReader reader;
    private ConfigFile configFile;

    public EMTCli() {
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
        return getProfile(false);
    }

    public ConfigProfile getProfile(boolean create) throws IOException, ProfileNotFoundException {
        return getProfile(create, null, null);
    }

    public ConfigProfile getProfile(String org, String groupId) throws IOException, ProfileNotFoundException {
        return getProfile(false, org, groupId);
    }

    public ConfigProfile getProfile(boolean create, String org, String groupId) throws IOException, ProfileNotFoundException {
        if (configFile == null) {
            configFile = ConfigFile.findConfigFile();
            if (configFile == null) {
                configFile = ConfigFile.createNew();
            }
        }
        final String p = activeProfile != null ? activeProfile : profileName;
        try {
            return configFile.getProfile(p, org, groupId);
        } catch (ProfileNotFoundException e) {
            if( create ) {
                final ConfigProfile configProfile = new ConfigProfile();
                configFile.getProfiles().put(p,configProfile);
                return configProfile;
            } else {
                throw e;
            }
        }
    }

    public void saveConfig() throws IOException {
        if (configFile == null) {
            configFile = ConfigFile.createNew();
        }
        configFile.save();
    }

    public String getActiveProfile() {
        return activeProfile != null ? activeProfile : "default";
    }

    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }
}
