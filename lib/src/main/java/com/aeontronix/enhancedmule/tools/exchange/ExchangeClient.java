/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.exchange;

import com.aeontronix.enhancedmule.tools.util.restclient.RESTClient;
import com.aeontronix.commons.URLBuilder;

import java.io.IOException;
import java.io.InputStream;

public class ExchangeClient {
    private RESTClient restClient;
    private String exchangeMavenUrl;

    public ExchangeClient(RESTClient restClient, String exchangeMavenUrl) {
        this.restClient = restClient;
        this.exchangeMavenUrl = exchangeMavenUrl;
    }

    public InputStream getAsset(String path) throws IOException {
        return restClient.get(new URLBuilder(exchangeMavenUrl).path("/api/v2/maven").path(path).toString()).executeReturnStream();
    }
}
