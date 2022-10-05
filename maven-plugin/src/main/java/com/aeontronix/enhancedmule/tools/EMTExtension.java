/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.ReflectionUtils;
import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.EMConfig;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import com.aeontronix.enhancedmule.tools.emclient.authentication.*;
import com.aeontronix.enhancedmule.tools.util.CredentialsConverter;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.MavenUtils;
import org.apache.http.HttpHost;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.repository.AuthenticationSelector;
import org.eclipse.aether.repository.RemoteRepository;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.aeontronix.commons.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

@Component(role = AbstractMavenLifecycleParticipant.class)
public class EMTExtension extends AbstractMavenLifecycleParticipant {
    public static final String PLUGINKEY = "com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin";
    public static final String TOKEN = "~~~Token~~~";
    public static final String ENHANCED_MULE_CLIENT = "enhancedMuleClient";
    private static final Logger logger = getLogger(EMTExtension.class);
    private static ConfigProfile configProfile;
    private EnhancedMuleClient emClient;
    private AnypointBearerTokenCredentialsProvider credentialsProvider;
    private boolean mulePluginCompatibility;
    private String enhancedMuleServerUrl;
    private String anypointBearerToken;
    private String username;
    private String password;
    private String emAccessTokenId;
    private String emAccessTokenSecret;
    private String serverId;
    private String org;
    private String profile;
    private static EMConfig emConfig;

