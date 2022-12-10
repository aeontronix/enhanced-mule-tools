/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.application;

public class ApplicationSourceEnhancementException extends Exception {
    public ApplicationSourceEnhancementException() {
    }

    public ApplicationSourceEnhancementException(String message) {
        super(message);
    }

    public ApplicationSourceEnhancementException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationSourceEnhancementException(Throwable cause) {
        super(cause);
    }

    public ApplicationSourceEnhancementException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
