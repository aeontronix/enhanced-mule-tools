/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.config.CredentialsProviderAnypointBearerToken;
import com.aeontronix.enhancedmule.tools.config.CredentialsProviderAnypointUsernamePasswordImpl;
import com.aeontronix.enhancedmule.tools.config.CredentialsProviderInteractiveAuthentication;
import com.aeontronix.commons.StringUtils;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.List;

@Component(role = AbstractMavenLifecycleParticipant.class)
public class EMTExtension extends AbstractMavenLifecycleParticipant implements Contextualizable {
    public static final String PLUGINKEY = "com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin";
    public static final String TOKEN = "~~~Token~~~";
    static EnhancedMuleClient emClient;

    @Override
    public void contextualize(Context context) throws ContextException {
    }

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        try {
            String enhancedMuleServerUrl = getProperty(session, "enhancedMuleServerUrl", "enhancedmule.server.url", AbstractAnypointMojo.DEFAULT_EMSERVER_URL);
            emClient = new EnhancedMuleClient(enhancedMuleServerUrl);
            String anypointBearerToken = getProperty(session, "bearerToken", AbstractAnypointMojo.BEARER_TOKEN_PROPERTY, null);
            String username = getProperty(session, "username", "anypoint.username", null);
            String password = getProperty(session, "password", "anypoint.password", null);
            boolean interactiveAuth = Boolean.parseBoolean(getProperty(session, "interactiveAuth", "anypoint.auth.interactive", "false"));
            if (anypointBearerToken != null) {
                emClient.setCredentialsLoader(new CredentialsProviderAnypointBearerToken(anypointBearerToken));
            } else if (StringUtils.isNotBlank(username)) {
                emClient.setCredentialsLoader(new CredentialsProviderAnypointUsernamePasswordImpl(username, password));
            } else if (interactiveAuth && session.getRequest().isInteractiveMode() ) {
                emClient.setCredentialsLoader(new CredentialsProviderInteractiveAuthentication());
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
