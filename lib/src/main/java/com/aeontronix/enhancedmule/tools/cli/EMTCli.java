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
import com.aeontronix.enhancedmule.tools.cli.apim.APIManagerCmd;
import com.aeontronix.enhancedmule.tools.cli.config.ActiveProfileCmd;
import com.aeontronix.enhancedmule.tools.cli.config.ConfigCmd;
import com.aeontronix.enhancedmule.tools.cli.crypto.DecryptCmd;
import com.aeontronix.enhancedmule.tools.cli.crypto.EncryptCmd;
import com.aeontronix.enhancedmule.tools.cli.crypto.KeyGenCmd;
import com.aeontronix.enhancedmule.tools.cli.exchange.ExchangeCmd;
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

@Command(name = "emt", subcommands = {
        ConfigCmd.class, KeyGenCmd.class, EncryptCmd.class,
        DecryptCmd.class, ActiveProfileCmd.class, LoginCmd.class, UserInfoCmd.class, APIManagerCmd.class,
        ExchangeCmd.class
}, versionProvider = VersionHelper.class, mixinStandardHelpOptions = true)
public class EMTCli extends AbstractCommand {
    @Option(names = {"--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;
    @Option(names = {"-d", "--debug"}, description = "Enable debug")
    private boolean debug;
    @Option(names = "-p", description = "Profile")
    private String profileName;
    private File workDir = new File(".");
    private LineReader reader;
    @ArgGroup(exclusive = false, multiplicity = "0..1")
    private CredentialsArgs credentialsArgs;
    private EMConfig config;
    private ConfigProfile activeProfile;

    public EMTCli() throws IOException, ProfileNotFoundException {
        config = EMConfig.findConfigFile();
        if (profileName != null) {
            activeProfile = config.getProfileByProfileName(profileName);
        } else {
            activeProfile = config.getProfile(null, null, null);
        }
    }

    @Override
    public EMTCli getCli() {
        return this;
    }

    public boolean isDebug() {
        return debug;
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

    @NotNull
    public ConfigProfile getActiveProfile() throws IOException, ProfileNotFoundException {
        return activeProfile;
    }

    @NotNull
    public String getActiveProfileId() throws IOException, ProfileNotFoundException {
        return profileName != null ? profileName : config.getActive();
    }

    public void saveConfig() throws IOException {
        config.save();
    }

    public Environment getEnvironment(String organizationName, String environmentName) throws IOException, ProfileNotFoundException, NotFoundException {
        if (organizationName == null) {
            organizationName = getActiveProfile().getDefaultOrg();
        }
        if (environmentName == null) {
            environmentName = getActiveProfile().getDefaultEnv();
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
            credentials = getActiveProfile().getCredentials();
        }
        if (credentials == null) {
            throw new IllegalArgumentException("No credentials available");
        }
        final EnhancedMuleClient enhancedMuleClient = new EnhancedMuleClient(getActiveProfile());
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
