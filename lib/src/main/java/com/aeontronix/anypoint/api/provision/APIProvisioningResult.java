/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.anypoint.api.provision;

import com.aeontronix.anypoint.api.API;
import com.aeontronix.anypoint.api.ClientApplication;

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
