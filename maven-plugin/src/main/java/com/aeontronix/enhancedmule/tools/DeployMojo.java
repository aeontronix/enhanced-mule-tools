/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.DeploymentServiceImpl;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.ExchangeDeploymentRequest;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.RuntimeDeploymentRequest;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.MavenUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * Deploy an application to Cloudhub or On-Prem/Hybrid
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Mojo(name = "deploy", requiresProject = false, defaultPhase = LifecyclePhase.DEPLOY)
public class DeployMojo extends LegacyDeployMojo {
    public static final String ANYPOINT_DEPLOY_PROPERTIES = "anypoint.deploy.properties.";
    private static final Logger logger = LoggerFactory.getLogger(DeployMojo.class);
    private static final EMTLogger emtLogger = new EMTLogger(logger);
    public static final String VAR = "var";
    public static final String CLOUDHUB = "cloudhub";
    /**
     * If true API provisioning will be skipped
     */
    @Parameter(property = "emt.skipProvisioning")
    protected boolean skipProvisioning;
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
     * Ignore missing application properties file
     */
    @Parameter(property = "anypoint.deploy.propertyfile.ignoremissing", required = false)
    protected boolean ignoreMissingPropertyFile;

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
    protected Map<String, String> vars;
    /**
     * Build number
     */
    @Parameter(property = "anypoint.deploy.buildnumber")
    private String buildNumber;
    @Parameter(property = "emt.provisioning.deletesnapshots")
    private Boolean deleteSnapshots;
    @Parameter(property = "anypoint.target")
    private String legacyTarget;
    /**
     * If true API provisioning will be skipped
     */
    @Parameter(property = "anypoint.api.provisioning.skip")
    @Deprecated
    protected boolean skipApiProvisioning;

    @Override
    protected void doExecute() throws Exception {
        if (!skipDeploy) {
            handleDeprecated();
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
            ApplicationIdentifier applicationIdentifier = project != null ? new ApplicationIdentifier(project.getGroupId(), project.getArtifactId(), project.getVersion()) : null;
            final DeploymentServiceImpl deploymentService = new DeploymentServiceImpl(getOrganization().getClient());
            try (ApplicationSource source = ApplicationSource.create(getOrganization().getId(), getClient(), file)) {
                if (target != null && target.equalsIgnoreCase("exchange")) {
                    final ExchangeDeploymentRequest req;
                    req = new ExchangeDeploymentRequest(buildNumber, applicationIdentifier, getOrganization(), source, null);
                    final ApplicationIdentifier appId = deploymentService.deployToExchange(req);
                    emtLogger.info(EMTLogger.Product.EXCHANGE, "Published application to exchange: " + appId.getGroupId() + ":" + appId.getArtifactId() + ":" + appId.getVersion());
                } else {
                    vars = findPrefixProperties(vars, VAR);
                    properties = findPrefixProperties(properties, ANYPOINT_DEPLOY_PROPERTIES);
                    final RuntimeDeploymentRequest request = new RuntimeDeploymentRequest(filename != null ? filename :
                            source.getFileName(), appName, source.getArtifactId(), buildNumber, vars, properties, propertyfile,
                            ignoreMissingPropertyFile, target, getEnvironment(), injectEnvInfo, skipWait, skipProvisioning, getLegacyAppDescriptor());
                    request.setFileProperties(fileProperties);
                    request.setFilePropertiesPath(filePropertiesPath);
                    request.setFilePropertiesSecure(filePropertiesSecure);
                    request.setDeleteSnapshots(deleteSnapshots != null && deleteSnapshots);
                    final ObjectNode appDescJson = source.getAnypointDescriptor();
                    deploymentService.deploy(request, appDescJson, source);
                }
            }
        }
    }

    private void handleDeprecated() {
        if (target == null && legacyTarget != null) {
            target = legacyTarget;
        }
        if (chMuleVersionName == null && muleVersionName != null) {
            logger.warn("muleVersionName (anypoint.deploy.ch.muleversion) is deprecated, please use chMuleVersionName (anypoint.deploy.ch.runtime.version) instead");
            chMuleVersionName = muleVersionName;
        }
        if (skipApiProvisioning && !skipProvisioning) {
            skipProvisioning = true;
        }
    }
}
