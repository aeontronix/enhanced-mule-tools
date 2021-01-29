/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy;

import com.aeontronix.enhancedmule.tools.AbstractService;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.api.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.runtime.HDeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Server;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("ALL")
public class DeploymentServiceImpl extends AbstractService implements DeploymentService {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImpl.class);

    /**
     * Deploy application with optional automatic api provisioning
     *
     * @param target                target to deploy to
     * @param appName               Application name
     * @param file                  Application archive file
     * @param filename              Application archive filename
     * @param apiProvisioningConfig API Provisioning config (if null no API provisioning will be done)
     * @return Deployment result
     * @throws IOException   If an error occurs reading the application file
     * @throws HttpException If an error occurs commnunicating with anypoint
     */
    @Override
    public HDeploymentResult deployOnPrem(Server target, @NotNull String appName, @NotNull File file, @NotNull String filename,
                                          APIProvisioningConfig apiProvisioningConfig) throws IOException, HttpException, ProvisioningException {
//        OnPremDeploymentOperation deploymentRequest = new OnPremDeploymentOperation(target);
//        return deploy(target.getParent(), appName, file, filename, apiProvisioningConfig, deploymentRequest);
        return null;
    }

}
