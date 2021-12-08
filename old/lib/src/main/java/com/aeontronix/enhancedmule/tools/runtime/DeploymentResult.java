/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.runtime;

import com.aeontronix.enhancedmule.tools.util.HttpException;

public abstract class DeploymentResult {
    public void waitDeployed() throws HttpException, ApplicationDeploymentFailedException {
        waitDeployed(60000L, 1500L);
    }

    public abstract void waitDeployed(long timeout, long retryDelay) throws HttpException, ApplicationDeploymentFailedException;
}
