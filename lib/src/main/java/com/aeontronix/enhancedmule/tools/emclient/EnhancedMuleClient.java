/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.emclient;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.URLBuilder;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProvider;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProviderBearerTokenImpl;
import com.aeontronix.enhancedmule.tools.config.ConfigProfile;
import com.aeontronix.enhancedmule.tools.emclient.authentication.AnypointBearerTokenCredentialsProvider;
import com.aeontronix.enhancedmule.tools.emclient.authentication.CredentialsProvider;
import com.aeontronix.enhancedmule.tools.exchange.ExchangeClient;
import com.aeontronix.enhancedmule.tools.util.AnypointAccessToken;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.enhancedmule.tools.util.restclient.RESTAuthenticationProvider;
import com.aeontronix.enhancedmule.tools.util.restclient.RESTClient;
import com.aeontronix.enhancedmule.tools.util.restclient.RESTClientJsonParserJacksonImpl;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

public class EnhancedMuleClient implements Closeable, AutoCloseable {
    public static final String EMULE_SERVER_URL = "https://api.enhanced-mule.com";
    private RESTClient restClient;
    private CredentialsProvider credentialsProvider;
    private String anypointPlatformUrl = "https://anypoint.mulesoft.com/";
    private String exchangeMavenBaseUrl = "https://maven.anypoint.mulesoft.com";
    private String exchangeMavenPath = "/api/v2/maven";
    private ExchangeClient exchangeClient;
    private String serverUrl;
    private String publicServerUrl;
    private ConfigProfile configProfile;

    public EnhancedMuleClient(ConfigProfile configProfile) {
        this(EMULE_SERVER_URL, configProfile);
    }

    public EnhancedMuleClient(String serverUrl, ConfigProfile configProfile) {
        this.configProfile = configProfile;
        restClient = new RESTClient(new RESTClientJsonParserJacksonImpl(), null, null, null);
        this.serverUrl = serverUrl;
        restClient.setBaseUrl(this.serverUrl);
        publicServerUrl = new URLBuilder(this.serverUrl).path("public").toString();
        restClient.addAuthProvider(new EMAccessTokenProvider());
        restClient.addAuthProvider(new MavenAuthenticationProvider());
        exchangeClient = new ExchangeClient(restClient, exchangeMavenBaseUrl);
    }

    public void setProxy(HttpHost proxyHost, String proxyUsername, String proxyPassword) {
        restClient.setProxy(proxyHost, proxyUsername, proxyPassword);
    }

    public ConfigProfile getConfigProfile() {
        return configProfile;
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
            if (true) {
                throw new RuntimeException("not implemented");
            }
            return new AnypointClient(new AuthenticationProvider() {
                @Override
                public AnypointAccessToken getBearerToken(HttpHelper httpHelper) throws HttpException {
                    return null;
                }

                @Override
                public String filterSecret(String resStr) {
                    return null;
                }
            });
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
            return restClient.get("/anypoint/bearer").execute(String.class);
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
            req.addHeader(new BasicHeader("Authorization", "Basic " + StringUtils.base64Encode(authStr.getBytes())));
        }
    }

    public class EMAccessTokenProvider implements RESTAuthenticationProvider {
        private String bearer;

        public EMAccessTokenProvider() {
        }

        @Override
        public boolean handles(HttpRequest req) {
            if (req instanceof HttpRequestWrapper) {
                final HttpRequestWrapper wrapper = (HttpRequestWrapper) req;
                return wrapper.getTarget().toString().startsWith(serverUrl) && !wrapper.getURI().toString().startsWith("/public");
            } else if (req instanceof HttpUriRequest) {
                final String url = ((HttpUriRequest) req).getURI().toString();
                return url.startsWith(serverUrl) && !url.startsWith(publicServerUrl);
            } else {
                return false;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void process(HttpRequest req, HttpContext httpContext) throws IOException {
            if (bearer == null) {
                final Map<String, String> authResult = restClient.postJson("/public/auth", credentialsProvider.getCredentials().toAuthRequestPayload()).execute(Map.class);
                bearer = authResult.get("accessToken");
                if (StringUtils.isBlank(bearer)) {
                    throw new IOException("No access token return by authentication");
                }
            }
            req.addHeader(new BasicHeader("Authorization", "Bearer " + bearer));
        }
    }
}
