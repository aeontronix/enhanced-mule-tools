/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.emclient;

import com.aeontronix.commons.io.IOUtils;
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
    public static final String EM_CLOUD_SERVER_URL = "https://api.enhanced-mule.com";
    public static final String DEFAULT_ANYPOINT_URL = "https://anypoint.mulesoft.com/";
    private final String anypointUrl;
    private CredentialsProvider credentialsProvider;
    private String exchangeMavenBaseUrl = "https://maven.anypoint.mulesoft.com";
    private String exchangeMavenPath = "/api/v2/maven";
    private RESTClientHost anypointRestClient;
    private String serverUrl;
    private ProxySettings proxySettings;
    private com.aeontronix.restclient.RESTClient restClient;
    private RESTClientHost serverRestClient;

    private EnhancedMuleClient(String serverUrl, ProxySettings proxySettings, String anypointUrl,
                               CredentialsProvider credentialsProvider, boolean insecureServer) {
        this.serverUrl = serverUrl;
        this.proxySettings = proxySettings;
        this.credentialsProvider = credentialsProvider;
        restClient = RESTClient.builder()
                .insecure(insecureServer)
                .proxy(this.proxySettings).build();
        if (anypointUrl == null) {
            this.anypointUrl = DEFAULT_ANYPOINT_URL;
        } else {
            this.anypointUrl = anypointUrl;
        }
        final RESTClientHostBuilder anypointRestClientBuilder = restClient.host(this.anypointUrl);
        if (this.credentialsProvider != null) {
            anypointRestClientBuilder.authenticationHandler(this.credentialsProvider.toAuthenticationHandler(restClient, this.anypointUrl));
        }
        anypointRestClient = anypointRestClientBuilder.build();
        serverRestClient = restClient.host(this.serverUrl).build();
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

    public LegacyAnypointClient getLegacyAnypointClient() throws IOException {
        if (credentialsProvider instanceof AnypointBearerTokenCredentialsProvider) {
            final String anypointBearerToken = ((AnypointBearerTokenCredentialsProvider) credentialsProvider).getAnypointBearerToken(this);
            final LegacyAnypointClient anypointClient = new LegacyAnypointClient(new AuthenticationProviderBearerTokenImpl(anypointBearerToken), anypointUrl);
            if (proxySettings != null) {
                final URI proxyUri = proxySettings.getProxyUri();
                anypointClient.setProxy(proxyUri.getScheme(), proxyUri.getHost(), proxyUri.getPort(),
                        proxySettings.getProxyUsername(), proxySettings.getProxyPassword());
            }
            return anypointClient;
        } else {
            throw new RuntimeException("Credentials provider is not a AnypointBearerTokenCredentialsProvider");
        }
    }

    public String getAnypointPlatformUrl() {
        return anypointUrl;
    }

    public String getExchangeMavenUrl() {
        return exchangeMavenBaseUrl + exchangeMavenPath;
    }

    public String getExchangeMavenBaseUrl() {
        return exchangeMavenBaseUrl;
    }

    public String getAnypointBearerToken() throws IOException {
        if (credentialsProvider instanceof AnypointBearerTokenCredentialsProvider) {
            return ((AnypointBearerTokenCredentialsProvider) credentialsProvider).getAnypointBearerToken(this);
        } else {
            throw new UnauthorizedHttpException("Not supported");
        }
    }

    public static Builder builder(CredentialsProvider credentialsProvider) {
        return new Builder(credentialsProvider);
    }

    public static class Builder {
        private ProxySettings proxySettings;
        private String anypointUrl;
        private String serverUrl = EM_CLOUD_SERVER_URL;
        private CredentialsProvider credentialsProvider;
        private boolean insecure;

        private Builder(CredentialsProvider credentialsProvider) {
            this.credentialsProvider = credentialsProvider;
        }

        public Builder proxySettings(ProxySettings proxySettings) {
            this.proxySettings = proxySettings;
            return this;
        }

        public Builder anypointUrl(String anypointUrl) {
            this.anypointUrl = anypointUrl;
            return this;
        }

        public Builder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public Builder insecure(boolean insecure) {
            this.insecure = insecure;
            return this;
        }

        public EnhancedMuleClient build() {
            return new EnhancedMuleClient(serverUrl, proxySettings, anypointUrl, credentialsProvider, insecure);
        }
    }
}
