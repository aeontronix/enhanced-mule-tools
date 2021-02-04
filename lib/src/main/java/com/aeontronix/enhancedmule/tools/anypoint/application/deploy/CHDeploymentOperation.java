/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment.CloudhubDeploymentParameters;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment.DeploymentParameters;
import com.aeontronix.enhancedmule.tools.application.DeploymentException;
import com.aeontronix.enhancedmule.tools.cloudhub.CHMuleVersion;
import com.aeontronix.enhancedmule.tools.cloudhub.CHWorkerType;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.runtime.CHApplication;
import com.aeontronix.enhancedmule.tools.runtime.CHDeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.aeontronix.commons.StringUtils.isBlank;
import static com.aeontronix.enhancedmule.tools.anypoint.Environment.Type.PRODUCTION;

public class CHDeploymentOperation extends DeploymentOperation {
    private static final Logger logger = LoggerFactory.getLogger(CHDeploymentOperation.class);
    private static final EMTLogger elogger = new EMTLogger(logger);
    private int workerCount;
    private CHMuleVersion muleVersion;
    private String region;
    private CHWorkerType workerType;

    public CHDeploymentOperation(RuntimeDeploymentRequest req, Environment environment, ApplicationSource source) {
        super(req, environment, source);
    }

//    public CHDeploymentOperation(String muleVersionName, String regionName, String workerTypeName, int workerCount,
//                                 Environment environment, String appName, ApplicationSource file, String filename,
//                                 APIProvisioningConfig apiProvisioningConfig,
//                                 DeploymentConfig deploymentConfig, ProvisioningRequest provisioningRequest,
//                                 ApplicationDescriptor applicationDescriptor) throws HttpException, NotFoundException {
//        super(environment, appName, file, filename, apiProvisioningConfig, deploymentConfig, provisioningRequest, applicationDescriptor);
//        this.workerCount = workerCount;
////        if (isBlank(muleVersionName)) {
////            muleVersion = environment.findDefaultCHMuleVersion();
////        } else {
////            muleVersion = environment.findCHMuleVersion(muleVersionName);
////        }
//
//    }

    @Override
    protected DeploymentResult doDeploy(RuntimeDeploymentRequest request) throws IOException, HttpException, DeploymentException {
        try {
            long start = System.currentTimeMillis();
            final DeploymentParameters deploymentParameters = request.getApplicationDescriptor().getDeploymentParams();
            final CloudhubDeploymentParameters cloudhub = request.getApplicationDescriptor().getDeploymentParams().getCloudhub();
            try {
                if (isBlank(cloudhub.getMuleVersion())) {
                    muleVersion = environment.findDefaultCHMuleVersion();
                } else {
                    muleVersion = environment.findCHMuleVersion(cloudhub.getMuleVersion());
                }
            } catch (NotFoundException e) {
                throw new DeploymentException(e);
            }
            if (isBlank(cloudhub.getRegion())) {
                region = environment.findDefaultCHRegion().getId();
            } else {
                region = cloudhub.getRegion();
            }
            if (isBlank(cloudhub.getWorkerType())) {
                workerType = environment.findSmallestWorkerType();
            } else {
                workerType = environment.findWorkerTypeByName(cloudhub.getWorkerType());
            }
            if( workerCount < 1) {
                workerCount = 1;
            }
            AnypointClient client = environment.getClient();
            HttpHelper httpHelper = client.getHttpHelper();
            JsonHelper.MapBuilder appInfoBuilder = client.getJsonHelper().buildJsonMap();
            final Boolean extMonitoring = deploymentParameters.getExtMonitoring();
            if (extMonitoring == null || extMonitoring) {
                deploymentRequest.setProperty("anypoint.platform.config.analytics.agent.enabled", "true");
            }
            String appName = request.getAppName();
            CHApplication existingApp = getExistingApp(appName);
            deploymentRequest.mergeExistingProperties(existingApp);
            appInfoBuilder.set("properties", deploymentRequest.getProperties())
                    .set("domain", appName)
                    .set("monitoringEnabled", true)
                    .set("monitoringAutoRestart", true)
                    .set("loggingNgEnabled", true)
                    .set("objectStoreV1", cloudhub.getObjectStoreV1())
                    .set("persistentQueues", cloudhub.getPersistentQueues())
                    .set("persistentQueuesEncrypted", cloudhub.getPersistentQueuesEncrypted())
                    .set("staticIPsEnabled", cloudhub.getStaticIPs())
                    .set("loggingCustomLog4JEnabled", cloudhub.getCustomlog4j());
            appInfoBuilder.addMap("muleVersion").set("version", muleVersion.getVersion()).set("updateId", muleVersion.getLatestUpdate().getId());
            appInfoBuilder.addMap("workers")
                    .set("amount", workerCount)
                    .addMap("type")
                    .set("name", workerType.getName())
                    .set("weight", workerType.getWeight())
                    .set("cpu", workerType.getCpu())
                    .set("memory", workerType.getMemory());
            appInfoBuilder.set("fileName", deploymentRequest.getFilename());
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
                        return deploymentRequest.getFilename();
                    }

                    @Override
                    public InputStream createInputStream() throws IOException {
                        return new FileInputStream(source.getLocalFile());
                    }
                });
                elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Uploading application archive to Cloudhub");
                deploymentJson = req.execute();
                elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Application starting: " + appName);
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
                elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Requested application start from exchange asset: " + appName);
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
        } catch (NotFoundException e) {
            throw new DeploymentException(e);
        }
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

    @Override
    public String processAppName(String appName) {
        if (appName == null) {
            final CloudhubDeploymentParameters cloudhub = deploymentRequest.getApplicationDescriptor().getDeploymentParams().getCloudhub();
            if (cloudhub.getAppNameSuffix() != null) {
                appName = deploymentRequest.getArtifactId() + cloudhub.getAppNameSuffix();
            } else {
                final Environment environment = getEnvironment();
                if (!cloudhub.getAppNameSuffixNPOnly() || !PRODUCTION.equals(environment.getType())) {
                    appName = deploymentRequest.getArtifactId() + environment.getSuffix();
                }
            }
            if (cloudhub.getAppNamePrefix() != null) {
                appName = cloudhub.getAppNamePrefix() + appName;
            }
            return appName;
        }
        return appName;
    }
}
