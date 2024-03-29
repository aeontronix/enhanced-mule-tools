/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.anypoint.application.DeploymentException;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.restclient.RESTException;
import com.aeontronix.unpack.UnpackException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public interface DeploymentService {
    ApplicationIdentifier deployToExchange(ExchangeDeploymentRequest req) throws IOException, UnpackException, RESTException;

    void deploy(RuntimeDeploymentRequest request, ObjectNode appDescJson, DescriptorLayers layers, ApplicationSource source) throws DeploymentException, ProvisioningException;
}
