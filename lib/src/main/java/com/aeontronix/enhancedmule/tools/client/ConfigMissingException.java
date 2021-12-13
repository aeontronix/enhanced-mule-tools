/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.client;

public class ConfigMissingException extends RuntimeException {
    public ConfigMissingException() {
    }

    public ConfigMissingException(String message) {
        super(message);
    }

    public ConfigMissingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigMissingException(Throwable cause) {
        super(cause);
    }

    public ConfigMissingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
