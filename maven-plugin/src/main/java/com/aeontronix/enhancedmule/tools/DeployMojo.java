/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.application.MavenHelper;
import com.aeontronix.enhancedmule.tools.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.application.deploy.RTFDeploymentConfig;
import com.aeontronix.enhancedmule.tools.fabric.Fabric;
import com.aeontronix.enhancedmule.tools.legacy.deploy.*;
import com.aeontronix.enhancedmule.tools.legacy.deploy.rtf.RTFDeployer;
import com.aeontronix.enhancedmule.tools.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Server;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.MavenUtils;
import com.aeontronix.unpack.UnpackException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Deploy an application to Cloudhub or On-Prem/Hybrid
 */
@Mojo(name = "deploy", requiresProject = false, defaultPhase = LifecyclePhase.DEPLOY)
public class DeployMojo extends AbstractEnvironmentalMojo {
    public static final String ANYPOINT_DEPLOY_PROPERTIES = "anypoint.deploy.properties.";
    private static final Logger logger = LoggerFactory.getLogger(DeployMojo.class);
    private static final EMTLogger elogger = new EMTLogger(logger);
    /**
     * If true API provisioning will be skipped
     */
    @Parameter(property = "anypoint.api.provisioning.skip")
    protected boolean skipApiProvisioning;
    /**
     * If true deployment will be skipped
     */
    @Parameter(property = "anypoint.deploy.skip")
    protected boolean skipDeploy;
    /**
     * File to deploy (only needed when invoking standalone without a valid pom). To deploy from exchange use uri in the format
     * of <pre>exchange://[orgId]:[groupId]:[artifactId]:[version]</pre> or <pre>exchange://[groupId]:[artifactId]:[version]</pre>
     */
    @Parameter(property = "anypoint.deploy.file")
    protected String file;
    /**
     * Filename (if not specified the file's name will be used)
     */
    @Parameter(property = "anypoint.deploy.filename")
    protected String filename;
    /**
     * Application name
     */
    @Parameter(property = "anypoint.deploy.name")
    protected String appName;
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
    protected long deployTimeout = TimeUnit.MINUTES.toMillis(10);
    /**
     * Delay (in milliseconds) in retrying a deployment
     */
    @Parameter(property = "anypoint.deploy.retrydelay")
    protected long deployRetryDelay = 2500L;
    /**
     * Application properties
     */
    @Parameter(property = "anypoint.deploy.properties", required = false)
    protected Map<String, String> properties;
    /**
     * Application property file
     */
    @Parameter(property = "anypoint.deploy.propertyfile", required = false)
    protected File propertyfile;

    /**
     * Properties that should be inserted into a property file in the application archive
     *
     * @see #filePropertiesPath
     */
    @Parameter(property = "anypoint.deploy.fileproperties", required = false)
    protected Map<String, String> fileProperties;

    /**
     * Location of property file to inserted with values specified in {@link #fileProperties}
     */
    @Parameter(property = "anypoint.deploy.fileproperties.path", required = false, defaultValue = "config.properties")
    protected String filePropertiesPath = "config.properties";
    /**
     * If set to true, all secure properties will be inserted in {@link #fileProperties} rather than in Runtime Manager.
     */
    @Parameter(property = "anypoint.deploy.fileproperties.secure", required = false, defaultValue = "false")
    protected boolean filePropertiesSecure;
    /**
     * Provisioning variables
     */
    @Parameter
    protected HashMap<String, String> vars;
    protected ApplicationSource source;
    /**
     * Anypoint target name (Server / Server Group / Cluster). If not set will deploy to Cloudhub
     */
    @Parameter(property = "anypoint.target")
    private String legacyTarget;
    /**
     * Anypoint target name (Server / Server Group / Cluster). If not set will deploy to Cloudhub
     */
    @Parameter(name = "target", property = "anypoint.deploy.target")
    private String target;
    /**
     * Deprecated, use chMuleVersionName
     */
    @Parameter(name = "muleVersionName", property = "anypoint.deploy.ch.muleversion", required = false)
    @Deprecated
    private String muleVersionName;
    /**
     *
     */
    @Parameter(property = "anypoint.deploy.ch.runtime.version", required = false)
    private String chMuleVersionName;
    /**
     * Cloudhub only: Deployment region
     */
    @Parameter(name = "region", property = "anypoint.deploy.ch.region", required = false)
    private String region;

