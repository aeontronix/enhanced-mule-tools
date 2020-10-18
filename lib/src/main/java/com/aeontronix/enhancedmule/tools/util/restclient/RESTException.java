/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.util.restclient;

import java.io.IOException;

public class RESTException extends IOException {
    private int statusCode;

    public RESTException(int statusCode) {
        this.statusCode = statusCode;
    }

    public RESTException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public RESTException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public RESTException(Throwable cause, int statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}