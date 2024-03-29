/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.restclient.RESTException;

import java.io.IOException;

public class HttpException extends IOException {
    protected int statusCode;

    public HttpException() {
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
        if (cause instanceof RESTException) {
            statusCode = ((RESTException) cause).getStatusCode();
        }
    }

    public HttpException(Throwable cause) {
        super(cause.getMessage(), cause);
        if (cause instanceof RESTException) {
            statusCode = ((RESTException) cause).getStatusCode();
        }
    }

    public HttpException(int statusCode) {
        this("Server returned status code " + statusCode);
        this.statusCode = statusCode;
    }

    public HttpException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public HttpException(Throwable cause, int statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
