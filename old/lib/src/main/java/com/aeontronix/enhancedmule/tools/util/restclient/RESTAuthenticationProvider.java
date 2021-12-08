/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.util.restclient;

import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public interface RESTAuthenticationProvider {
    boolean handles(HttpRequest req);

    void process(HttpRequest req, HttpContext httpContext) throws IOException;
}
