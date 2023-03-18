/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.anypointsdk.AnypointClient;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.config.ConfigProfile;
import com.aeontronix.enhancedmule.tools.config.EMConfig;
import com.aeontronix.enhancedmule.tools.config.ProfileNotFoundException;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import com.aeontronix.enhancedmule.tools.emclient.authentication.*;
import com.aeontronix.enhancedmule.tools.util.CredentialsConverter;
import com.aeontronix.enhancedmule.tools.util.EMTProperties;
import com.aeontronix.kryptotek.DigestUtils;
import com.aeontronix.restclient.ProxySettings;
import com.aeontronix.restclient.auth.BearerTokenAuthenticationHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.aeontronix.commons.StringUtils.isNotBlank;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractAnypointMojo extends AbstractMojo {
    private static final Logger logger = getLogger(AbstractAnypointMojo.class);
    public static final String BEARER_TOKEN_PROPERTY = "anypoint.bearer";
    public static final String DEFAULT_EMSERVER_URL = "https://api.enhanced-mule.com";
    public static final String EM_CLIENT = "emClient";
    public static final String EM_CLIENT_ID = "anypoint.client.id";
    public static final String EM_CLIENT_SECRET = "anypoint.client.secret";
    public static final String ANYPOINT_USERNAME = "anypoint.username";
    public static final String ANYPOINT_PASSWORD = "anypoint.password";
    /**
     * Anypoint username
     */
    @Parameter(property = ANYPOINT_USERNAME)
    protected String username;
    /**
     * Anypoint password
     */
    @Parameter(property = ANYPOINT_PASSWORD)
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
    @Parameter(property = "anypoint.url")
    protected String anypointPlatformUrl;
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
    private LegacyAnypointClient legacyClient;
    private EMConfig emConfig;
    protected ConfigProfile configProfile;
    protected AnypointClient anypointClient;

    public AbstractAnypointMojo() {
    }

    public synchronized LegacyAnypointClient getLegacyClient() throws IOException, ProfileNotFoundException {
        return legacyClient;
    }

    public EnhancedMuleClient getEmClient() {
        return emClient;
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating client");
                logger.debug("Server URL: {}", enhancedMuleServerUrl);
                logger.debug("Anypoint URL: {}", anypointPlatformUrl);
                logger.debug("Bearer: SHA:{}", bearerToken != null ? StringUtils.base64EncodeToString(DigestUtils.sha512(bearerToken.getBytes(UTF_8))) : "NOT SET");
                logger.debug("Username: {}", username != null ? username : "NOT SET");
                logger.debug("Password: SHA:{}", password != null ? StringUtils.base64EncodeToString(DigestUtils.sha512(password.getBytes(UTF_8))) : "NOT SET");
                logger.debug("Client Id: {}", clientId != null ? clientId : "NOT SET");
                logger.debug("Client Secret: SHA:{}", clientSecret != null ? StringUtils.base64EncodeToString(DigestUtils.sha512(clientSecret.getBytes(UTF_8))) : "NOT SET");
            }
            emConfig = EMConfig.findConfigFile();
            configProfile = emConfig.getProfile(profile);
            logger.info("Using profile: " + (profile != null ? profile : emConfig.getActive()));
            if (anypointPlatformUrl == null) {
                anypointPlatformUrl = configProfile.getAnypointUrl() != null ? configProfile.getAnypointUrl() : "https://anypoint.mulesoft.com";
            }
            logger.info("Anypoint server: {}", anypointPlatformUrl);
            final Proxy proxy = session.getSettings().getActiveProxy();
            CredentialsProvider credentialsProvider = null;
            if (isNotBlank(bearerToken)) {
                logger.info("Using Bearer Token");
                credentialsProvider = new CredentialsProviderAnypointBearerToken(bearerToken);
            } else if (isNotBlank(username) && isNotBlank(password)) {
                logger.info("Using Username Password: {}", username);
                credentialsProvider = new CredentialsProviderAnypointUsernamePasswordImpl(username, password);
            } else if (isNotBlank(clientId) && isNotBlank(clientSecret)) {
                logger.info("Using Client Credentials: {}", clientId);
                credentialsProvider = new CredentialsProviderClientCredentialsImpl(clientId, clientSecret);
            } else {
                if (configProfile != null && configProfile.getCredentials() != null) {
                    credentialsProvider = CredentialsConverter.convert(configProfile.getCredentials());
                }
            }
            if (credentialsProvider == null) {
                logger.info("No EMT credentials available");
                credentialsProvider = new CredentialsProviderEmptyImpl();
            }
            EnhancedMuleClient.Builder emClientBuilder = EnhancedMuleClient.builder(credentialsProvider)
                    .serverUrl(enhancedMuleServerUrl)
                    .anypointUrl(anypointPlatformUrl);
            ProxySettings proxySettings = null;
            if (proxy != null) {
                proxySettings = new ProxySettings(URI.create(proxy.getProtocol() + "://" + proxy.getHost() + ":" + proxy.getPort()),
                        proxy.getUsername(), proxy.getPassword(), null);
                emClientBuilder.proxySettings(proxySettings);
            }
            emClient = emClientBuilder.build();
            legacyClient = AnypointClientBuilder.buildClient(emClient.getAnypointBearerToken(), settings, anypointPlatformUrl);
            anypointClient = AnypointClient.builder()
                    .authenticationHandler(new BearerTokenAuthenticationHandler(emClient.getAnypointBearerToken()))
                    .proxy(proxySettings)
                    .anypointUrl(anypointPlatformUrl).build();
            logger.info("Initializing Enhanced Mule Tools");
        } catch (IOException | ProfileNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        try {
            doExecute();
        } catch (MojoFailureException | MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            if (this.legacyClient != null) {
                IOUtils.close(this.legacyClient);
            }
        }
    }

    protected abstract void doExecute() throws Exception;

    protected String getProperty(String name) {
        String property = session.getUserProperties().getProperty(name);
        if (property == null) {
            property = project.getProperties().getProperty(name);
        }
        return property;
    }

    public Map<String, String> getMavenProperties() {
        Map<String, String> results = new HashMap<>();
        project.getProperties().forEach((key, val) -> results.put(String.valueOf(key), String.valueOf(val)));
        session.getUserProperties().forEach((key, val) -> results.put(String.valueOf(key), String.valueOf(val)));
        return results;
    }

    public EMTProperties getEMTProperties() throws NotFoundException, IOException, ProfileNotFoundException {
        return new EMTProperties(getMavenProperties(), null, null, null);
    }

    protected Map<String, String> findPrefixedProperties(String... prefixes) {
        HashMap<String, String> results = new HashMap<>();
        for (String prefix : prefixes) {
            if (project != null) {
                results.putAll(findPrefixedProperties(project.getProperties(), prefix));
            }
            results.putAll(findPrefixedProperties(session.getUserProperties(), prefix));
        }
        return results;
    }

    protected static Map<String, String> findPrefixedProperties(Properties source, String prefix) {
        HashMap<String, String> results = new HashMap<>();
        for (Map.Entry<Object, Object> entry : source.entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith(prefix)) {
                key = key.substring(prefix.length());
                if (StringUtils.isNotBlank(key)) {
                    String value = entry.getValue().toString();
                    results.put(key, value);
                }
            }
        }
        return results;
    }

}
