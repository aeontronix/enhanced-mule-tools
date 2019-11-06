/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.deploy;

import com.aeontronix.enhancedmule.tools.AnypointClient;
import com.aeontronix.enhancedmule.tools.Environment;
import com.aeontronix.enhancedmule.tools.HttpException;
import com.aeontronix.enhancedmule.tools.NotFoundException;
import com.aeontronix.enhancedmule.tools.api.provision.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.api.provision.APIProvisioningResult;
import com.aeontronix.enhancedmule.tools.cloudhub.CHMuleVersion;
import com.aeontronix.enhancedmule.tools.cloudhub.CHWorkerType;
import com.aeontronix.enhancedmule.tools.runtime.CHApplication;
import com.aeontronix.enhancedmule.tools.runtime.CHDeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.aeontronix.enhancedmule.tools.util.StreamSource;
import com.kloudtek.unpack.transformer.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.kloudtek.util.StringUtils.isBlank;

public class CHDeploymentRequest extends DeploymentRequest {
    private static final Logger logger = LoggerFactory.getLogger(CHDeploymentRequest.class);
    private int workerCount;
    private CHMuleVersion muleVersion;
    private String region;
    private CHWorkerType workerType;

    public CHDeploymentRequest() {
    }

    @Override
    protected void preDeploy(APIProvisioningResult result, APIProvisioningConfig config, DeploymentConfig deploymentConfig, List<Transformer> transformers) {
    }

    public CHDeploymentRequest(String muleVersionName, String regionName, String workerTypeName, int workerCount,
                               Environment environment, String appName, ApplicationSource file, String filename,
                               APIProvisioningConfig apiProvisioningConfig,
                               DeploymentConfig deploymentConfig) throws HttpException, NotFoundException {
        super(environment, appName, file, filename, apiProvisioningConfig, deploymentConfig);
        this.workerCount = workerCount;
        if (isBlank(muleVersionName)) {
            muleVersion = environment.findDefaultCHMuleVersion();
        } else {
            muleVersion = environment.findCHMuleVersion(muleVersionName);
        }
        if (isBlank(regionName)) {
            region = environment.findDefaultCHRegion().getId();
        } else {
            region = regionName;
        }
        if (isBlank(workerTypeName)) {
            workerType = environment.findSmallestWorkerType();
        } else {
            workerType = environment.findWorkerTypeByName(workerTypeName);
        }
    }

    @Override
    protected DeploymentResult doDeploy() throws IOException, HttpException {
        long start = System.currentTimeMillis();
        AnypointClient client = environment.getClient();
        HttpHelper httpHelper = client.getHttpHelper();
        JsonHelper.MapBuilder appInfoBuilder = client.getJsonHelper().buildJsonMap();
        CHApplication existingApp = getExistingApp(appName);
        deploymentConfig.mergeExistingProperties(existingApp);
        appInfoBuilder.set("properties", deploymentConfig.getProperties())
                .set("domain", appName)
                .set("monitoringEnabled", true)
                .set("monitoringAutoRestart", true)
                .set("loggingNgEnabled", true)
                .set("loggingCustomLog4JEnabled", deploymentConfig.isCustomlog4j());
        appInfoBuilder.addMap("muleVersion").set("version", muleVersion.getVersion()).set("updateId", muleVersion.getLatestUpdate().getId());
        appInfoBuilder.addMap("workers")
                .set("amount", workerCount)
                .addMap("type")
                .set("name", workerType.getName())
                .set("weight", workerType.getWeight())
                .set("cpu", workerType.getCpu())
                .set("memory", workerType.getMemory());
        appInfoBuilder.set("fileName", filename);
        Map<String, Object> appInfo = appInfoBuilder.toMap();
        String deploymentJson;
        logger.debug("Deploying application");
        if (source.getLocalFile() != null) {
            HttpHelper.MultiPartRequest req;
            if (existingApp != null) {
                req = httpHelper.createMultiPartPutRequest("/cloudhub/api/v2/applications/" + existingApp.getDomain(),
                        environment);
            } else {
                req = httpHelper.createMultiPartPostRequest("/cloudhub/api/v2/applications", getEnvironment());
                req = req.addText("autoStart", "true");
            }
            String appInfoJson = new String(environment.getClient().getJsonHelper().toJson(appInfo));
            req = req.addText("appInfoJson", appInfoJson);
            logger.debug("Deployment JSON: {}",appInfoJson);
            req = req.addBinary("file", new StreamSource() {
                @Override
                public String getFileName() {
                    return filename;
                }

                @Override
                public InputStream createInputStream() throws IOException {
                    return new FileInputStream(source.getLocalFile());
                }
            });
            deploymentJson = req.execute();
        } else {
            Map<String, Object> deployJson = new HashMap<>();
            deployJson.put("applicationInfo", appInfo);
            deployJson.put("applicationSource", source.getSourceJson(client.getJsonHelper()));
            if (existingApp != null) {
                deploymentJson = httpHelper.httpPut("/cloudhub/api/v2/applications/" + existingApp.getDomain(), deployJson, environment);
            } else {
                deployJson.put("autoStart", true);
                deploymentJson = httpHelper.httpPost("/cloudhub/api/v2/applications/", deployJson, environment);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("File upload took " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds");
        }
        if(existingApp != null &&
                (! existingApp.getStatus().equalsIgnoreCase("STARTED")
                        || existingApp.getDeploymentUpdateStatus() != null )) {
            try {
                existingApp.start();
            } catch (Exception e) {
                logger.debug(e.getMessage(),e);
            }
        }
        return new CHDeploymentResult(client.getJsonHelper().readJson(new CHApplication(), deploymentJson, environment));
    }

    private CHApplication getExistingApp(String appName) throws HttpException {
        try {
            logger.debug("Searching for pre-existing application named " + appName);
            CHApplication application = environment.findCHApplicationByDomain(appName);
            logger.debug("Found application named {}", appName);
            return application;
        } catch (NotFoundException e) {
            logger.debug("Couldn't find application named {}", appName);
            return null;
        }
    }

}