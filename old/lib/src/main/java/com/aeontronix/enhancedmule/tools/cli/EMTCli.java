/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.config.*;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.cli.application.ApplicationCmd;
import com.aeontronix.enhancedmule.tools.cli.config.ConfigCmd;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import com.aeontronix.enhancedmule.tools.util.VersionHelper;
import org.jline.reader.LineReader;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;

import static picocli.CommandLine.ArgGroup;
import static picocli.CommandLine.Option;

@Command(name = "emt", subcommands = {ApplicationCmd.class, ConfigCmd.class, LoginCmd.class}, versionProvider = VersionHelper.class)
public class EMTCli {
    @Option(names = {"--version"}, versionHelp = true, description = "display version info")
    boolean versionInfoRequested;
    @Option(names = {"?", "-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;
    @Option(names = "-p", description = "Profile")
    private String profileName;
    private File workDir = new File(".");
    private LineReader reader;
    @ArgGroup(exclusive = false)
    private CredentialsArgs credentialsArgs;
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
        return getClient(null,null);
    }

    public EnhancedMuleClient getClient(String organizationName, String environmentName) throws IOException, ProfileNotFoundException {
//        Credential credential;
//        if (credentialsArgs != null) {
//            if (credentialsArgs.type == CredentialType.REFRESH) {
//                throw new IllegalArgumentException("Refresh credentials can only be used in configuration profile");
//            }
//            credential = new Credential(credentialsArgs.id, credentialsArgs.secret, credentialsArgs.type);
//        } else {
//            credential = getProfile(organizationName, environmentName).getCredential();
//        }
//        if (credential == null) {
//            throw new IllegalArgumentException("No credentials available");
//        }
//        final EnhancedMuleClient enhancedMuleClient = new EnhancedMuleClient(getProfile());
//        if (credential.getType() == CredentialType.PASSWORD) {
//            enhancedMuleClient.setCredentialsLoader(new CredentialsProviderAnypointUsernamePasswordImpl(credential.getId(), credential.getSecret()));
//        } else if (credential.getType() == CredentialType.ACCESS) {
//            enhancedMuleClient.setCredentialsLoader(new CredentialsProviderAccessTokenImpl(credential.getId(), credential.getSecret()));
//        }
//        return enhancedMuleClient;
        return null;
    }


    public Organization findOrganization(String organization) throws IOException, ProfileNotFoundException, NotFoundException {
        if( organization != null ) {
            return getClient(organization,null).getAnypointClient().findOrganizationByNameOrId(organization);
        } else {
            final AnypointClient anypointClient = getClient().getAnypointClient();
            final Organization org = anypointClient.getUser().getOrganization();
            org.setClient(anypointClient);
            return org;
        }
    }

    static class CredentialsArgs {
//        @Option(names = {"--ci", "--credential-id"}, description = "Credential Identifier")
//        private String id;
//        @Option(names = {"--cs", "--credential-secret"}, description = "Credential Secret")
//        private String secret;
//        @Option(names = {"--ct", "--credential-type"}, description = "Credential Secret")
//        private CredentialType type;
    }
}
