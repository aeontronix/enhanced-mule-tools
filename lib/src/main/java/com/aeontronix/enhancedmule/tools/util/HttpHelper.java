/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.authentication.EMAccessTokens;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProvider;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProviderUsernamePasswordImpl;
import com.aeontronix.commons.Base64;
import com.aeontronix.commons.ThreadUtils;
import com.aeontronix.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpHelper implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(HttpHelper.class);
    private static final String HEADER_AUTH = "Authorization";
    public static final String CONTENT_TYPE = "Content-Type";
    private EMHttpClient httpClient;
    private AuthenticationProvider authenticationProvider;
    private EMAccessTokens authToken;
    private JsonHelper jsonHelper;
    private int maxRetries = 4;
    private boolean loginRequest = false;
    private int loginAttempts = 0;
    private long retryDelay = 1000L;

    public HttpHelper() {
    }

    public HttpHelper(JsonHelper jsonHelper, AuthenticationProvider authenticationProvider) {
        this.jsonHelper = jsonHelper;
        this.httpClient = authenticationProvider.createHttpClient();
        this.authenticationProvider = authenticationProvider;
    }

    private static void setHeader(Map<String, String> headers, HttpRequestBase method) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            method.setHeader(entry.getKey(), entry.getValue());
        }
    }

    protected static String convertPath(String path) {
        return path.startsWith("/") ? "https://anypoint.mulesoft.com" + path : path;
    }

    @NotNull
    private static HashMap<String, String> createOrgAndOwnerHeaders(String orgId, String ownerId) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("x-organization-id", orgId);
        headers.put("x-owner-id", ownerId);
        return headers;
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    public EMAccessTokens getAuthToken() throws HttpException {
        if (authToken == null) {
            this.authToken = authenticationProvider.getBearerToken(this);
        }
        return this.authToken;
    }

    public void setAuthToken(EMAccessTokens authToken) {
        this.authToken = authToken;
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.httpClient = authenticationProvider.createHttpClient();
    }

    public void httpGetBasicAuth(String path, OutputStream outputStream) throws HttpException {
        logger.debug("HTTP GET W/ BASIC AUTH: " + path);
        HttpGet request = new HttpGet(convertPath(path));
        setBasicAuthHeader(request);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            verifyStatusCode(request, response);
            if (response.getEntity() == null) {
                throw new HttpException("Not body returned by url " + request.getURI());
            }
            IOUtils.copy(response.getEntity().getContent(), outputStream);
        } catch (IOException e) {
            throw new HttpException(e.getMessage(), e);
        }
    }

    public String anypointHttpGet(String path, Environment env) throws HttpException {
        logger.debug("HTTP GET " + path + " env=" + env);
        return executeAnypointWithEnv(new HttpGet(convertPath(path)), env);
    }

    public String httpGet(String path) throws HttpException {
        logger.debug("HTTP GET " + path);
        return executeWrapper(new HttpGet(convertPath(path)), null);
    }

    public String httpGet(String path, Map<String, String> headers) throws HttpException {
        logger.debug("HTTP GET " + path);
        HttpGet method = new HttpGet(convertPath(path));
        setHeader(headers, method);
        return executeWrapper(method, null);
    }

    public String anypointHttpGetWithOrgAndOwner(String path, String orgId, String ownerId) throws HttpException {
        logger.debug("HTTP GET " + path);
        return httpGet(path, createOrgAndOwnerHeaders(orgId, ownerId));
    }

    public String anypointHttpPost(String path, Object data, Environment env) throws HttpException {
        logger.debug("HTTP POST " + path + " env=" + env + " data=" + data);
        return executeAnypointWithDataAndEnv(new HttpPost(convertPath(path)), data, env);
    }

    public String anypointHttpPostWithOrgAndOwner(String path, Object data, String orgId, String ownerId) throws HttpException {
        return httpPost(path, data, createOrgAndOwnerHeaders(orgId, ownerId));
    }

    public String httpPost(String path, Object data, Map<String, String> headers) throws HttpException {
        logger.debug("HTTP POST " + path + " headers=" + headers + " data=" + data);
        HttpPost method = new HttpPost(convertPath(path));
        setHeader(headers, method);
        return execute(method, data);
    }

    public String httpPost(String path, Object data) throws HttpException {
        logger.debug("HTTP POST " + path + " data=" + data);
        return execute(new HttpPost(convertPath(path)), data);
    }

    public String httpPatch(String path, Object data) throws HttpException {
        logger.debug("HTTP PATCH " + path + " data=" + data);
        return execute(new HttpPatch(convertPath(path)), data);
    }

    public String httpPut(String path, Object data) throws HttpException {
        logger.debug("HTTP PUT " + path + " data=" + data);
        return execute(new HttpPut(convertPath(path)), data);
    }

    public String httpPut(String path, Map<String,String> headers, Object data) throws HttpException {
        logger.debug("HTTP PUT " + path + " data=" + data);
        final HttpPut method = new HttpPut(convertPath(path));
        setHeader(headers, method);
        return execute(method, data);
    }

    public String anypointHttpPut(String path, Object data, Environment environment) throws HttpException {
        logger.debug("HTTP PUT " + path);
        return executeAnypointWithDataAndEnv(new HttpPut(convertPath(path)), data, environment);
    }

    public String httpDelete(String path) throws HttpException {
        logger.debug("HTTP DELETE " + path);
        final HttpDelete method = new HttpDelete(convertPath(path));
        return executeWrapper(method, null);
    }

    public String httpDelete(String path, Object data) throws HttpException {
        logger.debug("HTTP DELETE " + path + " data=" + data);
        return execute(new HttpDeleteWithBody(convertPath(path)), data);
    }

    public String anypointHttpDelete(@NotNull String path, @NotNull Environment env) throws HttpException {
        logger.debug("HTTP DELETE " + path + " env=" + env);
        return executeAnypointWithEnv(new HttpDelete(convertPath(path)), env);
    }

    private void addEnvironmentHeaders(@Nullable Environment environment, HttpRequestBase request) {
        if (environment != null) {
            environment.addHeaders(request);
        }
    }

    public MultiPartRequest createAnypointMultiPartPostRequest(@NotNull String url, @Nullable Environment environment) {
        HttpPost request = new HttpPost(convertPath(url));
        addEnvironmentHeaders(environment, request);
        return new MultiPartRequest(request);
    }

    public MultiPartRequest createAnypointMultiPartPatchRequest(@NotNull String url, @Nullable Environment environment) {
        HttpPatch request = new HttpPatch(convertPath(url));
        addEnvironmentHeaders(environment, request);
        return new MultiPartRequest(request);
    }

    public MultiPartRequest createAnypointMultiPartPutRequest(@NotNull String url, @Nullable Environment environment) {
        HttpPut request = new HttpPut(convertPath(url));
        addEnvironmentHeaders(environment, request);
        return new MultiPartRequest(request);
    }

    private String execute(@NotNull HttpEntityEnclosingRequestBase method, Object data) throws HttpException {
        if (data != null) {
            if (data instanceof HttpEntity) {
                method.setEntity((HttpEntity) data);
            } else {
                if( method.getFirstHeader(CONTENT_TYPE) == null ) {
                    method.setHeader(CONTENT_TYPE, "application/json");
                }
                method.setEntity(new ByteArrayEntity(jsonHelper.toJson(data)));
            }
        }
        return executeWrapper(method, null);
    }

    private String executeAnypointWithDataAndEnv(@NotNull HttpEntityEnclosingRequestBase method, @NotNull Object data, @NotNull Environment env) throws HttpException {
        env.addHeaders(method);
        return execute(method, data);
    }

    private String executeAnypointWithEnv(@NotNull HttpRequestBase method, @NotNull Environment env) throws HttpException {
        env.addHeaders(method);
        return executeWrapper(method, null);
    }

    protected String executeWrapper(@NotNull HttpRequestBase method, MultiPartRequest multiPartRequest) throws HttpException {
        return executeWrapper(method, multiPartRequest, 0);
    }

    private String executeWrapper(@NotNull HttpRequestBase method, MultiPartRequest multiPartRequest, int attempt) throws HttpException {
        if (isLoginRequest()) {
            loginAttempts++;
        }
        try {
            if (multiPartRequest != null) {
                ((HttpEntityEnclosingRequestBase) method).setEntity(multiPartRequest.toEntity());
            }
            return doExecute(method);
        } catch (HttpException e) {
            if (e.getStatusCode() == 403 || e.getStatusCode() == 401) {
                if (loginAttempts > 1) {
                    throw e;
                } else {
                    updateBearerToken();
                    return doExecute(method);
                }
            } else if (e.getStatusCode() >= 500) {
                attempt++;
                if (attempt > maxRetries) {
                    throw e;
                } else {
                    ThreadUtils.sleep(retryDelay);
                    return executeWrapper(method, multiPartRequest, attempt);
                }
            } else {
                throw e;
            }
        }
    }

    private void updateBearerToken() throws HttpException {
        authToken = authenticationProvider.getBearerToken(this);
    }

    @Nullable
    private synchronized String doExecute(HttpRequestBase method) throws HttpException {
        if (authToken != null && method.getFirstHeader(HEADER_AUTH) == null) {
            method.setHeader(HEADER_AUTH, "bearer " + authToken.getAnypointAccessToken());
        }
        try (CloseableHttpResponse response = httpClient.execute(method)) {
            verifyStatusCode(method, response);
            if (isLoginRequest()) {
                loginAttempts = 0;
                setLoginRequest(false);
            }
            if (response.getEntity() != null && response.getEntity().getContent() != null) {
                String resStr = IOUtils.toString(response.getEntity().getContent());
                logger.debug("RESULT CONTENT: " + resStr);
                return resStr;
            } else {
                return null;
            }
        } catch (IOException e) {
            if(e instanceof HttpException) {
                throw (HttpException)e;
            }
            throw new RuntimeIOException(e);
        }
    }

    private void verifyStatusCode(HttpRequestBase method, CloseableHttpResponse response) throws IOException, HttpException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 200 || statusCode > 299) {
            String errMsg;
            if (response.getEntity() != null && response.getEntity().getContent() != null) {
                errMsg = " : " + IOUtils.toString(response.getEntity().getContent());
            } else {
                errMsg = "";
            }
            String message = "Anypoint returned status code " + statusCode + " - url: " + method.getURI() + " - err: " + errMsg;
            if (statusCode == 403) {
                throw new UnauthorizedHttpException(message, statusCode);
            } else {
                throw new HttpException(message, statusCode);
            }
        }
    }

    public synchronized void setProxy(@NotNull String scheme, @NotNull String host, int port,
                                      @Nullable String username, @Nullable String password) {
        if (httpClient != null) {
            IOUtils.close(httpClient);
        }
        httpClient = authenticationProvider.createHttpClient(new HttpHost(host, port, scheme), username, password);
    }

    public synchronized void unsetProxy() {
        if (httpClient != null) {
            IOUtils.close(httpClient);
        }
        httpClient = authenticationProvider.createHttpClient();
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }

    public boolean isLoginRequest() {
        return loginRequest;
    }

    public void setLoginRequest(boolean loginRequest) {
        this.loginRequest = loginRequest;
    }

    private HttpRequestBase setBasicAuthHeader(HttpRequestBase request) {
        if (authenticationProvider instanceof AuthenticationProviderUsernamePasswordImpl) {
            String authStr = ((AuthenticationProviderUsernamePasswordImpl) authenticationProvider).getUsername() +
                    ":" + ((AuthenticationProviderUsernamePasswordImpl) authenticationProvider).getPassword();
            byte[] encodedAuth = Base64.encodeBase64(authStr.getBytes(StandardCharsets.ISO_8859_1));
            String authHeader = "Basic " + new String(encodedAuth);
            request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            return request;
        } else {
            throw new RuntimeException("Only supported with username/password authentication at this time");
        }
    }

    public class MultiPartRequest {
        private Map<String, Object> parts = new HashMap<>();
        private HttpEntityEnclosingRequestBase request;

        MultiPartRequest(HttpEntityEnclosingRequestBase request) {
            this.request = request;
        }

        public MultiPartRequest addText(@NotNull String name, @NotNull String value) {
            parts.put(name, value);
            return this;
        }

        public MultiPartRequest addBinary(@NotNull String name, @NotNull StreamSource streamSource) {
            parts.put(name, streamSource);
            return this;
        }

        HttpEntity toEntity() throws HttpException {
            try {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                for (Map.Entry<String, Object> e : parts.entrySet()) {
                    if (e.getValue() instanceof String) {
                        builder.addTextBody(e.getKey(), (String) e.getValue());
                    } else if (e.getValue() instanceof StreamSource) {
                        builder.addBinaryBody(e.getKey(), ((StreamSource) e.getValue()).createInputStream(),
                                ContentType.APPLICATION_OCTET_STREAM, ((StreamSource) e.getValue()).getFileName());
                    }
                }
                return builder.build();
            } catch (IOException e) {
                throw new HttpException("Failed to read data to send: " + e.getMessage(), e);
            }
        }

        public String execute() throws HttpException, IOException {
            try {
                logger.debug("HTTP {}", request);
                return HttpHelper.this.executeWrapper(request, this);
            } catch (RuntimeIOException e) {
                throw e.getIOException();
            }
        }
    }

    public class RuntimeIOException extends RuntimeException {
        RuntimeIOException(@NotNull IOException ioException) {
            super(ioException.getMessage(), ioException);
        }

        @NotNull
        IOException getIOException() {
            return (IOException) getCause();
        }
    }
}
