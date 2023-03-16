/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.anypointsdk.AnypointClient;
import com.aeontronix.anypointsdk.cloudhub2.CH2AppDeploymentParameters;
import com.aeontronix.anypointsdk.cloudhub2.CH2DeploymentResponse;
import com.aeontronix.anypointsdk.cloudhub2.DeploymentFailedException;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.application.DeploymentException;
import com.aeontronix.enhancedmule.tools.application.deployment.CloudhubDeploymentParameters;
import com.aeontronix.enhancedmule.tools.cloudhub.CHMuleVersion;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.runtime.ApplicationDeploymentFailedException;
import com.aeontronix.enhancedmule.tools.runtime.CHApplication;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.restclient.RESTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.aeontronix.enhancedmule.tools.anypoint.Environment.Type.PRODUCTION;

public class CH2DeploymentOperation extends DeploymentOperation {
    private static final Logger logger = LoggerFactory.getLogger(CH2DeploymentOperation.class);
    private static final EMTLogger elogger = new EMTLogger(logger);
    public static final String CH2_TARGET_PREFIX = "cloudhub2:";
    private CHMuleVersion muleVersion;
    private AnypointClient anypointClient;

    public CH2DeploymentOperation(AnypointClient anypointClient, RuntimeDeploymentRequest req, Environment environment, ApplicationSource source) {
        super(req, environment, source);
        this.anypointClient = anypointClient;
    }

    @Override
    protected DeploymentResult doDeploy(RuntimeDeploymentRequest request) throws IOException, HttpException, DeploymentException {
        try {
            long start = System.currentTimeMillis();
            CH2AppDeploymentParameters ch2 = request.getApplicationDescriptor().getDeploymentParams().getCloudhub2();
            if (ch2 == null) {
                ch2 = new CH2AppDeploymentParameters();
            }
            String orgId = environment.getOrganization().getId();
            String target = request.getTarget();
            if (target.toLowerCase().startsWith(CH2_TARGET_PREFIX)) {
                target = target.substring(CH2_TARGET_PREFIX.length());
            }
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Deploying application to Cloudhub 2");
            CH2DeploymentResponse response = anypointClient.getCloudhub2Client().deployApplication(ch2, request.getAppName(),
                    orgId, environment.getId(), target,
                    source.toSDKSource(),
                    request.getProperties(), request.getSecureProperties(), request.getBuildNumber());
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Application starting: " + request.getAppName());
            if (logger.isDebugEnabled()) {
                logger.debug("File upload took " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds");
            }
            return new DeploymentResult() {
                @Override
                public void waitDeployed(long timeout, long retryDelay) throws HttpException, ApplicationDeploymentFailedException {
                    try {
                        response.waitDeploymentComplete(Duration.ofMillis(timeout), retryDelay);
                    } catch (DeploymentFailedException e) {
                        throw new ApplicationDeploymentFailedException(e);
                    }
                }
            };
        } catch (RESTException e) {
            throw new RuntimeException(e);
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
