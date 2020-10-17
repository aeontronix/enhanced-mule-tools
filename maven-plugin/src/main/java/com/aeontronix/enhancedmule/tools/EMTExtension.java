/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.config.*;
import com.aeontronix.enhancedmule.tools.util.MavenUtils;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.repository.AuthenticationSelector;
import org.eclipse.aether.repository.RemoteRepository;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Component(role = AbstractMavenLifecycleParticipant.class)
public class EMTExtension extends AbstractMavenLifecycleParticipant {
    public static final String PLUGINKEY = "com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin";
    public static final String TOKEN = "~~~Token~~~";
    public static final String ENHANCED_MULE_CLIENT = "enhancedMuleClient";
    private static final Logger logger = getLogger(EMTExtension.class);
    private EnhancedMuleClient emClient;
    private AnypointBearerTokenCredentialsProvider credentialsProvider;

    static EnhancedMuleClient createClient(String enhancedMuleServerUrl, MavenSession session, String anypointBearerToken, String username, String password, boolean interactiveAuth) throws MavenExecutionException {
        EnhancedMuleClient emClient = (EnhancedMuleClient) session.getCurrentProject().getContextValue(ENHANCED_MULE_CLIENT);
        if (emClient == null) {
            emClient = new EnhancedMuleClient(enhancedMuleServerUrl);
            session.getCurrentProject().setContextValue(ENHANCED_MULE_CLIENT, emClient);
            logger.info("Initializing Enhanced Mule Tools");
            CredentialsProvider credentialsProvider;
            if (anypointBearerToken != null) {
                logger.info("Using Bearer Token");
                credentialsProvider = new CredentialsProviderAnypointBearerToken(anypointBearerToken);
            } else if (StringUtils.isNotBlank(username)) {
                logger.info("Using Username Password");
                credentialsProvider = new CredentialsProviderAnypointUsernamePasswordImpl(username, password);
            } else if (interactiveAuth && session.getRequest().isInteractiveMode()) {
                logger.info("Using Interactive login");
                credentialsProvider = new CredentialsProviderInteractiveAuthentication();
            } else {
                throw new MavenExecutionException("No credentials provided and not in interactive mode", (File) null);
            }
            emClient.setCredentialsLoader(credentialsProvider);
        }
        return emClient;
    }

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        try {
            String enhancedMuleServerUrl = getProperty(session, "enhancedMuleServerUrl", "enhancedmule.server.url", AbstractAnypointMojo.DEFAULT_EMSERVER_URL);
            String anypointBearerToken = getProperty(session, "bearerToken", AbstractAnypointMojo.BEARER_TOKEN_PROPERTY, null);
            String username = getProperty(session, "username", "anypoint.username", null);
            String password = getProperty(session, "password", "anypoint.password", null);
            boolean interactiveAuth = Boolean.parseBoolean(getProperty(session, "interactiveAuth", "anypoint.auth.interactive", "false"));
            emClient = createClient(enhancedMuleServerUrl, session, anypointBearerToken, username, password, interactiveAuth);
            String serverId = getProperty(session, "serverId", "anypoint.serverid", "anypoint-exchange-v2");
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
            final RemoteRepository newRepo = new RemoteRepository.Builder(prep).setId(prep.getId())
                    .setUrl(prep.getUrl())
                    .setSnapshotPolicy(prep.getPolicy(true))
                    .setReleasePolicy(prep.getPolicy(false))
                    .setRepositoryManager(prep.isRepositoryManager())
                    .setProxy(prep.getProxy())
                    .setMirroredRepositories(prep.getMirroredRepositories())
                    .setContentType(prep.getContentType())
                    .setAuthentication(authenticationSelector.getAuthentication(prep)).build();
            remoteProjectRepositories.remove(prep);
            remoteProjectRepositories.add(newRepo);
        } catch (Exception e) {
            throw new MavenExecutionException(e.getMessage(), e);
        }
        super.afterProjectsRead(session);
    }

    private RemoteRepository findRemoteRepo(List<RemoteRepository> remoteArtifactRepositories, String serverId) {
        for (RemoteRepository remoteArtifactRepository : remoteArtifactRepositories) {
            if (remoteArtifactRepository.getId().equals(serverId)) {
                return remoteArtifactRepository;
            }
        }
        throw new IllegalStateException("serverId not found: " + serverId);
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
        if (defaultValue != null) {
            return defaultValue;
        }
        return null;
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
