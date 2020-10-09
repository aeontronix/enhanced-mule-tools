/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.exchange;

public class AssetCreationException extends Exception {
    public AssetCreationException() {
    }

    public AssetCreationException(String message) {
        super(message);
    }

    public AssetCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AssetCreationException(Throwable cause) {
        super(cause);
    }

    public AssetCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
