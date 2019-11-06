/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.api.provision.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.deploy.DeploymentConfig;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.util.MavenUtils;
import com.kloudtek.util.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class AbstractDeployMojo extends AbstractEnvironmentalMojo {
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
     * Force deployment even if same already deployed application exists
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
     * Indicates if existing application properties should be merged
     */
    @Parameter(property = "anypoint.deploy.mergeproperties")
    private boolean mergeExistingProperties;
    /**
     * Indicates the behavior to use when merging conflicting properties. If true it will override the existing property, or if false it will override it.
     */
    @Parameter(property = "anypoint.deploy.mergeproperties.override")
    private boolean mergeExistingPropertiesOverride;
    /**
     * Provisioning variables
     */
    @Parameter
    protected HashMap<String, String> vars;
    protected ApplicationSource source;

    @Override
    protected void doExecute() throws Exception {
        if (!skipDeploy) {
            MavenProject project = (MavenProject) getPluginContext().get("project");
            if (project.getArtifactId().equals("standalone-pom") && project.getGroupId().equals("org.apache.maven")) {
                project = null;
            }
            if (MavenUtils.isTemplateOrExample(project) && !force) {
                logger.warn("Project contains mule-application-template or mule-application-example, skipping deployment (use anypoint.deploy.force to force the deployment)");
                return;
            }
            if (file == null) {
                logger.debug("No deploy file defined");
                if (project == null) {
                    throw new MojoExecutionException("File not specified while running out of project");
                }
                file = MavenUtils.getProjectJar(project, logger).getPath();
            }
            source = ApplicationSource.create(getEnvironment().getOrganization().getId(), getClient(), file);
            try {
                if (filename == null) {
                    filename = source.getFileName();
                }
                if (appName == null) {
                    if (project != null) {
                        appName = project.getArtifactId();
                    } else {
                        appName = source.getArtifactId();
                    }
                }
                APIProvisioningConfig apiProvisioningConfig = null;
                if (!skipApiProvisioning) {
                    apiProvisioningConfig = new APIProvisioningConfig();
                    if (vars != null) {
                        apiProvisioningConfig.setVariables(vars);
                    }
                }
                DeploymentConfig deploymentConfig = new DeploymentConfig();
                deploymentConfig.setProperties(properties);
                deploymentConfig.setMergeExistingProperties(mergeExistingProperties);
                deploymentConfig.setMergeExistingPropertiesOverride(mergeExistingPropertiesOverride);
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

    protected abstract DeploymentResult deploy(Environment env, APIProvisioningConfig apiProvisioningConfig, DeploymentConfig deploymentConfig) throws Exception;
}
