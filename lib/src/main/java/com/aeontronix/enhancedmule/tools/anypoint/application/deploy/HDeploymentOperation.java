/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.HApplication;
import com.aeontronix.enhancedmule.tools.runtime.HDeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Server;
import com.aeontronix.enhancedmule.tools.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class HDeploymentOperation extends DeploymentOperation {
    private static final Logger logger = LoggerFactory.getLogger(HDeploymentOperation.class);
    private static final EMTLogger elogger = new EMTLogger(logger);
    private Server target;
    private JsonHelper jsonHelper;

    public HDeploymentOperation(RuntimeDeploymentRequest req, Server server, ApplicationSource source) {
        super(req, server.getParent(), source);
        this.target = server;
    }

//    public HDeploymentOperation(Server target, String appName, ApplicationSource file, String filename,
//                                @NotNull APIProvisioningConfig apiProvisioningConfig,
//                                @NotNull DeploymentConfig deploymentConfig, ProvisioningRequest provisioningRequest,
//                                ApplicationDescriptor applicationDescriptor) {
//        super(target.getParent(), appName, file, filename, apiProvisioningConfig, deploymentConfig, provisioningRequest, applicationDescriptor );
//        this.target = target;
//    }

    @Override
    protected DeploymentResult doDeploy(RuntimeDeploymentRequest deploymentRequest) throws IOException, HttpException {
        HttpHelper.MultiPartRequest request;
        long start = System.currentTimeMillis();
        LegacyAnypointClient client = environment.getClient();
        HttpHelper httpHelper = client.getHttpHelper();
        String appName = deploymentRequest.getAppName();
        try {
            logger.debug("Searching for pre-existing application named " + appName);
            HApplication application = target.findApplication(appName);
            logger.debug("Found application named {} : {}", appName, application.getId());
            request = httpHelper.createAnypointMultiPartPatchRequest("/hybrid/api/v1/applications/" + application.getId(),
                    target.getParent());
        } catch (NotFoundException e) {
            logger.debug("Couldn't find application named {}", appName);
            request = httpHelper.createAnypointMultiPartPostRequest("/hybrid/api/v1/applications", environment);
        }
        jsonHelper = target.getClient().getJsonHelper();
        HttpHelper.MultiPartRequest multiPartRequest = request.addText("artifactName", appName)
                .addText("targetId", target.getId());
        HashMap<String, Object> rootCfg = new HashMap<>();
        HashMap<String, Object> appCfg = new HashMap<>();
        appCfg.put("applicationName", appName);
        appCfg.put("properties", deploymentRequest.getProperties());
        rootCfg.put("mule.agent.application.properties.service", appCfg);
        String cfgJson = jsonHelper.getJsonMapper().writeValueAsString(rootCfg);
        logger.debug("Added config to hybrid deploy {}", cfgJson);
        multiPartRequest.addText("configuration", cfgJson);
        logger.debug("Sending hybrid deploy request: {}", multiPartRequest);
        multiPartRequest = multiPartRequest.addBinary("file", new StreamSource() {
            @Override
            public String getFileName() {
                return deploymentRequest.getFilename();
            }

            @Override
            public InputStream createInputStream() throws IOException {
                return new FileInputStream(source.getLocalFile());
            }
        });
        elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Uploading application archive to on-prem server");
        String json = executeRequest(start, multiPartRequest);
        elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Application starting");
        HApplication application = jsonHelper.readJson(HApplication.class, new HApplication(target), json, "/data");
        return new HDeploymentResult(application);
    }
}