    static EnhancedMuleClient createClient(String enhancedMuleServerUrl, MavenSession session, String anypointBearerToken,
                                           String username, String password, String emAccessTokenId, String emAccessTokenSecret,
                                           String profile, String org, String groupId) throws MavenExecutionException {
        EnhancedMuleClient emClient;
        try {
            emClient = (EnhancedMuleClient) session.getCurrentProject().getContextValue(ENHANCED_MULE_CLIENT);
        } catch (ClassCastException e) {
            emClient = null;
        }
        try {
            if (emClient == null) {
                emConfig = EMConfig.findConfigFile();
                emConfig.checkProfileExists(profile);
                if (profile != null) {
                    emConfig.checkProfileExists(profile);
                    emConfig.setActive(profile);
                }
                configProfile = emConfig.getActiveProfile();
                emClient = new EnhancedMuleClient(enhancedMuleServerUrl, configProfile);
                final Proxy proxy = session.getSettings().getActiveProxy();
                if (proxy != null) {
                    emClient.setProxy(new HttpHost(proxy.getHost()), proxy.getUsername(), proxy.getPassword());
                }
                session.getCurrentProject().setContextValue(ENHANCED_MULE_CLIENT, emClient);

                logger.info("Initializing Enhanced Mule Tools");
                CredentialsProvider credentialsProvider = null;
                if (isNotBlank(anypointBearerToken)) {
                    logger.info("Using Bearer Token");
                    credentialsProvider = new CredentialsProviderAnypointBearerToken(anypointBearerToken);
                } else if (isNotBlank(username) && isNotBlank(password)) {
                    logger.info("Using Username Password: {}", username);
                    credentialsProvider = new CredentialsProviderAnypointUsernamePasswordImpl(username, password);
                } else if (isNotBlank(emAccessTokenId) && isNotBlank(emAccessTokenSecret)) {
                    logger.info("Using Access Token");
                    credentialsProvider = new CredentialsProviderClientCredentialsImpl(emAccessTokenId, emAccessTokenSecret);
                } else {
                    if (configProfile != null && configProfile.getCredentials() != null) {
                        credentialsProvider = CredentialsConverter.convert(configProfile.getCredentials());
                    }
                    if (credentialsProvider == null) {
                        logger.info("No EMT credentials available");
                        credentialsProvider = new CredentialsProviderEmptyImpl();
                    }
                }
                emClient.setCredentialsLoader(credentialsProvider);
            }
        } catch (Exception e) {
            logger.warn("Failed to initialize emClient");
            throw new MavenExecutionException(e.getMessage(), e);
        }
        return emClient;
    }

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        try {
            loadProperties(session);
            emConfig = EMConfig.findConfigFile();
            logger.info(Ansi.ansi().fgBrightYellow().a("Profile: ").reset().a(emConfig.getActive()).toString());
            final MavenProject project = session.getTopLevelProject();
            emClient = createClient(enhancedMuleServerUrl, session, anypointBearerToken, username, password,
                    emAccessTokenId, emAccessTokenSecret, profile, org, project != null ? project.getGroupId() : null);
            try {
                emClient.getAnypointClient().getUser();
            } catch (IOException e) {
                logger.warn("Unable to verify authentication");
            }
            addRepositoriesAuthentication(session);
        } catch (Throwable e) {
            if (e instanceof HttpException && ((HttpException) e).getStatusCode() == 401) {
                logger.warn("Anypoint authentication failed", e);
            } else {
                logger.warn("Unable to setup extension emclient", e);
            }
        }
        super.afterProjectsRead(session);
    }

    private void addRepositoriesAuthentication(MavenSession session) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        final MavenProject currentProject = session.getCurrentProject();
        AuthenticationSelector authenticationSelector = session.getRepositorySession().getAuthenticationSelector();
        // Lambdas here break the build for some @#$@#$@#$ reason
        //noinspection Convert2Lambda
        MavenUtils.addRepositoryUsernamePassword(authenticationSelector, serverId, TOKEN, new MavenUtils.SecretResolver() {
            @Override
            public String getSecret() throws Exception {
                return emClient.getAnypointBearerToken();
            }
        });
        final List<RemoteRepository> remoteProjectRepositories = currentProject.getRemoteProjectRepositories();
        RemoteRepository prep = findRemoteRepo(remoteProjectRepositories, serverId);
        if (prep != null) {
            try {
                logger.debug("Creating new authentication object from selector");
                final org.eclipse.aether.repository.Authentication authentication = authenticationSelector.getAuthentication(prep);
                logger.debug("Creating RemoteRepository builder");
                RemoteRepository.Builder builder = new RemoteRepository.Builder(prep)
                        .setId(prep.getId())
                        .setUrl(prep.getUrl())
                        .setSnapshotPolicy(prep.getPolicy(true))
                        .setReleasePolicy(prep.getPolicy(false))
                        .setRepositoryManager(prep.isRepositoryManager())
                        .setProxy(prep.getProxy())
                        .setMirroredRepositories(prep.getMirroredRepositories())
                        .setContentType(prep.getContentType());
                logger.debug("Setting authentication object");
                try {
                    builder = builder.setAuthentication(authentication);
                } catch (NullPointerException e) {
                    logger.debug("Weird issue with NPE in builder.setAuthentication() occurred, trying to brute force fix issue");
                    try {
                        logger.debug("Prototype={}", ReflectionUtils.get(builder, "prototype"));
                    } catch (Throwable exception) {
                        logger.debug("Unable to log prototype :(", exception);
                    }
                    ReflectionUtils.set(builder, "prototype", null);
                    builder = builder.setAuthentication(authentication);
                }
                logger.debug("Building RemoteRepository");
                final RemoteRepository newRepo = builder.build();
                logger.debug("Removing old remote repository");
                remoteProjectRepositories.remove(prep);
                logger.debug("Adding new remote repository");
                remoteProjectRepositories.add(newRepo);
            } catch (Exception e) {
                logger.warn("Unable to add credentials to anypoint exchange maven repository: " + e.getMessage(), e);
            }
        }
    }

    private void loadProperties(MavenSession session) {
        mulePluginCompatibility = Boolean.parseBoolean(getProperty(session, "mulePluginCompatibility", "muleplugin.compat", "false"));
        enhancedMuleServerUrl = getProperty(session, "enhancedMuleServerUrl", "enhancedmule.server.url", AbstractAnypointMojo.DEFAULT_EMSERVER_URL);
        org = getProperty(session, "org", "anypoint.org", null);
        anypointBearerToken = getProperty(session, "bearerToken", AbstractAnypointMojo.BEARER_TOKEN_PROPERTY, null);
        username = getProperty(session, "username", "anypoint.username", null);
        password = getProperty(session, "password", "anypoint.password", null);
        emAccessTokenId = getProperty(session, "emAccessTokenId", "emule.accesstoken.id", null);
        emAccessTokenSecret = getProperty(session, "emAccessTokenSecret", "emule.accesstoken.secret", null);
        serverId = getProperty(session, "serverId", "anypoint.serverid", "anypoint-exchange-v2");
        profile = getProperty(session, "profile", "profile", null);
    }

    private RemoteRepository findRemoteRepo(List<RemoteRepository> remoteArtifactRepositories, String serverId) {
        for (RemoteRepository remoteArtifactRepository : remoteArtifactRepositories) {
            if (remoteArtifactRepository.getId().equals(serverId)) {
                return remoteArtifactRepository;
            }
        }
        return null;
    }

    private String getProperty(MavenSession session, String field, String property, String defaultValue) {
        final MavenProject currentProject = session.getCurrentProject();
        final Plugin plugin = currentProject.getPlugin(PLUGINKEY);
        if (plugin == null) {
            throw new IllegalStateException("Unable to find plugin " + PLUGINKEY);
        }
        final Object configuration = plugin.getConfiguration();
        if (configuration instanceof Xpp3Dom) {
            final Xpp3Dom attribute = ((Xpp3Dom) configuration).getChild(field);
            if (attribute != null) {
                return attribute.getValue();
            }
        }
        Object prop = session.getUserProperties().get(property);
        if (prop != null) {
            return prop.toString();
        }
        prop = currentProject.getProperties().get(property);
        if (prop != null) {
            return prop.toString();
        }
        return defaultValue;
    }

    public class AuthenticationWrapper extends Authentication {
        private String bearerToken;
        private EnhancedMuleClient emClient;

        public AuthenticationWrapper(EnhancedMuleClient emClient) {
            super(TOKEN, null);
            this.emClient = emClient;
        }

        @Override
        public String getPassword() {
            if (bearerToken == null) {
                try {
                    bearerToken = emClient.getAnypointBearerToken();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return bearerToken;
        }
    }
}
