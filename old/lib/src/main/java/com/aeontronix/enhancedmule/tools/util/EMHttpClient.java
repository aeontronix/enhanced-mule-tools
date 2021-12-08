/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.util;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.Closeable;
import java.io.IOException;

public interface EMHttpClient extends Closeable {
    CloseableHttpResponse execute(HttpRequestBase request) throws IOException, ClientProtocolException;
}
