/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.anypoint.runtime;

import com.aeontronix.anypoint.HttpException;

public abstract class DeploymentResult {
    public void waitDeployed() throws HttpException, ApplicationDeploymentFailedException {
        waitDeployed(60000L, 1500L);
    }

    public abstract void waitDeployed(long timeout, long retryDelay) throws HttpException, ApplicationDeploymentFailedException;
}
