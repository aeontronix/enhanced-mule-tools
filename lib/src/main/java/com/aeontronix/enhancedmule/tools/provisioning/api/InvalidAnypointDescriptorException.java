/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning.api;

public class InvalidAnypointDescriptorException extends Exception {
    public InvalidAnypointDescriptorException() {
    }

    public InvalidAnypointDescriptorException(String message) {
        super(message);
    }

    public InvalidAnypointDescriptorException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAnypointDescriptorException(Throwable cause) {
        super(cause);
    }

    public InvalidAnypointDescriptorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
