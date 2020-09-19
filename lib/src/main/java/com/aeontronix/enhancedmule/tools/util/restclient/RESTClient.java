/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.util.restclient;

import com.kloudtek.util.StringUtils;
import com.kloudtek.util.URLBuilder;
import com.kloudtek.util.UnexpectedException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HTTP;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class RESTClient implements Closeable, AutoCloseable {
    private CloseableHttpClient httpClient;
    private String baseUrl;
    private RESTClientJsonParser jsonParser;

    public RESTClient() {
        this(new RESTClientJsonParserJacksonImpl(), null, null, null);
    }

    public RESTClient(RESTClientJsonParser jsonParser, HttpHost proxyHost, String proxyUsername, String proxyPassword) {
        this.jsonParser = jsonParser;
        setProxy(proxyHost, proxyUsername, proxyPassword);
    }

    public void setProxy(HttpHost proxyHost, String proxyUsername, String proxyPassword) {
        HttpClientBuilder builder = HttpClients.custom().disableCookieManagement();
        if (proxyHost != null) {
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
            builder = builder.setRoutePlanner(routePlanner);
            if (StringUtils.isNotEmpty(proxyUsername) && StringUtils.isNotEmpty(proxyPassword)) {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(proxyHost), new UsernamePasswordCredentials(proxyUsername, proxyPassword));
                builder = builder.setDefaultCredentialsProvider(credsProvider);
            }
        }
        httpClient = builder.build();
    }

    private String toUrl(String path) {
        if (path.startsWith("/")) {
            return new URLBuilder(baseUrl).path(path).toString();
        } else {
            return path;
        }
    }

    public PostBuilder postJson(String path, Object entity) {
        try {
            final HttpPost method = new HttpPost(toUrl(path));
            method.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            String json = jsonParser.toJson(entity);
            method.setEntity(new StringEntity(json));
            return new PostBuilder(method);
        } catch (UnsupportedEncodingException e) {
            throw new UnexpectedException(e);
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public interface HttpOperationBuilder {
        <X> X execute(Class<X> clazz) throws IOException;
    }

    public class PostBuilder implements HttpOperationBuilder {
        private HttpRequestBase method;

        public PostBuilder(HttpRequestBase method) {
            this.method = method;
        }

        @Override
        public <X> X execute(Class<X> clazz) throws IOException {
            try (CloseableHttpResponse response = httpClient.execute(method)) {
                return jsonParser.parse(response.getEntity().getContent(), clazz);
            }
        }
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
