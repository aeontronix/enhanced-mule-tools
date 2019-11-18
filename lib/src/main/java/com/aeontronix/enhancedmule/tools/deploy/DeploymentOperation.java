/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.deploy;

import com.aeontronix.enhancedmule.tools.Environment;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.runtime.HDeploymentResult;

import java.io.File;
import java.io.IOException;

public interface DeploymentOperation {
    HDeploymentResult deploy(Environment environment, String appName, String filename, File file) throws IOException, HttpException;
}
