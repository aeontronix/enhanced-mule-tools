/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class AbstractDeploymentRequest {
    protected String buildNumber;

    public AbstractDeploymentRequest(String buildNumber) {
        if (buildNumber == null) {
            this.buildNumber = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS").format(LocalDateTime.now());
        }
    }

    public String getBuildNumber() {
        return buildNumber;
    }
}
