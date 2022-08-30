/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.util.restclient;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.exception.UnexpectedException;
import com.aeontronix.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import org.apache.http.protocol.HttpContext;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class RESTClient implements Closeable, AutoCloseable {
    private final List<RESTAuthenticationProvider> authenticationProviders = new ArrayList<>();
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

    public void addAuthProvider(RESTAuthenticationProvider provider) {
        authenticationProviders.add(provider);
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
        builder.addInterceptorFirst(new AuthenticationFilter());
        httpClient = builder.build();
    }

    private String toUrl(String path) {
        if (path.startsWith("/")) {
            return new URLBuilder(baseUrl).path(path).toString();
        } else {
            return path;
        }
    }

    public GetBuilder get(String path) throws RESTException {
        return new GetBuilder(new HttpGet(toUrl(path)));
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

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    public interface HttpOperationBuilder {
    }

    public interface ResponseHandler {
        <X> X handleResponse(StatusLine statusLine, CloseableHttpResponse response) throws IOException;
    }

    public abstract class AbstractMethodBuilder implements HttpOperationBuilder {
        protected HttpRequestBase method;

        public AbstractMethodBuilder(HttpRequestBase method) {
            this.method = method;
        }

        protected <X> X execute(ResponseHandler responseHandler) throws RESTException {
            try {
                try (CloseableHttpResponse response = httpClient.execute(method)) {
                    final StatusLine statusLine = response.getStatusLine();
                    final int statusCode = statusLine.getStatusCode();
                    if (statusCode >= 200 && statusCode <= 299) {
                        return responseHandler.handleResponse(statusLine, response);
                    } else {
                        String reasonPhrase = statusLine.getReasonPhrase();
                        if( StringUtils.isBlank(reasonPhrase) ) {
                            reasonPhrase = "Received HTTP error code: "+ statusCode;
                        }
                        throw new RESTException(reasonPhrase, null, statusCode);
                    }
                }
            } catch (IOException e) {
                throw new RESTException(e, -1);
            }
        }

        protected ResponseStream executeReturnStreamInternal() throws IOException {
            CloseableHttpResponse response;
            try {
                response = httpClient.execute(method);
            } catch (IOException e) {
                throw new RESTException(e, -1);
            }
            ResponseStream is = null;
            final StatusLine statusLine = response.getStatusLine();
            final int statusCode = statusLine.getStatusCode();
            if (statusCode >= 200 && statusCode <= 299) {
                if (response.getEntity() != null && response.getEntity().getContent() != null) {
                    is = new ResponseStream(response);
                } else {
                    return null;
                }
            } else {
                IOUtils.close(response);
                throw new RESTException(statusLine.getReasonPhrase(), null, statusCode);
            }
            return is;
        }
    }

    public class GetBuilder extends AbstractMethodBuilder {
        public GetBuilder(HttpRequestBase method) {
            super(method);
        }

        public <X> X execute(Class<X> cl) throws IOException {
            return execute(new ResponseHandler() {
                @SuppressWarnings("unchecked")
                @Override
                public <X> X handleResponse(StatusLine statusLine, CloseableHttpResponse response) throws IOException {
                    final HttpEntity entity = response.getEntity();
                    if( entity != null ) {
                        return (X) jsonParser.parse(entity.getContent(),cl);
                    } else {
                        return null;
                    }
                }
            });
        }

        public ResponseStream executeReturnStream() throws IOException {
            return executeReturnStreamInternal();
        }
    }

    public class PostBuilder extends AbstractMethodBuilder {
        public PostBuilder(HttpRequestBase method) {
            super(method);
        }

        public <X> X execute(Class<X> clazz) throws IOException {
            return execute(new ResponseHandler() {
                @Override
                public <X> X handleResponse(StatusLine statusLine, CloseableHttpResponse response) throws IOException {
                    return (X) jsonParser.parse(response.getEntity().getContent(), clazz);
                }
            });
        }
    }

    public class AuthenticationFilter implements HttpRequestInterceptor {
        @Override
        public void process(HttpRequest req, HttpContext httpContext) throws HttpException, IOException {
            for (RESTAuthenticationProvider authenticationProvider : authenticationProviders) {
                if (authenticationProvider.handles(req)) {
                    authenticationProvider.process(req, httpContext);
                }
            }
        }
    }
}
