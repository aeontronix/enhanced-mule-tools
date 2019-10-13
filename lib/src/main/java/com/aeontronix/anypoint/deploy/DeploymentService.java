/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.anypoint.deploy;

import com.aeontronix.anypoint.HttpException;
import com.aeontronix.anypoint.Service;
import com.aeontronix.anypoint.api.provision.APIProvisioningConfig;
import com.aeontronix.anypoint.api.provision.ProvisioningException;
import com.aeontronix.anypoint.runtime.HDeploymentResult;
import com.aeontronix.anypoint.runtime.Server;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public interface DeploymentService extends Service {
    HDeploymentResult deployOnPrem(Server target, @NotNull String name, @NotNull File file, @NotNull String filename,
                                   APIProvisioningConfig apiProvisioningConfig)
            throws IOException, HttpException, ProvisioningException;

}
