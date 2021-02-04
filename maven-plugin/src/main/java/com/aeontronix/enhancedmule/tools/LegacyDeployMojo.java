/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.RTFDeploymentConfig;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment.CloudhubDeploymentParameters;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment.DeploymentParameters;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment.RTFDeploymentParameters;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.time.Duration;

public abstract class LegacyDeployMojo extends AbstractEnvironmentalMojo {
    /**
     * Application name
     */
    @Parameter(property = "anypoint.deploy.name.chsuffix")
    protected String appNameCHSuffix;
    /**
     * Application name
     */
    @Parameter(property = "anypoint.deploy.name.chsuffixnponly")
    protected boolean appNameCHSuffixNPOnly;
    /**
     * Application name cloudhub prefix
     */
    @Parameter(property = "anypoint.deploy.name.chprefix")
    protected String appNameCHPrefix;
    /**
     * If true, will force deployment even if same already application was already deployed.
     */
    @Parameter(property = "anypoint.deploy.force")
    protected boolean force;
    /**
     * If true will skip wait for application to start (successfully or not)
     */
    @Parameter(property = "anypoint.deploy.skipwait")
    protected boolean skipWait;
    /**
     * Deployment timeout
     */
    @Parameter(property = "anypoint.deploy.timeout")
    protected Long deployTimeout; //DONE
    /**
     * Delay (in milliseconds) in retrying a deployment
     */
    @Parameter(property = "anypoint.deploy.retrydelay")
    protected Long deployRetryDelay;
    /**
     * Application name
     */
    @Parameter(property = "anypoint.deploy.name")
    protected String appName;
    /**
     * Anypoint target name (Server / Server Group / Cluster). If not set will deploy to Cloudhub
     */
    @Parameter(name = "target", property = "anypoint.deploy.target")
    protected String target; //DONE
    /**
     * Deprecated, use chMuleVersionName
     */
    @Parameter(name = "muleVersionName", property = "anypoint.deploy.ch.muleversion", required = false)
    @Deprecated
    protected String muleVersionName;
    /**
     *
     */
    @Parameter(property = "anypoint.deploy.ch.runtime.version", required = false)
    protected String chMuleVersionName;
    /**
     * Cloudhub only: Deployment region
     */
    @Parameter(name = "region", property = "anypoint.deploy.ch.region", required = false)
    protected String region;

    /**
     * Cloudhub only: Worker type (will default to smallest if not specified)
     */
    @Parameter(name = "workerType", property = "anypoint.deploy.ch.worker.type", required = false)
    protected String workerType;

    /**
     * Cloudhub only: Worker count (will default to one if not specified).
     */
    @Parameter(name = "workerCount", property = "anypoint.deploy.ch.worker.count")
    protected Integer workerCount;
    /**
     * Cloudhub only: If true custom log4j will be used (and cloudhub logging disabled)
     */
    @Parameter(name = "customlog4j", property = "anypoint.deploy.ch.customlog4j")
    protected Boolean customlog4j;
    /**
     * Specified if environment info should be injected
     */
    @Parameter(property = "anypoint.deploy.injectEnvInfo", defaultValue = "true")
    protected Boolean injectEnvInfo;
    /**
     * Indicates if existing application properties should be merged
     */
    @Parameter(property = "anypoint.deploy.mergeproperties", defaultValue = "true")
    protected Boolean mergeExistingProperties;
    /**
     * Indicates the behavior to use when merging conflicting properties. If true it will override the existing property, or if false it will override it.
     */
    @Parameter(property = "anypoint.deploy.mergeproperties.override")
    protected Boolean mergeExistingPropertiesOverride;
    /**
     * Enable persistent queues
     */
    @Parameter(property = "anypoint.deploy.persistentqueue", defaultValue = "false")
    protected Boolean persistentQueues;
    /**
     * Enable encryption for persistent queues
     */
    @Parameter(property = "anypoint.deploy.persistentqueue.encrypted", defaultValue = "false")
    protected Boolean persistentQueuesEncrypted;
    /**
     * Set object store v1 instead of v2
     */
    @Parameter(property = "anypoint.deploy.objectstorev1", defaultValue = "false")
    protected Boolean objectStoreV1;
    /**
     * Enable monitoring and visualizer
     */
    @Parameter(property = "anypoint.deploy.extMonitoring", defaultValue = "true")
    protected Boolean extMonitoring;
    /**
     * Enable static ips
     */
    @Parameter(property = "anypoint.deploy.staticips", defaultValue = "false")
    protected Boolean staticIPs;
    @Parameter(property = "anypoint.deploy.rtf.cpu.reserved")
    protected String cpuReserved;
    @Parameter(property = "anypoint.deploy.rtf.cpu.limit")
    protected String cpuLimit;
    @Parameter(property = "anypoint.deploy.rtf.memory.reserved")
    protected String memoryReserved;
    @Parameter(property = "anypoint.deploy.rtf.memory.limit")
    protected String memoryLimit;
    @Parameter(property = "anypoint.deploy.rtf.clustered")
    protected Boolean clustered;
    @Parameter(property = "anypoint.deploy.rtf.xnodereplicas")
    protected Boolean enforceDeployingReplicasAcrossNodes;
    @Parameter(property = "anypoint.deploy.rtf.http.inbound.publicUrl")
    protected String httpInboundPublicUrl;
    @Parameter(property = "anypoint.deploy.rtf.jvm.args")
    protected String jvmArgs;
    @Parameter(property = "anypoint.deploy.rtf.runtime.version")
    protected String rtfRuntimeVersion;
    @Parameter(property = "anypoint.deploy.rtf.lastmilesecurity")
    protected Boolean lastMileSecurity;
    @Parameter(property = "anypoint.deploy.rtf.forwardSslSession")
    protected Boolean forwardSslSession;
    @Parameter(property = "anypoint.deploy.rtf.updatestrategy")
    protected RTFDeploymentConfig.DeploymentModel updateStrategy;
    @Parameter(property = "anypoint.deploy.rtf.replicas")
    protected Integer replicas;

