/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.application.MavenHelper;

public abstract class AbstractDeploymentRequest {
    protected String buildNumber;

    public AbstractDeploymentRequest(String buildNumber) {
        if (buildNumber == null) {
            this.buildNumber = MavenHelper.generateTimestampString();
        } else {
            this.buildNumber = buildNumber;
        }
    }

    public String getBuildNumber() {
        return buildNumber;
    }
}
