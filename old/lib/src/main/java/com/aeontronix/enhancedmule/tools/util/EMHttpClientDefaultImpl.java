/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.util;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

public class EMHttpClientDefaultImpl implements EMHttpClient {
    private CloseableHttpClient httpClient;

    public EMHttpClientDefaultImpl(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public CloseableHttpResponse execute(HttpRequestBase request) throws IOException, ClientProtocolException {
        return httpClient.execute(request);
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
