/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy.rtf;

import com.aeontronix.commons.URLBuilder;
import com.aeontronix.enhancedmule.tools.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.application.deploy.RTFDeploymentConfig;
import com.aeontronix.enhancedmule.tools.fabric.Fabric;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.legacy.deploy.Deployer;
import com.aeontronix.enhancedmule.tools.legacy.deploy.DeploymentConfig;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RTFDeployer extends Deployer {
    private final Fabric fabric;
    private final String appName;
    private final ApplicationSource applicationSource;
    private final String filename;
    private final APIProvisioningConfig apiProvisioningConfig;
    private final DeploymentConfig deploymentConfig;
    private ApplicationIdentifier appId;

    public RTFDeployer(Fabric fabric, String appName, ApplicationSource applicationSource, String filename,
                       APIProvisioningConfig apiProvisioningConfig, DeploymentConfig deploymentConfig,
                       ApplicationIdentifier appId) {
        this.fabric = fabric;
        this.appName = appName;
        this.applicationSource = applicationSource;
        this.filename = filename;
        this.apiProvisioningConfig = apiProvisioningConfig;
        this.deploymentConfig = deploymentConfig;
        this.appId = appId;
    }

    @Override
    protected DeploymentResult doDeploy() throws IOException, HttpException {
        final RTFDeploymentConfig rtf = deploymentConfig.getRtf();
        Map<String,Object> req = new HashMap<>();
        req.put("name",appName);
        req.put("labels", Collections.singletonList("beta"));
        Map<String, Object> target = subMap(req, "target");
        target.put("provider","MC");
        target.put("targetId",fabric.getId());
        Map<String, Object> deploymentSettings = subMap(target, "deploymentSettings");
        Map<String, Object> resources = subMap(deploymentSettings, "resources");
        Map<String, Object> cpu = subMap(resources, "cpu");
        cpu.put("reserved",rtf.getCpuReserved());
        cpu.put("limit",rtf.getCpuLimit());
        Map<String, Object> memory = subMap(resources, "memory");
        memory.put("reserved",rtf.getMemoryReserved());
        memory.put("limit",rtf.getMemoryLimit());
        deploymentSettings.put("clustered",rtf.isClustered());
        deploymentSettings.put("enforceDeployingReplicasAcrossNodes",rtf.isEnforceDeployingReplicasAcrossNodes());
        if( rtf.getHttpInboundPublicUrl() != null ) {
            final Map<String, Object> http = subMap(deploymentSettings, "http");
            final Map<String, Object> inbound = subMap(http, "inbound");
            inbound.put("publicUrl",rtf.getHttpInboundPublicUrl());
        }
        final Map<String, Object> jvm = subMap(deploymentSettings, "jvm");
        if( rtf.getJvmArgs() != null ) {
            jvm.put("args",rtf.getJvmArgs());
        }
        deploymentSettings.put("runtimeVersion",rtf.getRuntimeVersion());
        deploymentSettings.put("lastMileSecurity",rtf.isLastMileSecurity());
        deploymentSettings.put("forwardSslSession",rtf.isForwardSslSession());
        deploymentSettings.put("updateStrategy",rtf.getUpdateStrategy() != null ? rtf.getUpdateStrategy().name().toLowerCase() : "rolling");
        target.put("replicas",rtf.getReplicas());
        final Map<String, Object> application = subMap(req, "application");
        final Map<String, Object> ref = subMap(application, "ref");
        ref.put("groupId",appId.getGroupId());
        ref.put("artifactId",appId.getArtifactId());
        ref.put("version",appId.getVersion());
        ref.put("packaging","jar");
        application.put("desiredState","STARTED");
        final Map<String, Object> configuration = subMap(application, "configuration");
        final Map<String, Object> properties = subMap(configuration, "mule.agent.application.properties.service");
        properties.put("applicationName",appName);
        properties.put("properties",deploymentConfig.getProperties());
        properties.put("secureproperties", Collections.emptyMap());
        final String json = environment.getClient().getHttpHelper().httpPost(new URLBuilder("/hybrid/api/v2/organizations")
                .path(environment.getParent().getId()).path("environments").path(environment.getId())
                .path("deployments")
                .toString(), req);
        return null;
    }

    @NotNull
    private static Map<String, Object> subMap(Map<String, Object> req, String name) {
        Map<String,Object> target = new HashMap<>();
        req.put(name,target);
        return target;
    }
}
