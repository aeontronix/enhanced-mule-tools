/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.emclient;

import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProviderBearerTokenImpl;
import com.aeontronix.enhancedmule.tools.emclient.authentication.AnypointBearerTokenCredentialsProvider;
import com.aeontronix.enhancedmule.tools.emclient.authentication.CredentialsProvider;
import com.aeontronix.enhancedmule.tools.util.UnauthorizedHttpException;
import com.aeontronix.restclient.ProxySettings;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.RESTClientHost;
import com.aeontronix.restclient.RESTClientHostBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

public class EnhancedMuleClient implements Closeable, AutoCloseable {
    public static final String EMULE_SERVER_URL = "https://api.enhanced-mule.com";
    private CredentialsProvider credentialsProvider;
    private String anypointPlatformUrl = "https://anypoint.mulesoft.com/";
    private String exchangeMavenBaseUrl = "https://maven.anypoint.mulesoft.com";
    private String exchangeMavenPath = "/api/v2/maven";
    private RESTClientHost anypointRestClient;
    private String serverUrl;
    private ProxySettings proxySettings;
    private String publicServerUrl;
    private ConfigProfile configProfile;
    private com.aeontronix.restclient.RESTClient restClient;
    private RESTClientHost serverRestClient;

    public EnhancedMuleClient(ConfigProfile configProfile, ProxySettings proxySettings) {
        this(EMULE_SERVER_URL, configProfile, proxySettings);
    }

    public EnhancedMuleClient(String serverUrl, ConfigProfile configProfile, ProxySettings proxySettings) {
        this.configProfile = configProfile;
        this.serverUrl = serverUrl;
        this.proxySettings = proxySettings;
        initRestClient();
        publicServerUrl = new URLBuilder(this.serverUrl).path("public").toString();
    }

    private void initRestClient() {
        restClient = RESTClient.builder().proxy(proxySettings).build();
        final RESTClientHostBuilder anypointRestClientBuilder = restClient.host(this.anypointPlatformUrl);
        if (credentialsProvider != null) {
            anypointRestClientBuilder.authenticationHandler(credentialsProvider.toAuthenticationHandler(restClient, this.anypointPlatformUrl));
        }
        anypointRestClient = anypointRestClientBuilder.build();
        serverRestClient = restClient.host(this.serverUrl).build();
    }

    public ConfigProfile getConfigProfile() {
        return configProfile;
    }

    public RESTClient getRestClient() {
        return restClient;
    }

    public RESTClientHost getAnypointRestClient() {
        return anypointRestClient;
    }

    @Override
    public void close() throws IOException {
        IOUtils.close(restClient);
    }

    public CredentialsProvider getCredentialsLoader() {
        return credentialsProvider;
    }

    public void setCredentialsLoader(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        initRestClient();
    }

    public LegacyAnypointClient getLegacyAnypointClient() throws IOException {
        if (credentialsProvider instanceof AnypointBearerTokenCredentialsProvider) {
            final String anypointBearerToken = ((AnypointBearerTokenCredentialsProvider) credentialsProvider).getAnypointBearerToken(this);
            final LegacyAnypointClient anypointClient = new LegacyAnypointClient(new AuthenticationProviderBearerTokenImpl(anypointBearerToken));
            if (proxySettings != null) {
                final URI proxyUri = proxySettings.getProxyUri();
                anypointClient.setProxy(proxyUri.getScheme(), proxyUri.getHost(), proxyUri.getPort(),
                        proxySettings.getProxyUsername(), proxySettings.getProxyPassword());
            }
            return anypointClient;
        } else {
            throw new RuntimeException("not implemented");
        }
    }

    public String getAnypointPlatformUrl() {
        return anypointPlatformUrl;
    }

    public void setAnypointPlatformUrl(String anypointPlatformUrl) {
        this.anypointPlatformUrl = anypointPlatformUrl;
    }

    public String getExchangeMavenUrl() {
        return exchangeMavenBaseUrl + exchangeMavenPath;
    }

    public String getExchangeMavenBaseUrl() {
        return exchangeMavenBaseUrl;
    }

    public void setExchangeMavenBaseUrl(String exchangeMavenBaseUrl) {
        this.exchangeMavenBaseUrl = exchangeMavenBaseUrl;
    }

    public String getAnypointBearerToken() throws IOException {
        if (credentialsProvider instanceof AnypointBearerTokenCredentialsProvider) {
            return ((AnypointBearerTokenCredentialsProvider) credentialsProvider).getAnypointBearerToken(this);
        } else {
            throw new UnauthorizedHttpException("Not supported");
        }
    }

}
