/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.api;

import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;

public class SLATierNotFoundException extends NotFoundException {
    public SLATierNotFoundException() {
    }

    public SLATierNotFoundException(String message) {
        super(message);
    }

    public SLATierNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SLATierNotFoundException(Throwable cause) {
        super(cause);
    }
}
