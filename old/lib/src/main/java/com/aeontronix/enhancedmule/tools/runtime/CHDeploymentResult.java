/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.runtime;

import com.aeontronix.commons.ThreadUtils;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

public class CHDeploymentResult extends DeploymentResult {
    public static final String UNDEPLOYED = "UNDEPLOYED";
    static final String DEPLOYMENT_FAILED = "DEPLOYMENT_FAILED";
    static final String DEPLOY_FAILED = "DEPLOY_FAILED";
    static final String STARTED = "STARTED";
    private static final Logger logger = LoggerFactory.getLogger(CHDeploymentResult.class);
    private CHApplication application;

    public CHDeploymentResult(CHApplication application) {
        this.application = application;
    }

    @Override
    public void waitDeployed(long timeout, long retryDelay) throws HttpException, ApplicationDeploymentFailedException {
        ThreadUtils.sleep(5000);
        LinkedList<String> history = new LinkedList<>();
        String lastStatus = null;
        long expires = System.currentTimeMillis() + timeout;
        long lastUpdt = application.getLastUpdateTime();
        for (; ; ) {
            try {
                application = application.refresh();
                final String status = application.getStatus();
                if (lastStatus == null || !lastStatus.equals(status)) {
                    history.add(status);
                    lastStatus = status;
                }
                if (DEPLOYMENT_FAILED.equalsIgnoreCase(status)) {
                    logger.error("Deployment failed due to status: " + status + " history=" + history);
                    throw new ApplicationDeploymentFailedException();
                } else if (DEPLOY_FAILED.equalsIgnoreCase(application.getDeploymentUpdateStatus())) {
                    logger.error("Deployment failed due to deployment update status: " + application.getDeploymentUpdateStatus()+" history="+history);
                    throw new ApplicationDeploymentFailedException();
                } else if (status.equalsIgnoreCase(STARTED)
                        && application.getDeploymentUpdateStatus() == null
                        && application.getLastUpdateTime() > lastUpdt) {
                    return;
                }
            } catch (NotFoundException e) {
                // application
            }
            if (expires > System.currentTimeMillis()) {
                ThreadUtils.sleep(retryDelay);
            } else {
                logger.error("Deployment failed due to timeout. history="+history);
                throw new ApplicationDeploymentFailedException();
            }
        }
    }
}
