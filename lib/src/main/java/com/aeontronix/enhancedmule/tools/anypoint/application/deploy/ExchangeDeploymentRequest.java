/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;

public class ExchangeDeploymentRequest extends AbstractDeploymentRequest {
    private final ApplicationIdentifier appId;
    private final Organization org;
    private final ApplicationSource applicationSource;
    private final String newVersion;

    public ExchangeDeploymentRequest(String buildNumber, ApplicationIdentifier appId, Organization org, ApplicationSource applicationSource,
                                     String newVersion) {
        super(buildNumber);
        this.appId = appId;
        this.org = org;
        this.applicationSource = applicationSource;
        this.newVersion = newVersion;
    }

    public ApplicationIdentifier getAppId() {
        return appId;
    }

    public Organization getOrg() {
        return org;
    }

    public ApplicationSource getApplicationSource() {
        return applicationSource;
    }

    public String getNewVersion() {
        return newVersion;
    }
}
