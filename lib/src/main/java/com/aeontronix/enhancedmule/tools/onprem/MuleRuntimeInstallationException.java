/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.onprem;

public class MuleRuntimeInstallationException extends Exception {
    public MuleRuntimeInstallationException() {
    }

    public MuleRuntimeInstallationException(String message) {
        super(message);
    }

    public MuleRuntimeInstallationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MuleRuntimeInstallationException(Throwable cause) {
        super(cause);
    }
}
