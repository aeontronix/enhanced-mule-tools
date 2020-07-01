/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.deploy;

import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.Service;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.runtime.HDeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Server;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public interface DeploymentService extends Service {
    HDeploymentResult deployOnPrem(Server target, @NotNull String name, @NotNull File file, @NotNull String filename,
                                   APIProvisioningConfig apiProvisioningConfig)
            throws IOException, HttpException, ProvisioningException;

}
