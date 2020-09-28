/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.kloudtek.util.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAnypointMojo extends AbstractMojo {
    public static final String BEARER_TOKEN_PROPERTY = "anypoint.bearer";
    public static final String DEFAULT_EMSERVER_URL = "https://api.enhanced-mule.com";
    private static final Logger logger = LoggerFactory.getLogger(AbstractAnypointMojo.class);
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
    @Parameter(property = BEARER_TOKEN_PROPERTY)
    protected String bearerToken;
    @Parameter(property = "enhancedmule.server.url", defaultValue = DEFAULT_EMSERVER_URL)
    protected String enhancedMuleServerUrl;
    protected EnhancedMuleClient emClient;
    private AnypointClient client;
    @Parameter(defaultValue = "true", property = "anypoint.auth.bearer-property.set")
    private boolean addBearerTokenProperty;
    @Parameter(defaultValue = BEARER_TOKEN_PROPERTY, property = "anypoint.auth.bearer-property.property")
    private String addBearerTokenPropertyKey;
    @Parameter(defaultValue = "anypoint-exchange-v2", property = "anypoint.auth.addservercreds.serverid")
    private String addServerCredentialsServerId;
    @Parameter(defaultValue = "true", property = "anypoint.auth.interactive")
    private boolean interactiveAuth;
    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    public AbstractAnypointMojo() {
    }

    public synchronized AnypointClient getClient() {
        if (client == null) {
            client = ClientBuilder.buildClient(bearerToken, username, password, clientId, clientSecret, settings);
        }
        return client;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        emClient = new EnhancedMuleClient(enhancedMuleServerUrl);
        try {
            doExecute();
        } catch (MojoFailureException | MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            if (this.client != null) {
                IOUtils.close(this.client);
            }
        }
    }

    protected abstract void doExecute() throws Exception;
}
