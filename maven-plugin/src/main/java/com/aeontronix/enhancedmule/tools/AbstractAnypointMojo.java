/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.commons.io.IOUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractAnypointMojo extends AbstractMojo {
    public static final String BEARER_TOKEN_PROPERTY = "anypoint.bearer";
    public static final String DEFAULT_EMSERVER_URL = "https://api.enhanced-mule.com";
    private static final Logger logger = LoggerFactory.getLogger(AbstractAnypointMojo.class);
    public static final String EM_CLIENT = "emClient";
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
    @Parameter(defaultValue = "true", property = "anypoint.auth.interactive")
    private boolean interactiveAuth;
    @Parameter(defaultValue = "${settings}", readonly = true)
    protected Settings settings;
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    public AbstractAnypointMojo() {
    }

    public synchronized AnypointClient getClient() throws IOException {
        if (client == null) {
            client = ClientBuilder.buildClient(emClient.getAnypointBearerToken(), null, null, null, null, settings);
        }
        return client;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            emClient = EMTExtension.createClient(enhancedMuleServerUrl, session, bearerToken, username, password, interactiveAuth);
        } catch (MavenExecutionException e) {
            Throwable cause = e.getCause();
            if( cause == null ) {
                cause = e;
            }
            throw new MojoExecutionException(cause.getMessage(), cause);
        }
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
