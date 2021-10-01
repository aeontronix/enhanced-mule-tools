/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationEnhancer;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.anypoint.application.DeploymentException;
import com.aeontronix.enhancedmule.tools.anypoint.application.MavenHelper;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptorDefaultValues;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.deployment.DeploymentParameters;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.legacy.deploy.rtf.RTFDeploymentOperation;
import com.aeontronix.enhancedmule.tools.runtime.ApplicationDeploymentFailedException;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.util.DescriptorHelper;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.aeontronix.unpack.UnpackException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;

import static com.aeontronix.enhancedmule.tools.util.JsonHelper.isNotNull;
import static com.aeontronix.enhancedmule.tools.util.JsonHelper.isNull;
import static org.slf4j.LoggerFactory.getLogger;

public class DeploymentServiceImpl implements DeploymentService {
    private static final Logger logger = getLogger(DeploymentServiceImpl.class);
    private static final EMTLogger elogger = new EMTLogger(logger);
    private final ObjectMapper jsonMapper = JsonHelper.createMapper();
    private AnypointClient client;

    public DeploymentServiceImpl(AnypointClient client) {
        this.client = client;
    }

    @Override
    public ApplicationIdentifier deployToExchange(ExchangeDeploymentRequest req) throws IOException, UnpackException {
        return MavenHelper.uploadToMaven(req.getAppId(), req.getOrg(), req.getApplicationSource(), null, req.getBuildNumber());
    }

    @Override
    public void deploy(RuntimeDeploymentRequest request) throws DeploymentException, ProvisioningException {
        final ApplicationSource source = request.getApplicationSource();
        String target = request.getTarget();
        if (request.getFilename() == null) {
            request.setFilename(source.getFileName());
        }
        final Environment environment = request.getEnvironment();
        final Organization organization = environment.getOrganization();
        try {
            final ObjectNode anypointDescriptor = source.getAnypointDescriptor();
            final JsonNode processed = anypointDescriptor.get("processed");
            if (isNull(processed) || !processed.booleanValue()) {
                ApplicationDescriptorDefaultValues.setDefaultValues(source.getApplicationSourceMetadata(), anypointDescriptor, jsonMapper);
                try {
                    ApplicationEnhancer.enhanceApplicationArchive(jsonMapper, source, anypointDescriptor, false);
                } catch (UnpackException e) {
                    throw new IOException(e);
                }
            }
            final JsonNode jsonDesc = loadDescriptor(request, environment, jsonMapper, anypointDescriptor);
            if (jsonDesc != null && !jsonDesc.isNull()) {
                final JsonNode appId = jsonDesc.get("id");
                if (appId != null && !appId.isNull()) {
                    request.getVars().put("app.id", appId.textValue());
                } else {
                    request.getVars().put("app.id", source.getArtifactId());
                }
            } else {
                request.getVars().put("app.id", source.getArtifactId());
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

    private JsonNode loadDescriptor(RuntimeDeploymentRequest request, Environment environment,
                                    ObjectMapper jsonMapper, ObjectNode applicationAnypointDescriptor) throws IOException, ProvisioningException {
        // Default layer
        final JsonNode jsonDesc = ApplicationDescriptor.createDefault(jsonMapper);
        // Descriptor layer
        if (applicationAnypointDescriptor != null && !applicationAnypointDescriptor.isNull()) {
            DescriptorHelper.override((ObjectNode) jsonDesc, applicationAnypointDescriptor);
            // Descriptor override layers
            processOverrides(environment, (ObjectNode) jsonDesc, applicationAnypointDescriptor.get("overrides"));
        }
        if( request.getOverrideParameters() != null && !request.getOverrideParameters().isEmpty() ) {
            final DocumentContext documentContext = JsonHelper.createJsonPathDocument(jsonDesc);
            for (Map.Entry<String, String> overrideParam : request.getOverrideParameters().entrySet()) {
                documentContext.set(JsonPath.compile(overrideParam.getKey()), overrideParam.getValue());
            }
        }
        return jsonDesc;
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
                    if (environment.getType().name().equalsIgnoreCase(value)) {
                        DescriptorHelper.override(jsonDesc, getOverrideJson(override));
                    }
                }
            }
        }
    }

    private ObjectNode getOverrideJson(JsonNode override) throws ProvisioningException {
        final JsonNode overrideJson = override.get("override");
        if (isNull(overrideJson)) {
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
            return new CHDeploymentOperation(request, environment, source);
        } else if (target.startsWith("rtf:")) {
            return new RTFDeploymentOperation(organization.findFabricByName(target.substring(4)), request, environment, source);
        } else {
            try {
                return new HDeploymentOperation(request, environment.findServerByName(target), source);
            } catch (NotFoundException e) {
                return new RTFDeploymentOperation(organization.findFabricByName(target), request, environment, source);
            }
        }
    }

}
