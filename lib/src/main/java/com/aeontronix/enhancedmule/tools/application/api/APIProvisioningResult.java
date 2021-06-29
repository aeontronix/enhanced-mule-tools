/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application.api;

import com.aeontronix.enhancedmule.tools.anypoint.api.API;
import com.aeontronix.enhancedmule.tools.anypoint.api.ClientApplication;

public class APIProvisioningResult {
    private API api;
    private ClientApplication clientApplication;

    public API getApi() {
        return api;
    }

    public void setApi(API api) {
        this.api = api;
    }

    public ClientApplication getClientApplication() {
        return clientApplication;
    }

    public void setClientApplication(ClientApplication clientApplication) {
        this.clientApplication = clientApplication;
    }
}
