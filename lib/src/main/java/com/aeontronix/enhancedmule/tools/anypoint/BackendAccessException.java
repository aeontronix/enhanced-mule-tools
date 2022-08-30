/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.anypoint;

public class BackendAccessException extends RuntimeException {
    public BackendAccessException() {
    }

    public BackendAccessException(String message) {
        super(message);
    }

    public BackendAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BackendAccessException(Throwable cause) {
        super(cause);
    }

    public BackendAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
