/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.authentication.AuthenticationProvider;
import com.aeontronix.enhancedmule.tools.authentication.AuthenticationProviderBearerTokenImpl;
import com.aeontronix.enhancedmule.tools.authentication.AuthenticationProviderConnectedAppsImpl;
import com.aeontronix.enhancedmule.tools.authentication.AuthenticationProviderUsernamePasswordImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.kloudtek.util.StringUtils.isNotBlank;

public abstract class AbstractAnypointMojo extends AbstractMojo {
    private static final Logger logger = LoggerFactory.getLogger(AbstractAnypointMojo.class);
    private AnypointClient client;
    /**
     * Anypoint username
     */
    @Parameter(property = "anypoint.username")
    protected String username;
    /**
     * Anypoint password
     */
    @Parameter(property = "anypoint.password")
    protected String password;
    /**
     * Anypoint username
     */
    @Parameter(property = "anypoint.client.id")
    protected String clientId;
    /**
     * Anypoint password
     */
    @Parameter(property = "anypoint.client.secret")
    protected String clientSecret;
    /**
     * Anypoint bearer token
     */
    @Parameter(property = "anypoint.bearer")
    protected String bearer;
    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    public synchronized AnypointClient getClient() {
        if (client == null) {
            AuthenticationProvider authenticationProvider;
            if (bearer != null) {
                authenticationProvider = new AuthenticationProviderBearerTokenImpl(bearer);
            } else if(isNotBlank(username) && isNotBlank(password)) {
                logger.debug("Using username/password credentials: {}", username);
                authenticationProvider = new AuthenticationProviderUsernamePasswordImpl(username, password);
            } else if (isNotBlank(clientId) && isNotBlank(clientSecret)) {
                logger.debug("Using client credentials: {}", clientId);
                authenticationProvider = new AuthenticationProviderConnectedAppsImpl(clientId, clientSecret);
            } else {
                throw new IllegalArgumentException("No authentication credentials specified (username/password, client id/secret or bearer)");
            }
            client = new AnypointClient(authenticationProvider);
        }

        Proxy proxy = settings.getActiveProxy();
        logger.debug("Checking debug settings");
        if (proxy != null) {
            logger.debug("Using proxy: " + proxy.getProtocol() + " " + proxy.getHost() + " " + proxy.getPort());
            client.setProxy(proxy.getProtocol(), proxy.getHost(), proxy.getPort(), proxy.getUsername(), proxy.getPassword());
        } else {
            logger.debug("No proxy specified");
        }
        return client;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            doExecute();
        } catch (MojoFailureException | MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected abstract void doExecute() throws Exception;
}
