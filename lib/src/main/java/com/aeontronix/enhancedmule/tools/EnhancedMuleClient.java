/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.util.restclient.RESTClient;
import com.aeontronix.enhancedmule.tools.util.restclient.RESTClientJsonParserJacksonImpl;
import org.apache.http.HttpHost;

import java.io.Closeable;
import java.io.IOException;

public class EnhancedMuleClient implements Closeable, AutoCloseable {
    private RESTClient restClient;

    public EnhancedMuleClient() {
        this("https://api.enhanced-mule.com");
    }

    public EnhancedMuleClient(String serverUrl) {
        restClient = new RESTClient(new RESTClientJsonParserJacksonImpl(), null, null, null);
        restClient.setBaseUrl(serverUrl);
    }

    public void setProxy(HttpHost proxyHost, String proxyUsername, String proxyPassword) {
        restClient.setProxy(proxyHost, proxyUsername, proxyPassword);
    }

    public RESTClient getRestClient() {
        return restClient;
    }

    @Override
    public void close() throws IOException {
        restClient.close();
    }
}
