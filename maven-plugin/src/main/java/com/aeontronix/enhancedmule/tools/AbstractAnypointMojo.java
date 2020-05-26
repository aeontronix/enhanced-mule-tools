/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

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
     * Anypoint bearer token
     */
    @Parameter(property = "anypoint.bearer")
    protected String bearer;
    /**
     * If set to true, will use oauth client credentials (use client id as credentials and client secret as password)
     */
    @Parameter(property = "anypoint.clientcredentials")
    protected boolean clientCredentials;
    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    public synchronized AnypointClient getClient() {
        if (client == null) {
            AuthenticationProvider authenticationProvider;
            if (bearer != null) {
                authenticationProvider = new AuthenticationProviderBearerTokenImpl(bearer);
            } else if (isNotBlank(username) && isNotBlank(password)) {
                if (clientCredentials) {
                    logger.debug("Using client credentials: {}", username);
                    authenticationProvider = new AuthenticationProviderClientCredentialsImpl(username, password);
                } else {
                    logger.debug("Using username/password credentials: {}", username);
                    authenticationProvider = new AuthenticationProviderUsernamePasswordImpl(username, password);
                }
            } else {
                throw new IllegalArgumentException("No authentication credentials specified (username/password or bearer)");
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
