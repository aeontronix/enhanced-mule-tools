/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.anypoint.deploy;

import com.aeontronix.anypoint.Environment;
import com.aeontronix.anypoint.HttpException;
import com.aeontronix.anypoint.runtime.HDeploymentResult;

import java.io.File;
import java.io.IOException;

public interface DeploymentOperation {
    HDeploymentResult deploy(Environment environment, String appName, String filename, File file) throws IOException, HttpException;
}
