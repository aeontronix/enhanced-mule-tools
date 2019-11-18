/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.util;

public class UnauthorizedHttpException extends HttpException {
    public UnauthorizedHttpException() {
    }

    public UnauthorizedHttpException(String message) {
        super(message);
    }

    public UnauthorizedHttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedHttpException(Throwable cause) {
        super(cause);
    }

    public UnauthorizedHttpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public UnauthorizedHttpException(int statusCode) {
        super(statusCode);
    }

    public UnauthorizedHttpException(String message, int statusCode) {
        super(message, statusCode);
    }

    public UnauthorizedHttpException(String message, Throwable cause, int statusCode) {
        super(message, cause, statusCode);
    }

    public UnauthorizedHttpException(Throwable cause, int statusCode) {
        super(cause, statusCode);
    }

    public UnauthorizedHttpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int statusCode) {
        super(message, cause, enableSuppression, writableStackTrace, statusCode);
    }
}
