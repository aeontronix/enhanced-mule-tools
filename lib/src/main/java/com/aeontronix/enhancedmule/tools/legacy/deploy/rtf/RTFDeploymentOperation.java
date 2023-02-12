/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy.rtf;

import com.aeontronix.anypointsdk.AnypointClient;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.exception.UnexpectedException;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.DeploymentOperation;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.ExchangeDeploymentRequest;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.RuntimeDeploymentRequest;
import com.aeontronix.enhancedmule.tools.application.deployment.RTFDeploymentParameters;
import com.aeontronix.enhancedmule.tools.fabric.Fabric;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.legacy.deploy.FileApplicationSource;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Target;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.MavenHelper;
import com.aeontronix.enhancedmule.tools.util.UnauthorizedHttpException;
import com.aeontronix.restclient.RESTException;
import com.aeontronix.unpack.UnpackException;
import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class RTFDeploymentOperation extends DeploymentOperation {
    private static final Logger logger = getLogger(RTFDeploymentOperation.class);
    private static final EMTLogger emtLogger = new EMTLogger(logger);
    private AnypointClient anypointClient;
    private final Fabric fabric;

    public RTFDeploymentOperation(AnypointClient anypointClient, Fabric fabric, RuntimeDeploymentRequest req, Environment environment, ApplicationSource applicationSource) {
        super(req, environment, applicationSource);
        this.anypointClient = anypointClient;
        this.fabric = fabric;
    }

//    public RTFDeploymentOperation(Fabric fabric, Environment environment, String appName, ApplicationSource applicationSource, String filename,
//                                  APIProvisioningConfig apiProvisioningConfig, DeploymentConfig deploymentConfig,
//                                  , ProvisioningRequest provisioningRequest) {
//        super(environment, appName, applicationSource, filename, apiProvisioningConfig, deploymentConfig, provisioningRequest);
//        this.fabric = fabric;
//        this.appId = appId;
//    }

    @NotNull
    private static Map<String, Object> subMap(Map<String, Object> req, String name) {
        Map<String, Object> target = new HashMap<>();
        req.put(name, target);
        return target;
    }

    @Override
    protected DeploymentResult doDeploy(RuntimeDeploymentRequest request) throws IOException, HttpException {
        final RTFDeploymentParameters rtf = request.getApplicationDescriptor().getDeploymentParams().getRtf();
        if (StringUtils.isBlank(rtf.getRuntimeVersion())) {
            try {
                final Target target = environment.getOrganization().findTargetById(fabric.getId());
                Target.Runtime runtime = target.findRuntimeByType("mule");
                if (runtime == null) {
                    throw new IllegalArgumentException("Unable to find mule runtimes (no mule runtimes in fabric), please explicitly set runtime version");
                }
                final List<Target.RuntimeVersion> versions = runtime.getVersions();
                if (versions == null || versions.isEmpty()) {
                    throw new IllegalArgumentException("Unable to find mule runtimes version (no versions found), please explicitly set runtime version");
                }
                final Target.RuntimeVersion version = versions.get(0);
                rtf.setRuntimeVersion(version.getBaseVersion() + ":" + version.getTag());
            } catch (NotFoundException e) {
                throw new UnexpectedException("RTF Target not found: " + fabric.getId());
            }
        }
        ApplicationIdentifier appId = source.getApplicationIdentifier();
        if (source instanceof FileApplicationSource) {
            final ExchangeDeploymentRequest req = new ExchangeDeploymentRequest(request.getBuildNumber(), appId, getEnvironment().getOrganization(), source, null);
            try {
                appId = MavenHelper.uploadToMaven(anypointClient.getExchangeClient(), req.getAppId(), req.getOrg(), req.getApplicationSource(), null, req.getBuildNumber());
            } catch (UnpackException e) {
                throw new UnauthorizedHttpException(e);
            } catch (RESTException e) {
                throw new IOException(e);
            }
            emtLogger.info(EMTLogger.Product.EXCHANGE, "Published application to exchange: " + appId.getGroupId() + ":" + appId.getArtifactId() + ":" + appId.getVersion());
        }
        Map<String, Object> req = new HashMap<>();
        req.put("name", request.getAppName());
        req.put("labels", Collections.singletonList("beta"));
        Map<String, Object> target = subMap(req, "target");
        target.put("provider", "MC");
        target.put("targetId", fabric.getId());
        Map<String, Object> deploymentSettings = subMap(target, "deploymentSettings");
        Map<String, Object> resources = subMap(deploymentSettings, "resources");
        Map<String, Object> cpu = subMap(resources, "cpu");
        cpu.put("reserved", rtf.getCpuReserved());
        cpu.put("limit", rtf.getCpuLimit());
        Map<String, Object> memory = subMap(resources, "memory");
        memory.put("reserved", rtf.getMemoryReserved());
        memory.put("limit", rtf.getMemoryLimit());
        deploymentSettings.put("clustered", rtf.getClustered());
        deploymentSettings.put("enforceDeployingReplicasAcrossNodes", rtf.getEnforceDeployingReplicasAcrossNodes());
        final Map<String, Object> http = subMap(deploymentSettings, "http");
        final Map<String, Object> inbound = subMap(http, "inbound");
        inbound.put("publicUrl", rtf.getHttpInboundPublicUrl());
        final Map<String, Object> jvm = subMap(deploymentSettings, "jvm");
        if (rtf.getJvmArgs() != null) {
            jvm.put("args", rtf.getJvmArgs());
        }
        deploymentSettings.put("runtimeVersion", rtf.getRuntimeVersion());
        deploymentSettings.put("lastMileSecurity", rtf.getLastMileSecurity());
        deploymentSettings.put("forwardSslSession", rtf.getForwardSslSession());
        deploymentSettings.put("updateStrategy", rtf.getUpdateStrategy() != null ? rtf.getUpdateStrategy().name().toLowerCase() : "rolling");
        Integer replicas = rtf.getReplicas();
        if (replicas == null) {
            replicas = 1;
        }
        target.put("replicas", replicas);
        final Map<String, Object> application = subMap(req, "application");
        final Map<String, Object> ref = subMap(application, "ref");
        ref.put("groupId", appId.getGroupId());
        ref.put("artifactId", appId.getArtifactId());
        ref.put("version", appId.getVersion());
        ref.put("packaging", "jar");
        application.put("desiredState", "STARTED");
        final Map<String, Object> configuration = subMap(application, "configuration");
        final Map<String, Object> properties = subMap(configuration, "mule.agent.application.properties.service");
        properties.put("applicationName", request.getAppName());
        properties.put("properties", deploymentRequest.getProperties());
        properties.put("secureproperties", Collections.emptyMap());
        String deploymentId = getExistingAppDeploymentId(request.getAppName(), fabric.getId());

        if (StringUtils.isNotEmpty(deploymentId)) {
            final String json = environment.getClient().getHttpHelper()
                    .httpPatch(new URLBuilder("/hybrid/api/v2/organizations")
                            .path(environment.getOrganization().getId()).path("environments").path(environment.getId())
                            .path("deployments").path(deploymentId).toString(), req);

        } else {

            final String json = environment.getClient().getHttpHelper()
                    .httpPost(
                            new URLBuilder("/hybrid/api/v2/organizations").path(environment.getOrganization().getId())
                                    .path("environments").path(environment.getId()).path("deployments").toString(),
                            req);
        }
        return null;
    }

    private String getExistingAppDeploymentId(String appName, String targetId) throws HttpException {
        logger.debug("Searching for pre-existing RTF application named " + appName);
        final LegacyAnypointClient client = environment.getClient();
        final String deployments = client.getHttpHelper()
                .httpGet(new URLBuilder("/hybrid/api/v2/organizations").path(environment.getOrganization().getId())
                        .path("environments").path(environment.getId()).path("deployments").toString());
        if (deployments != null) {
            for (JsonNode node : client.getJsonHelper().readJsonTree(deployments).at("/items")) {
                if (appName.equalsIgnoreCase(node.get("name").asText())
                        && targetId.equalsIgnoreCase(node.get("target").get("targetId").asText())) {
                    return node.get("id").asText();
                }
            }
        }
        return null;
    }
}
