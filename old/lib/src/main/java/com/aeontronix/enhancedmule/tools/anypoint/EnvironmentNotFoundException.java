/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint;

public class EnvironmentNotFoundException extends NotFoundException {
    public EnvironmentNotFoundException() {
    }

    public EnvironmentNotFoundException(String message) {
        super(message);
    }

    public EnvironmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnvironmentNotFoundException(Throwable cause) {
        super(cause);
    }
}
