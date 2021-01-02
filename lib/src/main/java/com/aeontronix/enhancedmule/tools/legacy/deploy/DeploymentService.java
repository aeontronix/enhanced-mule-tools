/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy;

import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.anypoint.Service;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.api.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
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
