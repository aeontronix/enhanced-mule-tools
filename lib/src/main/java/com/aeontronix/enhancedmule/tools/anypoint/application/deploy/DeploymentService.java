/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.anypoint.application.DeploymentException;
import com.aeontronix.unpack.UnpackException;

import java.io.IOException;

public interface DeploymentService {
    ApplicationIdentifier deployToExchange(ExchangeDeploymentRequest req) throws IOException, UnpackException;

    void deploy(RuntimeDeploymentRequest request) throws DeploymentException, ProvisioningException;
}
