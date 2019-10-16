/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.api.provision;

import com.aeontronix.enhancedmule.tools.api.API;
import com.aeontronix.enhancedmule.tools.api.ClientApplication;

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
