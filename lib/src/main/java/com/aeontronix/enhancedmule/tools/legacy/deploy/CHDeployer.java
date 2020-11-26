/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.util.*;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.cloudhub.CHMuleVersion;
import com.aeontronix.enhancedmule.tools.cloudhub.CHWorkerType;
import com.aeontronix.enhancedmule.tools.runtime.CHApplication;
import com.aeontronix.enhancedmule.tools.runtime.CHDeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.aeontronix.commons.StringUtils.isBlank;

public class CHDeployer extends Deployer {
    private static final Logger logger = LoggerFactory.getLogger(CHDeployer.class);
    private static final EMTLogger elogger = new EMTLogger(logger);
    private int workerCount;
    private CHMuleVersion muleVersion;
    private String region;
    private CHWorkerType workerType;

    public CHDeployer() {
    }

    public CHDeployer(String muleVersionName, String regionName, String workerTypeName, int workerCount,
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
        if(deploymentConfig.isExtMonitoring()) {
            deploymentConfig.setProperty("anypoint.platform.config.analytics.agent.enabled","true");
        }
        CHApplication existingApp = getExistingApp(appName);
        deploymentConfig.mergeExistingProperties(existingApp);
        appInfoBuilder.set("properties", deploymentConfig.getProperties())
                .set("domain", appName)
                .set("monitoringEnabled", true)
                .set("monitoringAutoRestart", true)
                .set("loggingNgEnabled", true)
                .set("objectStoreV1", deploymentConfig.isObjectStoreV1())
                .set("persistentQueues", deploymentConfig.isPersistentQueues())
                .set("persistentQueuesEncrypted", deploymentConfig.isPersistentQueuesEncrypted())
                .set("staticIPsEnabled", deploymentConfig.isStaticIPs())
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
                req = httpHelper.createAnypointMultiPartPutRequest("/cloudhub/api/v2/applications/" + existingApp.getDomain(),
                        environment);
            } else {
                req = httpHelper.createAnypointMultiPartPostRequest("/cloudhub/api/v2/applications", getEnvironment());
                req = req.addText("autoStart", "true");
            }
            String appInfoJson = new String(environment.getClient().getJsonHelper().toJson(appInfo));
            req = req.addText("appInfoJson", appInfoJson);
            logger.debug("Deployment JSON: {}", appInfoJson);
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
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Uploading application archive to Cloudhub");
            deploymentJson = req.execute();
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Application starting");
        } else {
            Map<String, Object> deployJson = new HashMap<>();
            deployJson.put("applicationInfo", appInfo);
            deployJson.put("applicationSource", source.getSourceJson(client.getJsonHelper()));
            if (existingApp != null) {
                deploymentJson = httpHelper.anypointHttpPut("/cloudhub/api/v2/applications/" + existingApp.getDomain(), deployJson, environment);
            } else {
                deployJson.put("autoStart", true);
                deploymentJson = httpHelper.anypointHttpPost("/cloudhub/api/v2/applications/", deployJson, environment);
            }
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Requested application start from exchange asset");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("File upload took " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds");
        }
        if (existingApp != null &&
                (!existingApp.getStatus().equalsIgnoreCase("STARTED")
                        || existingApp.getDeploymentUpdateStatus() != null)) {
            try {
                existingApp.start();
            } catch (Exception e) {
                logger.debug(e.getMessage(), e);
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
