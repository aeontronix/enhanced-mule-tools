/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.application.MavenHelper;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment.DeploymentParameters;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.application.DeploymentException;
import com.aeontronix.enhancedmule.tools.fabric.Fabric;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.legacy.deploy.rtf.RTFDeploymentOperation;
import com.aeontronix.enhancedmule.tools.runtime.ApplicationDeploymentFailedException;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Server;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.aeontronix.unpack.UnpackException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class DeploymentServiceImpl implements DeploymentService {
    private static final Logger logger = getLogger(DeploymentServiceImpl.class);
    private static final EMTLogger elogger = new EMTLogger(logger);
    private AnypointClient client;

    public DeploymentServiceImpl(AnypointClient client) {
        this.client = client;
    }

    @Override
    public void deployToExchange(ExchangeDeploymentRequest req) throws IOException, UnpackException {
        MavenHelper.uploadToMaven(req.getAppId(), req.getOrg(), req.getApplicationSource(), null, req.getBuildNumber());
    }

    @Override
    public void deploy(RuntimeDeploymentRequest request, ObjectNode appDescJson, ApplicationSource source) throws DeploymentException, ProvisioningException {
        final String target = request.getTarget();
        final Environment environment = request.getEnvironment();
        final Organization organization = environment.getOrganization();
        try {
            DeploymentOperation op = createDeploymentOperation(request, source, target, environment, organization);
            request.setAppName(op.processAppName(request.getAppName()));
            final JsonNode appId = appDescJson.get("id");
            if (appId != null && !appId.isNull()) {
                request.getVars().put("app.id", appId.textValue());
            }
            JsonHelper.processVariables(appDescJson, request.getVars());
            ApplicationDescriptor applicationDescriptor = client.getJsonHelper().getJsonMapper().readerFor(ApplicationDescriptor.class).readValue(appDescJson);
            request.setApplicationDescriptor(applicationDescriptor);
            logger.info(Ansi.ansi().fgBrightYellow().a("Deploying application").toString());
            logger.info(Ansi.ansi().fgBrightYellow().a("Organization: ").reset().a(organization.getName()).toString());
            logger.info(Ansi.ansi().fgBrightYellow().a("Environment: ").reset().a(environment.getName()).toString());
            logger.info(Ansi.ansi().fgBrightYellow().a("Target: ").reset().a(target).toString());
            logger.info(Ansi.ansi().fgBrightYellow().a("App Name: ").reset().a(request.getAppName()).toString());
            final DeploymentResult result = op.deploy();
            waitForApplicationStart(request, result);
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Deployment completed");
        } catch (NotFoundException e) {
            throw new DeploymentException("Target " + target + " not found in env " + environment.getName() + " in business group " + organization.getName());
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
    }

    private void waitForApplicationStart(RuntimeDeploymentRequest request, DeploymentResult result) throws HttpException, ApplicationDeploymentFailedException {
        if (result != null && !request.isSkipWait()) {
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Waiting for application start");
            final DeploymentParameters deploymentParameters = request.getDeploymentParameters();
            result.waitDeployed(deploymentParameters.getDeployTimeout().toMillis(), deploymentParameters.getDeployRetryDelay().toMillis());
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Application started successfully");
        }
    }

    @NotNull
    private DeploymentOperation createDeploymentOperation(RuntimeDeploymentRequest request, ApplicationSource source, String target, Environment environment, Organization organization) throws HttpException, NotFoundException {
        DeploymentOperation op;
        if (target.equalsIgnoreCase("cloudhub")) {
            op = new CHDeploymentOperation(request, environment, source);
        } else {
            try {
                Server server = environment.findServerByName(target);
                op = new HDeploymentOperation(request, server, source);
            } catch (NotFoundException e) {
                final Fabric fabric = organization.findFabricByName(target);
                op = new RTFDeploymentOperation(fabric, request, environment, source);
            }
        }
        return op;
    }
}
