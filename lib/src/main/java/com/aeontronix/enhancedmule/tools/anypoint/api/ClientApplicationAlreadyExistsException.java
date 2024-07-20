/*
 * Copyright (c) 2024. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.anypoint.api;

public class ClientApplicationAlreadyExistsException extends Exception {
    public ClientApplicationAlreadyExistsException() {
    }

    public ClientApplicationAlreadyExistsException(String message) {
        super(message);
    }

    public ClientApplicationAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientApplicationAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public ClientApplicationAlreadyExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
