/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.deploy;

import com.aeontronix.enhancedmule.tools.AnypointClient;
import com.aeontronix.enhancedmule.tools.HttpException;
import com.aeontronix.enhancedmule.tools.NotFoundException;
import com.aeontronix.enhancedmule.tools.api.provision.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.api.provision.APIProvisioningResult;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.HApplication;
import com.aeontronix.enhancedmule.tools.runtime.HDeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Server;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.aeontronix.enhancedmule.tools.util.StreamSource;
import com.kloudtek.unpack.transformer.SetPropertyTransformer;
import com.kloudtek.unpack.transformer.Transformer;
import com.kloudtek.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HDeploymentRequest extends DeploymentRequest {
    private static final Logger logger = LoggerFactory.getLogger(HDeploymentRequest.class);
    private Server target;
    private JsonHelper jsonHelper;

    public HDeploymentRequest(Server target, String appName, ApplicationSource file, String filename,
                              @NotNull APIProvisioningConfig apiProvisioningConfig,
                              @NotNull DeploymentConfig deploymentConfig) {
        super(target.getParent(), appName, file, filename, apiProvisioningConfig, deploymentConfig);
        this.target = target;
    }

    @Override
    protected DeploymentResult doDeploy() throws IOException, HttpException {
        HttpHelper.MultiPartRequest request;
        long start = System.currentTimeMillis();
        AnypointClient client = environment.getClient();
        HttpHelper httpHelper = client.getHttpHelper();
        try {
            logger.debug("Searching for pre-existing application named " + appName);
            HApplication application = target.findApplication(appName);
            logger.debug("Found application named {} : {}", appName, application.getId());
            request = httpHelper.createMultiPartPatchRequest("/hybrid/api/v1/applications/" + application.getId(),
                    target.getParent());
        } catch (NotFoundException e) {
            logger.debug("Couldn't find application named {}", appName);
            request = httpHelper.createMultiPartPostRequest("/hybrid/api/v1/applications", environment);
        }
        jsonHelper = target.getClient().getJsonHelper();
        HttpHelper.MultiPartRequest multiPartRequest = request.addText("artifactName", appName)
                .addText("targetId", target.getId());
        HashMap<String, Object> rootCfg = new HashMap<>();
        HashMap<String, Object> appCfg = new HashMap<>();
        appCfg.put("applicationName",appName);
        appCfg.put("properties",deploymentConfig.getProperties());
        rootCfg.put("mule.agent.application.properties.service",appCfg);
        String cfgJson = jsonHelper.getJsonMapper().writeValueAsString(rootCfg);
        logger.debug("Added config to hybrid deploy {}",cfgJson);
        multiPartRequest.addText("configuration", cfgJson);
        logger.debug("Sending hybrid deploy request: {}",multiPartRequest);
        multiPartRequest = multiPartRequest.addBinary("file", new StreamSource() {
            @Override
            public String getFileName() {
                return filename;
            }

            @Override
            public InputStream createInputStream() throws IOException {
                return new FileInputStream(source.getLocalFile());
            }
        });
        String json = executeRequest(start, multiPartRequest);
        HApplication application = jsonHelper.readJson(new HApplication(target), json, "/data");
        return new HDeploymentResult(application);
    }
}
