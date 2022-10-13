/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.anypoint.application.DeploymentException;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.deployment.DeploymentParameters;
import com.aeontronix.enhancedmule.tools.fabric.Fabric;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.legacy.deploy.rtf.RTFDeploymentOperation;
import com.aeontronix.enhancedmule.tools.runtime.ApplicationDeploymentFailedException;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Server;
import com.aeontronix.enhancedmule.tools.util.*;
import com.aeontronix.unpack.UnpackException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

import static com.aeontronix.enhancedmule.tools.util.JsonHelper.isNotNull;
import static com.aeontronix.enhancedmule.tools.util.JsonHelper.isNull;
import static org.slf4j.LoggerFactory.getLogger;

public class DeploymentServiceImpl implements DeploymentService {
    private static final Logger logger = getLogger(DeploymentServiceImpl.class);
    private static final EMTLogger elogger = new EMTLogger(logger);
    private AnypointClient client;

    public DeploymentServiceImpl(AnypointClient client) {
        this.client = client;
    }

    @Override
    public ApplicationIdentifier deployToExchange(ExchangeDeploymentRequest req) throws IOException, UnpackException {
        return MavenHelper.uploadToMaven(req.getAppId(), req.getOrg(), req.getApplicationSource(), null, req.getBuildNumber());
    }

    @Override
    public void deploy(RuntimeDeploymentRequest request, ObjectNode appDescJson, ApplicationSource source) throws DeploymentException, ProvisioningException {
        String target = request.getTarget();
        if (request.getFilename() == null) {
            request.setFilename(source.getFileName());
        }
        final Environment environment = request.getEnvironment();
        final Organization organization = environment.getOrganization();
        try {
            final ObjectMapper jsonMapper = client.getJsonHelper().getJsonMapper();
            // default layer
            final JsonNode jsonDesc = ApplicationDescriptor.createDefault(jsonMapper);
            // Descriptor layer
            if( appDescJson != null && !appDescJson.isNull() ) {
                DescriptorHelper.override((ObjectNode) jsonDesc, appDescJson);
                // Descriptor override layers
                processOverrides(environment, (ObjectNode) jsonDesc, appDescJson.get("overrides"));
                final JsonNode appId = appDescJson.get("id");
                if (appId != null && !appId.isNull()) {
                    request.getVars().put("app.id", appId.textValue());
                } else {
                    request.getVars().put("app.id", source.getArtifactId());
                }
            } else {
                request.getVars().put("app.id", source.getArtifactId());
            }
            final JsonNode legacyAppDescriptor = request.getLegacyAppDescriptor();
            if( legacyAppDescriptor != null && !legacyAppDescriptor.isNull()) {
                DescriptorHelper.override((ObjectNode) jsonDesc, (ObjectNode) legacyAppDescriptor);
            }
            JsonHelper.processVariables((ObjectNode) jsonDesc, request.getVars());
            ApplicationDescriptor applicationDescriptor = jsonMapper.readerFor(ApplicationDescriptor.class)
                    .readValue(jsonDesc);
            request.setApplicationDescriptor(applicationDescriptor);
            if (target == null) {
                target = applicationDescriptor.getDeploymentParams().getTarget();
                if (target == null) {
                    target = "cloudhub";
                }
                request.setTarget(target);
            }
            DeploymentOperation op = createDeploymentOperation(request, source, environment, organization);
            request.setAppName(op.processAppName(request.getAppName()));
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

    private void processOverrides(Environment environment, ObjectNode jsonDesc, JsonNode overrides) throws ProvisioningException {
        if (isNotNull(overrides)) {
            for (JsonNode override : overrides) {
                final String type = JsonHelper.getText(override, "type");
                if (type == null) {
                    throw new ProvisioningException("Invalid override descriptor type " + type + " : " + override);
                } else if (type.equalsIgnoreCase("env")) {
                    final String value = getOverrideValue(override, type);
                    if (environment.getName().equalsIgnoreCase(value) || environment.getId().equalsIgnoreCase(value)) {
                        DescriptorHelper.override(jsonDesc, getOverrideJson(override));
                    }
                } else if (type.equalsIgnoreCase("envtype")) {
                    final String value = getOverrideValue(override, type);
                    if (environment.getType().name().equalsIgnoreCase(value) ) {
                        DescriptorHelper.override(jsonDesc, getOverrideJson(override));
                    }
                }
            }
        }
    }

    private ObjectNode getOverrideJson(JsonNode override) throws ProvisioningException {
        final JsonNode overrideJson = override.get("override");
        if(isNull(overrideJson)) {
            throw new ProvisioningException("Invalid override descriptor : " + override);
        }
        return (ObjectNode) overrideJson;
    }

    @NotNull
    private String getOverrideValue(JsonNode override, String type) throws ProvisioningException {
        final String value = JsonHelper.getText(override, "value");
        if (StringUtils.isBlank(value)) {
            throw new ProvisioningException("Invalid override descriptor value " + type + " : " + override);
        }
        return value;
    }

    private void waitForApplicationStart(RuntimeDeploymentRequest request, DeploymentResult result) throws HttpException, ApplicationDeploymentFailedException {
        if (result != null && !request.isSkipWait()) {
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Waiting for application start");
            final DeploymentParameters deploymentParameters = request.getApplicationDescriptor().getDeploymentParams();
            result.waitDeployed(deploymentParameters.getDeployTimeout().toMillis(), deploymentParameters.getDeployRetryDelay().toMillis());
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Application started successfully");
        }
    }

    @NotNull
    private DeploymentOperation createDeploymentOperation(RuntimeDeploymentRequest request, ApplicationSource source, Environment environment, Organization organization) throws HttpException, NotFoundException {
        DeploymentOperation op;
        final String target = request.getTarget();
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
