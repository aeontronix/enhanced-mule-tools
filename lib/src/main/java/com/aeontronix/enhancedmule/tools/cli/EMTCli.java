/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.anypointsdk.AnypointClient;
import com.aeontronix.anypointsdk.auth.AnypointClientCredentialsAuthenticationHandler;
import com.aeontronix.anypointsdk.auth.AnypointUPWAuthenticationHandler;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.cli.apim.APIManagerCmd;
import com.aeontronix.enhancedmule.tools.cli.application.ApplicationCmd;
import com.aeontronix.enhancedmule.tools.cli.cloudhub.CloudhubCmd;
import com.aeontronix.enhancedmule.tools.cli.config.ActiveProfileCmd;
import com.aeontronix.enhancedmule.tools.cli.config.ConfigCmd;
import com.aeontronix.enhancedmule.tools.cli.crypto.DecryptCmd;
import com.aeontronix.enhancedmule.tools.cli.crypto.EncryptCmd;
import com.aeontronix.enhancedmule.tools.cli.crypto.KeyGenCmd;
import com.aeontronix.enhancedmule.tools.cli.exchange.ExchangeCmd;
import com.aeontronix.enhancedmule.tools.config.*;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import com.aeontronix.enhancedmule.tools.util.CredentialsConverter;
import com.aeontronix.enhancedmule.tools.util.VersionHelper;
import com.aeontronix.restclient.auth.AuthenticationHandler;
import com.aeontronix.restclient.auth.BearerTokenAuthenticationHandler;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.LineReader;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static picocli.CommandLine.ArgGroup;
import static picocli.CommandLine.Option;

@Command(name = "emt", subcommands = {
        ConfigCmd.class, KeyGenCmd.class, EncryptCmd.class,
        DecryptCmd.class, ActiveProfileCmd.class, LoginCmd.class, UserInfoCmd.class, APIManagerCmd.class,
        ExchangeCmd.class, ApplicationCmd.class, CloudhubCmd.class
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
    @Option(names = {"-u", "--base-url"}, description = "Anypoint base URL")
    private URL anypointUrl;
    private EMConfig config;
    private ConfigProfile activeProfile;
    private AnypointClient anypointClient;

    public EMTCli() throws IOException, ProfileNotFoundException {
        this(true);
    }

    public EMTCli(boolean loadConfig) throws IOException, ProfileNotFoundException {
        if (loadConfig) {
            config = EMConfig.findConfigFile();
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
        if (activeProfile == null) {
            if (profileName != null) {
                activeProfile = config.getProfileByProfileName(profileName);
            } else {
                activeProfile = config.getProfile(null);
            }
        }
        return activeProfile;
    }

    @NotNull
    public String getActiveProfileId() throws IOException, ProfileNotFoundException {
        return profileName != null ? profileName : config.getActive();
    }

    public void saveConfig() throws IOException {
        config.save();
    }

    public synchronized AnypointClient getAnypointClient() throws IOException, ProfileNotFoundException {
        if (anypointClient == null) {
            ConfigProfile prof = getActiveProfile();
            AuthenticationHandler authenticationHandler;
            if (credentialsArgs != null) {
                authenticationHandler = credentialsArgs.getAuthenticationHandler();
            } else {
                ConfigCredentials creds = prof.getCredentials();
                if (creds instanceof CredentialsClientCredentialsImpl) {
                    authenticationHandler = new AnypointClientCredentialsAuthenticationHandler(((CredentialsClientCredentialsImpl) creds).getClientId(),
                            ((CredentialsClientCredentialsImpl) creds).getClientSecret());
                } else if (creds instanceof CredentialsUsernamePasswordImpl) {
                    authenticationHandler = new AnypointUPWAuthenticationHandler(((CredentialsUsernamePasswordImpl) creds).getUsername(),
                            ((CredentialsUsernamePasswordImpl) creds).getPassword());
                } else if (creds instanceof CredentialsBearerTokenImpl) {
                    authenticationHandler = new BearerTokenAuthenticationHandler(((CredentialsBearerTokenImpl) creds).getBearerToken());
                } else {
                    throw new IllegalStateException("No valid credentials specified in command or present in active configuration");
                }
            }
            String profileAnypointUrl = prof.getAnypointUrl();
            anypointClient = AnypointClient.builder()
                    .anypointUrl(anypointUrl != null ? anypointUrl.toString() :
                            profileAnypointUrl != null ? profileAnypointUrl : AnypointClient.ANYPOINT_DEFAULT_URL)
                    .authenticationHandler(authenticationHandler).build();
        }
        return anypointClient;
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
        final EnhancedMuleClient client = createEMClient();
        client.getLegacyAnypointClient().findEnvironment(organizationName, environmentName, false, false, null);
        return null;
    }

    public EnhancedMuleClient createEMClient() throws IOException, ProfileNotFoundException {
        ConfigCredentials credentials;
        credentials = credentialsArgs != null ? credentialsArgs.getCredentials() : null;
        ConfigProfile profile = getActiveProfile();
        if (credentials == null) {
            credentials = profile.getCredentials();
        }
        if (anypointUrl == null && profile.getAnypointUrl() != null) {
            anypointUrl = new URL(profile.getAnypointUrl());
        }
        return EnhancedMuleClient
                .builder(CredentialsConverter.convert(credentials))
                .proxySettings(null)
                .anypointUrl(anypointUrl != null ? anypointUrl.toString() : null)
                .insecure(profile.isInsecureServer())
                .build();
    }

    public Organization findOrganization(String organization) throws IOException, ProfileNotFoundException, NotFoundException {
        if (organization != null) {
            return createEMClient().getLegacyAnypointClient().findOrganizationByNameOrId(organization);
        } else {
            final LegacyAnypointClient anypointClient = createEMClient().getLegacyAnypointClient();
            final Organization org = anypointClient.getUser().getOrganization();
            org.setClient(anypointClient);
            return org;
        }
    }

}
