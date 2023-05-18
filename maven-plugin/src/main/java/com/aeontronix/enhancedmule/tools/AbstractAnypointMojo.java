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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import static com.aeontronix.commons.StringUtils.isBlank;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractAnypointMojo extends AbstractMojo {
    private static final Logger logger = getLogger(AbstractAnypointMojo.class);
    public static final String DEFAULT_EMSERVER_URL = "https://api.enhanced-mule.com";
    public static final String EM_CLIENT = "emClient";
    @Parameter
    protected String authType;
    /**
     * Anypoint username
     */
    @Parameter
    protected String username;
    /**
     * Anypoint password
     */
    @Parameter
    protected String password;
    @Parameter
    protected String clientId;
    @Parameter
    protected String clientSecret;
    /**
     * Anypoint bearer token
     */
    @Parameter
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
    @Parameter
    protected String org;
    @Parameter(property = "profile")
    protected String profile;
    protected EnhancedMuleClient emClient;
    private LegacyAnypointClient legacyClient;
    private EMConfig emConfig;
    protected ConfigProfile configProfile;
    protected AnypointClient anypointClient;
    private AuthType authTypeEnum;

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
            initFields();
            if (logger.isDebugEnabled()) {
                logger.debug("Creating client");
                logger.debug("Server URL: {}", enhancedMuleServerUrl);
                logger.debug("Anypoint URL: {}", anypointPlatformUrl);
                logger.debug("Bearer: SHA:{}", bearerToken != null ? StringUtils.base64EncodeToString(DigestUtils.sha512(bearerToken.getBytes(UTF_8))) : "NOT SET");
                logger.debug("Username: {}", username != null ? username : "NOT SET");
                logger.debug("Password: SHA:{}", password != null ? StringUtils.base64EncodeToString(DigestUtils.sha512(password.getBytes(UTF_8))) : "NOT SET");
                logger.debug("Client Id: {}", clientId != null ? clientId : "NOT SET");
                logger.debug("Client Secret: SHA:{}", clientSecret != null ? StringUtils.base64EncodeToString(DigestUtils.sha512(clientSecret.getBytes(UTF_8))) : "NOT SET");
                logger.debug("Auth Type: {}", authTypeEnum != null ? authTypeEnum : "NOT SET");
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
            if (authTypeEnum != null) {
                switch (authTypeEnum) {
                    case BEARER:
                        logger.info("Using Bearer Token");
                        credentialsProvider = new CredentialsProviderAnypointBearerToken(bearerToken);
                        break;
                    case CLIENTCREDS:
                        logger.info("Using Client Credentials: {}", clientId);
                        credentialsProvider = new CredentialsProviderClientCredentialsImpl(clientId, clientSecret);
                        break;
                    case UPW:
                        logger.info("Using Username Password: {}", username);
                        credentialsProvider = new CredentialsProviderAnypointUsernamePasswordImpl(username, password);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid auth type enum: " + authTypeEnum);
                }
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
            legacyClient.setNewClient(anypointClient);
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

    private void initFields() {
        clientId = getMavenProperty("emt.auth.client.id", clientId, "anypoint.client.id");
        clientSecret = getMavenProperty("emt.auth.client.secret", clientSecret, "anypoint.client.secret");
        username = getMavenProperty("emt.auth.username", username, "anypoint.username");
        password = getMavenProperty("emt.auth.password", password, "anypoint.password");
        bearerToken = getMavenProperty("emt.auth.bearer", bearerToken, "anypoint.bearer");
        authType = getMavenProperty("emt.auth.type", authType);
        org = getMavenProperty("emt.org", org, "anypoint.org");
        if (StringUtils.isNotBlank(authType)) {
            try {
                authTypeEnum = AuthType.valueOf(authType.toUpperCase());
                switch (authTypeEnum) {
                    case UPW:
                        if (isBlank(username) || isBlank(password)) {
                            throw new IllegalArgumentException("Auth type upw specified but username and password aren't both set");
                        }
                        break;
                    case CLIENTCREDS:
                        if (isBlank(clientId) || isBlank(clientSecret)) {
                            throw new IllegalArgumentException("Auth type upw specified but clientId and clientSecret aren't both set");
                        }
                        break;
                    case BEARER:
                        if (isBlank(bearerToken)) {
                            throw new IllegalArgumentException("Auth type upw specified but bearerToken is not set");
                        }
                        break;
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Auth type value is invalid, must be one of " + Arrays.toString(AuthType.values()));
            }
        } else {
            if (StringUtils.isNotBlank(bearerToken)) {
                authTypeEnum = AuthType.BEARER;
            } else if (StringUtils.isNotBlank(clientId) && StringUtils.isNotBlank(clientSecret)) {
                authTypeEnum = AuthType.CLIENTCREDS;
            } else if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                authTypeEnum = AuthType.UPW;
            } else {
                authTypeEnum = null;
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

    @SuppressWarnings("Convert2Lambda")
    public Map<String, String> getMavenProperties() {
        Map<String, String> results = new HashMap<>();
        BiConsumer<Object, Object> converter = new BiConsumer<Object, Object>() {
            @Override
            public void accept(Object key, Object val) {
                results.put(String.valueOf(key), String.valueOf(val));
            }
        };
        project.getProperties().forEach(converter);
        session.getUserProperties().forEach(converter);
        return results;
    }


    public Boolean getMavenProperty(String key, @Nullable Boolean defValue, @NotNull String... legacyKeys) {
        String result = getMavenProperty(key, defValue != null ? defValue.toString() : null, legacyKeys);
        if (result != null) {
            return Boolean.valueOf(result);
        } else {
            return null;
        }
    }

    public String getMavenProperty(String key, @Nullable String defValue, @NotNull String... legacyKeys) {
        return EMTProperties.getProperty(getMavenProperties(), key, defValue, legacyKeys);
    }

    public EMTProperties getEMTProperties() throws NotFoundException, IOException, ProfileNotFoundException {
        return new EMTProperties(getMavenProperties(), null, null, null);
    }

    @NotNull
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
