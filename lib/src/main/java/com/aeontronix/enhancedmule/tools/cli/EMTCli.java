/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.config.ConfigCredentials;
import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.EMConfig;
import com.aeontronix.enhancedmule.config.ProfileNotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.cli.application.ApplicationCmd;
import com.aeontronix.enhancedmule.tools.cli.config.ActiveProfileCmd;
import com.aeontronix.enhancedmule.tools.cli.config.ConfigCmd;
import com.aeontronix.enhancedmule.tools.cli.crypto.DecryptCmd;
import com.aeontronix.enhancedmule.tools.cli.crypto.EncryptCmd;
import com.aeontronix.enhancedmule.tools.cli.crypto.KeyGenCmd;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import com.aeontronix.enhancedmule.tools.util.CredentialsConverter;
import com.aeontronix.enhancedmule.tools.util.VersionHelper;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.LineReader;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;

import static picocli.CommandLine.ArgGroup;
import static picocli.CommandLine.Option;

@Command(name = "emt", subcommands = {ApplicationCmd.class, ConfigCmd.class, KeyGenCmd.class, EncryptCmd.class,
        DecryptCmd.class, ActiveProfileCmd.class},
        versionProvider = VersionHelper.class, mixinStandardHelpOptions = true)
public class EMTCli {
    @Option(names = {"--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;
    @Option(names = "-p", description = "Profile")
    private String profileName;
    private File workDir = new File(".");
    private LineReader reader;
    @ArgGroup(exclusive = false, multiplicity = "0..1")
    private CredentialsArgs credentialsArgs;
    private EMConfig config;

    public EMTCli() throws IOException, ProfileNotFoundException {
        config = EMConfig.findConfigFile();
        config.checkProfileExists(profileName);
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

    @NotNull
    public ConfigProfile getProfile() throws IOException, ProfileNotFoundException {
        return getProfile(null, null);
    }

    public ConfigProfile getProfile(String org, String groupId) throws IOException, ProfileNotFoundException {
        return config.getProfile(null, org, groupId);
    }

    public void saveConfig() throws IOException {
        config.save();
    }

    public Environment getEnvironment(String organizationName, String environmentName) throws IOException, ProfileNotFoundException, NotFoundException {
        if (organizationName == null) {
            organizationName = getProfile().getDefaultOrg();
        }
        if (environmentName == null) {
            environmentName = getProfile().getDefaultEnv();
        }
        if (organizationName == null) {
            throw new IllegalArgumentException("Organization not set and no default is assigned in profile");
        }
        if (environmentName == null) {
            throw new IllegalArgumentException("Environment not set and no default is assigned in profile");
        }
        final EnhancedMuleClient client = getClient(organizationName, environmentName);
        client.getAnypointClient().findEnvironment(organizationName, environmentName, false, false, null);
        return null;
    }

    public EnhancedMuleClient getClient() throws IOException, ProfileNotFoundException {
        return getClient(null, null);
    }

    public EnhancedMuleClient getClient(String organizationName, String environmentName) throws IOException, ProfileNotFoundException {
        ConfigCredentials credentials;
        credentials = credentialsArgs != null ? credentialsArgs.getCredentials() : null;
        if (credentials == null) {
            credentials = getProfile(organizationName, environmentName).getCredentials();
        }
        if (credentials == null) {
            throw new IllegalArgumentException("No credentials available");
        }
        final EnhancedMuleClient enhancedMuleClient = new EnhancedMuleClient(getProfile());
        enhancedMuleClient.setCredentialsLoader(CredentialsConverter.convert(credentials));
        return enhancedMuleClient;
    }

    public Organization findOrganization(String organization) throws IOException, ProfileNotFoundException, NotFoundException {
        if (organization != null) {
            return getClient(organization, null).getAnypointClient().findOrganizationByNameOrId(organization);
        } else {
            final AnypointClient anypointClient = getClient().getAnypointClient();
            final Organization org = anypointClient.getUser().getOrganization();
            org.setClient(anypointClient);
            return org;
        }
    }

}
