/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy.rtf;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.UnexpectedException;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.DeploymentOperation;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.RuntimeDeploymentRequest;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment.RTFDeploymentParameters;
import com.aeontronix.enhancedmule.tools.fabric.Fabric;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Target;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RTFDeploymentOperation extends DeploymentOperation {
    private final Fabric fabric;

    public RTFDeploymentOperation(Fabric fabric, RuntimeDeploymentRequest req, Environment environment, ApplicationSource applicationSource) {
        super(req, environment, applicationSource);
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
        if( replicas == null ) {
            replicas = 1;
        }
        target.put("replicas", replicas);
        final Map<String, Object> application = subMap(req, "application");
        final Map<String, Object> ref = subMap(application, "ref");
        final ApplicationIdentifier appId = source.getApplicationIdentifier();
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
        final String json = environment.getClient().getHttpHelper().httpPost(new URLBuilder("/hybrid/api/v2/organizations")
                .path(environment.getOrganization().getId()).path("environments").path(environment.getId())
                .path("deployments")
                .toString(), req);
        return null;
    }
}