    public JsonNode getLegacyAppDescriptor() throws IOException {
        final ApplicationDescriptor app = new ApplicationDescriptor();
        final DeploymentParameters deploymentParameters = new DeploymentParameters();
        app.setDeploymentParams(deploymentParameters);
        deploymentParameters.setTarget(target);
        deploymentParameters.setDeployTimeout(deployTimeout != null ? Duration.ofMillis(deployTimeout) : null);
        deploymentParameters.setDeployRetryDelay(deployRetryDelay != null ? Duration.ofMillis(deployRetryDelay) : null);
        deploymentParameters.setMergeExistingProperties(mergeExistingProperties);
        deploymentParameters.setMergeExistingPropertiesOverride(mergeExistingPropertiesOverride);
        deploymentParameters.setMergeExistingPropertiesOverride(mergeExistingPropertiesOverride);
        deploymentParameters.setExtMonitoring(extMonitoring);
        final CloudhubDeploymentParameters ch = deploymentParameters.getCloudhub();
        ch.setAppNamePrefix(appNameCHPrefix);
        ch.setAppNameSuffixNPOnly(appNameCHSuffixNPOnly);
        ch.setAppNameSuffix(appNameCHSuffix);
        ch.setMuleVersion(chMuleVersionName);
        ch.setPersistentQueues(persistentQueues);
        ch.setPersistentQueuesEncrypted(persistentQueuesEncrypted);
        ch.setObjectStoreV1(objectStoreV1);
        ch.setCustomlog4j(customlog4j);
        ch.setStaticIPs(staticIPs);
        ch.setRegion(region);
        ch.setWorkerType(workerType);
        ch.setWorkerCount(workerCount);
        final RTFDeploymentParameters rtf = deploymentParameters.getRtf();
        rtf.setCpuReserved(cpuReserved);
        rtf.setCpuLimit(cpuLimit);
        rtf.setMemoryReserved(memoryReserved);
        rtf.setMemoryLimit(memoryLimit);
        rtf.setClustered(clustered);
        rtf.setEnforceDeployingReplicasAcrossNodes(enforceDeployingReplicasAcrossNodes);
        rtf.setHttpInboundPublicUrl(httpInboundPublicUrl);
        rtf.setJvmArgs(jvmArgs);
        rtf.setRuntimeVersion(rtfRuntimeVersion);
        rtf.setLastMileSecurity(lastMileSecurity);
        rtf.setForwardSslSession(forwardSslSession);
        rtf.setUpdateStrategy(updateStrategy);
        rtf.setReplicas(replicas);
        return getClient().getJsonHelper().getJsonMapper().valueToTree(app);
    }
}
