/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.deploy.CHDeploymentRequest;
import com.aeontronix.enhancedmule.tools.deploy.DeploymentConfig;
import com.aeontronix.enhancedmule.tools.deploy.HDeploymentRequest;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Server;
import com.aeontronix.enhancedmule.tools.util.MavenUtils;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.io.IOUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Deploy an application to Cloudhub or On-Prem/Hybrid
 */
@Mojo(name = "deploy", requiresProject = false, defaultPhase = LifecyclePhase.DEPLOY)
public class DeployMojo extends AbstractEnvironmentalMojo {
    private static final Logger logger = LoggerFactory.getLogger(DeployMojo.class);
    public static final String ANYPOINT_DEPLOY_PROPERTIES = "anypoint.deploy.properties.";
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

    /**
     * Anypoint target name (Server / Server Group / Cluster). If not set will deploy to Cloudhub
     */
    @Parameter(name = "target", property = "anypoint.target")
    private String target;
    protected ApplicationSource source;

    /**
     * Cloudhub only: Mule version name (will default to latest if not set)
     */
    @Parameter(name = "muleVersionName", property = "anypoint.deploy.ch.muleversion", required = false)
    private String muleVersionName;

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
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    @SuppressWarnings("Duplicates")
    protected DeploymentResult deploy(Environment environment,
                                      @NotNull APIProvisioningConfig apiProvisioningConfig,
                                      @NotNull DeploymentConfig deploymentConfig) throws Exception {
        if( project != null ) {
            findDeployProperties(project.getProperties());
        }
        findDeployProperties(session.getUserProperties());
        findDeployProperties(session.getSystemProperties());
        ApplicationSource applicationSource = ApplicationSource.create(environment.getOrganization().getId(), environment.getClient(), file);
        try {
            if (StringUtils.isBlank(target)) {
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
                    return new CHDeploymentRequest(muleVersionName, region, workerType, workerCount, environment, appName,
                            applicationSource, filename, apiProvisioningConfig, deploymentConfig).deploy();
                } catch (ProvisioningException | IOException | NotFoundException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            } else {
                try {
                    Server server = environment.findServerByName(target);
                    return new HDeploymentRequest(server, appName, applicationSource, filename,
                            apiProvisioningConfig, deploymentConfig).deploy();
                } catch (NotFoundException e) {
                    throw new MojoExecutionException("Target " + target + " not found in env " + environment + " in business group " + org);
                } catch (ProvisioningException | IOException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        } finally {
            IOUtils.close(applicationSource);
        }
    }

    private void findDeployProperties(Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            if( key.startsWith(ANYPOINT_DEPLOY_PROPERTIES) ) {
                key = key.substring(ANYPOINT_DEPLOY_PROPERTIES.length());
                if( StringUtils.isNotBlank(key) ) {
                    String value = entry.getValue().toString();
                    if( this.properties == null ) {
                        this.properties = new HashMap<>();
                    }
                    this.properties.put(key,value);
                }
            }
        }
    }

    @Override
    protected void doExecute() throws Exception {
        if (!skipDeploy) {
            if (project.getArtifactId().equals("standalone-pom") && project.getGroupId().equals("org.apache.maven")) {
                project = null;
            }
            if (MavenUtils.isTemplateOrExample(project) && !force) {
                logger.warn("Project contains mule-application-template or mule-application-example, skipping deployment (use anypoint.deploy.force to force the deployment)");
                return;
            }
            if( appName == null ) {
                appName = project.getArtifactId();
                if( StringUtils.isBlank(target) ) {
                    appName = appName + "-" + getEnvironment().getLName();
                }
            }
            if (file == null) {
                if( logger.isDebugEnabled() ) {
                    logger.debug("No deploy file defined");
                }
                if (project == null) {
                    throw new MojoExecutionException("File not specified while running out of project");
                }
                file = MavenUtils.getProjectJar(project).getPath();
            }
            source = ApplicationSource.create(getEnvironment().getOrganization().getId(), getClient(), file);
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
                    apiProvisioningConfig.init(getEnvironment());
                }
                DeploymentConfig deploymentConfig = new DeploymentConfig();
                if( propertyfile != null ) {
                    if( ! propertyfile.exists() ) {
                        throw new IllegalArgumentException("Property file not found: "+propertyfile);
                    }
                    Properties fileProps = new Properties();
                    try(FileInputStream fis = new FileInputStream(propertyfile) ) {
                        fileProps.load(fis);
                    }
                    for (Map.Entry<Object, Object> entry : fileProps.entrySet()) {
                        String key = entry.getKey().toString();
                        if( ! properties.containsKey(key) ) {
                            properties.put(key,entry.getValue().toString());
                        }
                    }
                }
                deploymentConfig.setProperties(properties);
                deploymentConfig.setMergeExistingProperties(mergeExistingProperties);
                deploymentConfig.setMergeExistingPropertiesOverride(mergeExistingPropertiesOverride);
                deploymentConfig.setFileProperties(fileProperties);
                deploymentConfig.setFilePropertiesPath(filePropertiesPath);
                deploymentConfig.setFilePropertiesSecure(filePropertiesSecure);
                DeploymentResult app = deploy(getEnvironment(), apiProvisioningConfig, deploymentConfig);
                if (!skipWait) {
                    logger.info("Waiting for application start");
                    app.waitDeployed(deployTimeout, deployRetryDelay);
                    logger.info("Application started successfully");
                }
                logger.info("Deployment completed successfully");
            } finally {
                IOUtils.close(source);
            }
        }
    }
}
