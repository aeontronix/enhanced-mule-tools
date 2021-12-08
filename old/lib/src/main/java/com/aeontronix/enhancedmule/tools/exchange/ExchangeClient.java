/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.exchange;

import com.aeontronix.commons.URLBuilder;
import com.aeontronix.enhancedmule.tools.util.restclient.RESTClient;
import com.aeontronix.enhancedmule.tools.util.restclient.ResponseStream;

import java.io.IOException;

public class ExchangeClient {
    public static final String MAVENBASEPATH = "/api/v2/maven";
    private RESTClient restClient;
    private String exchangeMavenUrl;

    public ExchangeClient(RESTClient restClient, String exchangeMavenUrl) {
        this.restClient = restClient;
        this.exchangeMavenUrl = exchangeMavenUrl;
    }

    public ResponseStream getAsset(String groupId, String artifactId, String version, String classifier, String extension) throws IOException {
        StringBuilder filename = new StringBuilder(artifactId).append('-').append(version);
        if (classifier != null) {
            filename.append('-').append(classifier);
        }
        filename.append('.').append(extension);
        final String url = new URLBuilder(exchangeMavenUrl).path(MAVENBASEPATH).path(groupId).path(artifactId).path(version).path(filename.toString()).toString();
        return restClient.get(url).executeReturnStream();
    }

    public ResponseStream getAsset(String path) throws IOException {
        return restClient.get(new URLBuilder(exchangeMavenUrl).path(MAVENBASEPATH).path(path).toString()).executeReturnStream();
    }
}
