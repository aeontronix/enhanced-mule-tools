/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.exchange;

import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;

public class AssetProvisioningException extends ProvisioningException {
    public AssetProvisioningException() {
    }

    public AssetProvisioningException(String message) {
        super(message);
    }

    public AssetProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }

    public AssetProvisioningException(Throwable cause) {
        super(cause);
    }

    public AssetProvisioningException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