    /**
     * Cloudhub only: Worker type (will default to smallest if not specified)
     */
    @Parameter(name = "workerType", property = "anypoint.deploy.ch.worker.type", required = false)
    private String workerType;

    /**
     * Cloudhub only: Worker count (will default to one if not specified).
     */
    @Parameter(name = "workerCount", property = "anypoint.deploy.ch.worker.count")
    private Integer workerCount;
    /**
     * Cloudhub only: If true custom log4j will be used (and cloudhub logging disabled)
     */
    @Parameter(name = "customlog4j", property = "anypoint.deploy.ch.customlog4j")
    private boolean customlog4j;
    /**
     * Specified if environment info should be injected
     */
    @Parameter(property = "anypoint.deploy.injectEnvInfo", defaultValue = "true")
    private boolean injectEnvInfo;
    /**
     * Indicates if existing application properties should be merged
     */
    @Parameter(property = "anypoint.deploy.mergeproperties", defaultValue = "true")
    private boolean mergeExistingProperties;
    /**
     * Indicates the behavior to use when merging conflicting properties. If true it will override the existing property, or if false it will override it.
     */
    @Parameter(property = "anypoint.deploy.mergeproperties.override")
    private boolean mergeExistingPropertiesOverride;
    /**
     * Enable persistent queues
     */
    @Parameter(property = "anypoint.deploy.persistentqueue", defaultValue = "false")
    private boolean persistentQueues;
    /**
     * Enable encryption for persistent queues
     */
    @Parameter(property = "anypoint.deploy.persistentqueue.encrypted", defaultValue = "false")
    private boolean persistentQueuesEncrypted;
    /**
     * Set object store v1 instead of v2
     */
    @Parameter(property = "anypoint.deploy.objectstorev1", defaultValue = "false")
    private boolean objectStoreV1;
    /**
     * Enable monitoring and visualizer
     */
    @Parameter(property = "anypoint.deploy.extMonitoring", defaultValue = "true")
    private boolean extMonitoring = true;
    /**
     * Enable static ips
     */
    @Parameter(property = "anypoint.deploy.staticips", defaultValue = "false")
    private boolean staticIPs;
    /**
     * Build number
     */
    @Parameter(property = "anypoint.deploy.buildnumber")
    private String buildNumber;
    @Parameter(property = "anypoint.deploy.rtf.cpu.reserved",defaultValue = "20m")
    private String cpuReserved;
    @Parameter(property = "anypoint.deploy.rtf.cpu.limit",defaultValue = "1700m")
    private String cpuLimit;
    @Parameter(property = "anypoint.deploy.rtf.memory.reserved",defaultValue = "700Mi")
    private String memoryReserved;
    @Parameter(property = "anypoint.deploy.rtf.memory.limit",defaultValue = "700Mi")
    private String memoryLimit;
    @Parameter(property = "anypoint.deploy.rtf.clustered",defaultValue = "false")
    private boolean clustered;
    @Parameter(property = "anypoint.deploy.rtf.xnodereplicas",defaultValue = "false")
    private boolean enforceDeployingReplicasAcrossNodes;
    @Parameter(property = "anypoint.deploy.rtf.http.inbound.publicUrl")
    private String httpInboundPublicUrl;
    @Parameter(property = "anypoint.deploy.rtf.jvm.args")
    private String jvmArgs;
    @Parameter(property = "anypoint.deploy.rtf.runtime.version")
    private String rtfRuntimeVersion;
    @Parameter(property = "anypoint.deploy.rtf.lastmilesecurity")
    private boolean lastMileSecurity;
    @Parameter(property = "anypoint.deploy.rtf.forwardSslSession")
    private boolean forwardSslSession;
    @Parameter(property = "anypoint.deploy.rtf.updatestrategy",defaultValue = "ROLLING")
    private RTFDeploymentConfig.DeploymentModel updateStrategy;
    @Parameter(property = "anypoint.deploy.rtf.replicas",defaultValue = "1")
    private int replicas;

