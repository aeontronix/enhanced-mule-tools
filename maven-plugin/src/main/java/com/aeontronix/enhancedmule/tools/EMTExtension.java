/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.authentication.AccessTokens;
import com.aeontronix.enhancedmule.tools.authentication.EMuleInteractiveAuthenticator;
import com.aeontronix.enhancedmule.tools.util.MavenUtils;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.repository.AuthenticationSelector;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.List;

import static com.kloudtek.util.StringUtils.isEmpty;

@Component(role = AbstractMavenLifecycleParticipant.class)
public class EMTExtension extends AbstractMavenLifecycleParticipant implements Contextualizable {
    public static final String PLUGINKEY = "com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin";
    public static final String TOKEN = "~~~Token~~~";
    private EnhancedMuleClient emClient;

    @Override
    public void contextualize(Context context) throws ContextException {
    }

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        try {
            String enhancedMuleServerUrl = getProperty(session, "enhancedMuleServerUrl", "enhancedmule.server.url", AbstractAnypointMojo.DEFAULT_EMSERVER_URL);
            emClient = new EnhancedMuleClient(enhancedMuleServerUrl);
            String bearerToken = getProperty(session, "bearerToken", AbstractAnypointMojo.BEARER_TOKEN_PROPERTY, null);
            String username = getProperty(session, "username", "anypoint.username", null);
            String password = getProperty(session, "password", "anypoint.password", null);
            boolean addBearerTokenProperty = Boolean.parseBoolean(getProperty(session, "addBearerTokenProperty", "anypoint.auth.bearer-property.set", "true"));
            String addBearerTokenPropertyKey = getProperty(session, "addBearerTokenPropertyKey", "anypoint.auth.bearer-property.property", AbstractAnypointMojo.BEARER_TOKEN_PROPERTY);
            boolean interactiveAuth = Boolean.parseBoolean(getProperty(session, "interactiveAuth", "anypoint.auth.interactive", "true"));
            boolean addServerCredentials = Boolean.parseBoolean(getProperty(session, "addServerCredentials", "anypoint.auth.addservercreds", "true"));
            String serverId = getProperty(session, "addServerCredentialsServerId", "anypoint.auth.addservercreds.serverid", "anypoint-exchange-v2");

            final AccessTokens tokens;
            if (isEmpty(bearerToken) && isEmpty(username) && interactiveAuth && session.getRequest().isInteractiveMode()) {
                tokens = new EMuleInteractiveAuthenticator(emClient.getRestClient()).authenticate();
            } else {
                final AnypointClient anypointClient = ClientBuilder.buildClient(bearerToken, username, password, null, null, session.getSettings());
                tokens = anypointClient.getBearerToken();
            }
            if (addBearerTokenProperty) {
                session.getUserProperties().put(addBearerTokenPropertyKey, tokens.getAnypointAccessToken());
            }
            if (addServerCredentials) {
                AuthenticationSelector authenticationSelector = session.getRepositorySession().getAuthenticationSelector();
                final String anypointAccessToken = tokens.getAnypointAccessToken();
                MavenUtils.addRepositoryUsernamePassword(authenticationSelector, serverId, TOKEN, anypointAccessToken);
                final MavenProject currentProject = session.getCurrentProject();
                final List<ArtifactRepository> remoteArtifactRepositories = currentProject.getRemoteArtifactRepositories();
                ArtifactRepository arepo = findRepo(remoteArtifactRepositories, serverId);
                final Authentication authentication = new Authentication(TOKEN, anypointAccessToken);
                arepo.setAuthentication(authentication);
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
                final Server s = new Server();
                s.setId(serverId);
                s.setUsername(TOKEN);
                s.setPassword(anypointAccessToken);
                session.getSettings().getServers().add(s);
            }
        } catch (Exception e) {
            throw new MavenExecutionException(e.getMessage(), e);
        }
        super.afterProjectsRead(session);
    }

    private ArtifactRepository findRepo(List<ArtifactRepository> remoteArtifactRepositories, String serverId) {
        for (ArtifactRepository remoteArtifactRepository : remoteArtifactRepositories) {
            if (remoteArtifactRepository.getId().equals(serverId)) {
                return remoteArtifactRepository;
            }
        }
        throw new IllegalStateException("serverId not found: " + serverId);
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
            final String attribute = ((Xpp3Dom) configuration).getAttribute(field);
            if (attribute != null) {
                return attribute;
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
}
