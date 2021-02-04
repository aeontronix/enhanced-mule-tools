/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.application.DeploymentException;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.unpack.UnpackException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public interface DeploymentService {
    void deployToExchange(ExchangeDeploymentRequest req) throws IOException, UnpackException;

    void deploy(RuntimeDeploymentRequest request, ObjectNode appDescJson, ApplicationSource source) throws DeploymentException, ProvisioningException;
}
