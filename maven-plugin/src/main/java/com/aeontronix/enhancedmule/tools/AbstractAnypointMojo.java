/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractAnypointMojo extends AbstractMojo {
    public static final String BEARER_TOKEN_PROPERTY = "anypoint.bearer";
    public static final String DEFAULT_EMSERVER_URL = "https://api.enhanced-mule.com";
    public static final String EM_CLIENT = "emClient";
    public static final String EM_CLIENT_ID = "anypoint.client.id";
    public static final String EM_CLIENT_SECRET = "anypoint.client.secret";
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
    @Parameter(property = EM_CLIENT_ID)
    protected String clientId;
    @Parameter(property = EM_CLIENT_SECRET)
    protected String clientSecret;
    /**
     * Anypoint bearer token
     */
    @Parameter(property = BEARER_TOKEN_PROPERTY)
    protected String bearerToken;
    @Parameter(property = "enhancedmule.server.url", defaultValue = DEFAULT_EMSERVER_URL)
    protected String enhancedMuleServerUrl;
    @Parameter(defaultValue = "${settings}", readonly = true)
    protected Settings settings;
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;
    /**
     * Anypoint organization name
     */
    @Parameter(property = "anypoint.org")
    protected String org;
    @Parameter(property = "profile")
    protected String profile;
    protected EnhancedMuleClient emClient;
    private AnypointClient client;

    public AbstractAnypointMojo() {
    }

    public synchronized AnypointClient getClient() throws IOException {
        if (client == null) {
            client = AnypointClientBuilder.buildClient(emClient.getAnypointBearerToken(), settings);
        }
        return client;
    }

    public EnhancedMuleClient getEmClient() {
        return emClient;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            emClient = EMTExtension.createClient(enhancedMuleServerUrl, session, bearerToken, username, password,
                    clientId, clientSecret, profile, org, project != null ? project.getGroupId() : null);
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

    protected String getProperty(String name) {
        String property = session.getUserProperties().getProperty(name);
        if( property == null ) {
            property = project.getProperties().getProperty(name);
        }
        return property;
    }

    protected Map<String, String> findPrefixProperties(Map<String, String> target, String prefix) {
        if (project != null) {
            target = findPrefixProperties(project.getProperties(), target, prefix);
        }
        target = findPrefixProperties(session.getUserProperties(), target, prefix);
        return target;
    }

    protected static Map<String, String> findPrefixProperties(Properties source, Map<String, String> target, String prefix) {
        for (Map.Entry<Object, Object> entry : source.entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith(prefix)) {
                key = key.substring(prefix.length() + 1);
                if (StringUtils.isNotBlank(key)) {
                    String value = entry.getValue().toString();
                    if (target == null) {
                        target = new HashMap<>();
                    }
                    target.put(key, value);
                }
            }
        }
        return target;
    }
}
