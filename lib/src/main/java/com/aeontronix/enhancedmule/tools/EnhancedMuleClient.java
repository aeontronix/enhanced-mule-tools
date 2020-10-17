/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProviderBearerTokenImpl;
import com.aeontronix.enhancedmule.tools.config.AnypointBearerTokenCredentialsProvider;
import com.aeontronix.enhancedmule.tools.config.CredentialsProvider;
import com.aeontronix.enhancedmule.tools.exchange.ExchangeClient;
import com.aeontronix.enhancedmule.tools.util.restclient.RESTAuthenticationProvider;
import com.aeontronix.enhancedmule.tools.util.restclient.RESTClient;
import com.aeontronix.enhancedmule.tools.util.restclient.RESTClientJsonParserJacksonImpl;
import com.aeontronix.commons.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import java.io.Closeable;
import java.io.IOException;

public class EnhancedMuleClient implements Closeable, AutoCloseable {
    private RESTClient restClient;
    private CredentialsProvider credentialsProvider;
    private String anypointPlatformUrl = "https://anypoint.mulesoft.com/";
    private String exchangeMavenBaseUrl = "https://maven.anypoint.mulesoft.com";
    private String exchangeMavenPath = "/api/v2/maven";
    private ExchangeClient exchangeClient;

    public EnhancedMuleClient() {
        this("https://api.enhanced-mule.com");
    }

    public EnhancedMuleClient(String serverUrl) {
        restClient = new RESTClient(new RESTClientJsonParserJacksonImpl(), null, null, null);
        restClient.setBaseUrl(serverUrl);
        restClient.addAuthProvider(new MavenAuthenticationProvider());
        exchangeClient = new ExchangeClient(restClient, exchangeMavenBaseUrl);
    }

    public void setProxy(HttpHost proxyHost, String proxyUsername, String proxyPassword) {
        restClient.setProxy(proxyHost, proxyUsername, proxyPassword);
    }

    public ExchangeClient getExchangeClient() {
        return exchangeClient;
    }

    public RESTClient getRestClient() {
        return restClient;
    }

    @Override
    public void close() throws IOException {
        restClient.close();
    }

    public CredentialsProvider getCredentialsLoader() {
        return credentialsProvider;
    }

    public void setCredentialsLoader(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public AnypointClient getAnypointClient() throws IOException {
        if (credentialsProvider instanceof AnypointBearerTokenCredentialsProvider) {
            final String anypointBearerToken = ((AnypointBearerTokenCredentialsProvider) credentialsProvider).getAnypointBearerToken(this);
            return new AnypointClient(new AuthenticationProviderBearerTokenImpl(anypointBearerToken));
        } else {
            throw new RuntimeException("Not implemented yet");
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
            return null;
        }
    }

    public class MavenAuthenticationProvider implements RESTAuthenticationProvider {
        @Override
        public boolean handles(HttpRequest req) {
            return (req instanceof HttpRequestWrapper && ((HttpRequestWrapper) req).getTarget().toString().startsWith(exchangeMavenBaseUrl)) ||
                    (req instanceof HttpUriRequest && ((HttpUriRequest) req).getURI().toString().startsWith(exchangeMavenBaseUrl));
        }

        @Override
        public void process(HttpRequest req, HttpContext httpContext) throws IOException {
            String authStr = "~~~Token~~~:" + getAnypointBearerToken();
            req.addHeader(new BasicHeader("Authorization", "Basic "+StringUtils.base64Encode(authStr.getBytes())));
        }
    }
}