    @Override
    protected void doExecute() throws Exception {
        if (!skipDeploy) {
            if( target == null && legacyTarget != null ) {
                target = legacyTarget;
            }
            logger.debug("Deploy target: {}",target);
            // handle deprecated vars
            if( chMuleVersionName == null && muleVersionName != null ) {
                logger.warn("muleVersionName (anypoint.deploy.ch.muleversion) is deprecated, please use chMuleVersionName (anypoint.deploy.ch.runtime.version) instead");
                chMuleVersionName = muleVersionName;
            }
            if (project.getArtifactId().equals("standalone-pom") && project.getGroupId().equals("org.apache.maven")) {
                project = null;
            }
            if (MavenUtils.isTemplateOrExample(project) && !force) {
                logger.warn("Project contains mule-application-template or mule-application-example, skipping deployment (use anypoint.deploy.force to force the deployment)");
                return;
            }
            if (file == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No deploy file defined");
                }
                if (project == null) {
                    throw new MojoExecutionException("File not specified while running out of project");
                }
                file = MavenUtils.getProjectJar(project).getPath();
            }
            source = ApplicationSource.create(getOrganization().getId(), getClient(), file);
            if (appName == null) {
                if (project != null) {
                    appName = project.getArtifactId();
                } else {
                    appName = source.getArtifactId();
                }
                if (StringUtils.isBlank(target) || "cloudhub".equalsIgnoreCase(target)) {
                    appName = appName + "-" + getEnvironment().getLName();
                }
            }
            try {
                if (filename == null) {
                    filename = source.getFileName();
                }
                APIProvisioningConfig apiProvisioningConfig = null;
                if (!skipApiProvisioning) {
                    apiProvisioningConfig = new APIProvisioningConfig();
                    if (vars != null) {
                        apiProvisioningConfig.setVariables(vars);
                    }
                    try {
                        apiProvisioningConfig.setEnvironment(getEnvironment());
                    } catch (NotFoundException e) {
                        apiProvisioningConfig.setOrganization(getOrganization());
                    }
                }
                DeploymentConfig deploymentConfig = new DeploymentConfig();
                deploymentConfig.setRtf(new RTFDeploymentConfig(cpuReserved, cpuLimit, memoryReserved, memoryLimit,
                        clustered, enforceDeployingReplicasAcrossNodes, httpInboundPublicUrl, jvmArgs, rtfRuntimeVersion,
                        lastMileSecurity, forwardSslSession, updateStrategy, replicas));
                if (propertyfile != null) {
                    if (!propertyfile.exists()) {
                        throw new IllegalArgumentException("Property file not found: " + propertyfile);
                    }
                    Properties fileProps = new Properties();
                    try (FileInputStream fis = new FileInputStream(propertyfile)) {
                        fileProps.load(fis);
                    }
                    for (Map.Entry<Object, Object> entry : fileProps.entrySet()) {
                        String key = entry.getKey().toString();
                        if (!properties.containsKey(key)) {
                            properties.put(key, entry.getValue().toString());
                        }
                    }
                }
                if(injectEnvInfo) {
                    try {
                        properties.put("anypoint.env.name",getEnvironment().getName());
                        properties.put("anypoint.env.id",getEnvironment().getId());
                        properties.put("anypoint.env.type",getEnvironment().getType().name());
                    } catch (NotFoundException e) {
                        logger.debug("No environment, skipping settings properties for env");
                    }
                    properties.put("anypoint.org.name",getOrganization().getName());
                    properties.put("anypoint.org.id",getOrganization().getId());
                }
                deploymentConfig.setProperties(properties);
                deploymentConfig.setMergeExistingProperties(mergeExistingProperties);
                deploymentConfig.setMergeExistingPropertiesOverride(mergeExistingPropertiesOverride);
                deploymentConfig.setFileProperties(fileProperties);
                deploymentConfig.setFilePropertiesPath(filePropertiesPath);
                deploymentConfig.setFilePropertiesSecure(filePropertiesSecure);
                DeploymentResult app = deploy(apiProvisioningConfig, deploymentConfig);
                if (app != null && !skipWait) {
                    elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Waiting for application start");
                    app.waitDeployed(deployTimeout, deployRetryDelay);
                    elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Application started successfully");
                }
                elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Deployment completed");
            } finally {
                IOUtils.close(source);
            }
        }
    }


    @SuppressWarnings("Duplicates")
    protected DeploymentResult deploy(@NotNull APIProvisioningConfig apiProvisioningConfig,
                                      @NotNull DeploymentConfig deploymentConfig) throws Exception {
        if( buildNumber == null ) {
            buildNumber = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS").format(LocalDateTime.now());
        }
        if (project != null) {
            findDeployProperties(project.getProperties());
        }
        findDeployProperties(session.getUserProperties());
        findDeployProperties(session.getSystemProperties());
        try (ApplicationSource applicationSource = ApplicationSource.create(getOrganization().getId(), getClient(), file)) {
            if (StringUtils.isBlank(target) || target.equalsIgnoreCase("cloudhub")) {
                if (workerCount == null) {
                    workerCount = 1;
                }
                try {
                    deploymentConfig.setCustomlog4j(customlog4j);
                    deploymentConfig.setPersistentQueues(persistentQueues);
                    deploymentConfig.setPersistentQueuesEncrypted(persistentQueuesEncrypted);
                    deploymentConfig.setObjectStoreV1(objectStoreV1);
                    deploymentConfig.setExtMonitoring(extMonitoring);
                    deploymentConfig.setStaticIPs(staticIPs);
                    return new CHDeployer(chMuleVersionName, region, workerType, workerCount, getEnvironment(), appName,
                            applicationSource, filename, apiProvisioningConfig, deploymentConfig).deploy();
                } catch (ProvisioningException | IOException | NotFoundException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            } else {
                try {
                    if (target.equalsIgnoreCase("exchange")) {
                        uploadToExchange(applicationSource);
                        return null;
                    } else {
                        try {
                            Server server = getEnvironment().findServerByName(target);
                            return new HDeployer(server, appName, applicationSource, filename,
                                    apiProvisioningConfig, deploymentConfig).deploy();
                        } catch (NotFoundException e) {
                            final Fabric fabric = getOrganization().findFabricByName(target);
                            final ApplicationIdentifier appId;
                            if( applicationSource instanceof FileApplicationSource ) {
                                appId = uploadToExchange(applicationSource);
                            } else {
                                ExchangeApplicationSource eApp = (ExchangeApplicationSource) applicationSource;
                                appId = new ApplicationIdentifier(eApp.getGroupId(),eApp.getArtifactId(),eApp.getVersion());
                            }
                            return new RTFDeployer(fabric, getEnvironment(), appName, applicationSource, filename,
                                    apiProvisioningConfig, deploymentConfig, appId).deploy();
                        }
                    }
                } catch (NotFoundException e) {
                    throw new MojoExecutionException("Target " + target + " not found in env " + getEnvironment().getName() + " in business group " + org);
                } catch (ProvisioningException | IOException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        }
    }

    private ApplicationIdentifier uploadToExchange(ApplicationSource applicationSource) throws IOException, UnpackException, NotFoundException {
        if (project != null) {
            return MavenHelper.uploadToMaven(new ApplicationIdentifier(project.getGroupId(), project.getArtifactId(), project.getVersion()), getOrganization(), applicationSource, null, buildNumber);
        } else {
            return MavenHelper.uploadToMaven(null, getOrganization(), applicationSource, null, buildNumber);
        }
    }

    private void findDeployProperties(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            if (key.startsWith(ANYPOINT_DEPLOY_PROPERTIES)) {
                key = key.substring(ANYPOINT_DEPLOY_PROPERTIES.length());
                if (StringUtils.isNotBlank(key)) {
                    String value = entry.getValue().toString();
                    if (this.properties == null) {
                        this.properties = new HashMap<>();
                    }
                    this.properties.put(key, value);
                }
            }
        }
    }

}
