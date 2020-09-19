/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.authentication.AccessTokens;
import com.aeontronix.enhancedmule.tools.authentication.EMuleInteractiveAuthenticator;
import com.aeontronix.enhancedmule.tools.util.MavenUtils;
import com.aeontronix.enhancedmule.tools.util.restclient.RESTClient;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.repository.AuthenticationSelector;

import static com.kloudtek.util.StringUtils.isEmpty;

/**
 * Prepare a project for deployment to anypoint exchange maven.
 */
@Mojo(name = "auth", defaultPhase = LifecyclePhase.VALIDATE)
public class AuthenticateMojo extends AbstractOrganizationalMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;
    @Parameter(defaultValue = "true")
    private boolean addServerCredentials;
    @Parameter(defaultValue = "anypoint-exchange-v2")
    private String serverId;
    @Parameter(defaultValue = "true", property = "anypoint.auth.bearer-property.set")
    private boolean addBearerTokenProperty;
    @Parameter(defaultValue = BEARER_TOKEN_PROPERTY, property = "anypoint.auth.bearer-property.property")
    private String addBearerTokenPropertyKey;
    @Parameter(defaultValue = "true", property = "anypoint.auth.interactive")
    private boolean interactive;
    @Parameter(defaultValue = "false", property = "anypoint.auth.skip")
    private boolean skip;

    @Override
    protected void doExecute() throws Exception {
        if (!skip) {
            final AccessTokens tokens;
            if (isEmpty(bearerToken) && isEmpty(username) && isEmpty(clientId) && interactive && session.getRequest().isInteractiveMode() ) {
                tokens = new EMuleInteractiveAuthenticator(emClient.getRestClient()).authenticate();
            } else {
                tokens = getClient().getBearerToken();
            }
            if( addBearerTokenProperty ) {
                session.getUserProperties().put(addBearerTokenPropertyKey, tokens.getAnypointAccessToken());
            }
            if (addServerCredentials) {
                AuthenticationSelector authenticationSelector = session.getRepositorySession().getAuthenticationSelector();
                MavenUtils.addRepositoryUsernamePassword(authenticationSelector, serverId, "~~~Token~~~", tokens.getAnypointAccessToken());
            }
        }
    }
}
