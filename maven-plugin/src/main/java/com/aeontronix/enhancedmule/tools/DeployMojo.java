/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.DeploymentServiceImpl;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.DescriptorLayers;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.ExchangeDeploymentRequest;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.RuntimeDeploymentRequest;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.EMTProperties;
import com.aeontronix.enhancedmule.tools.util.MavenUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.aeontronix.commons.StringUtils.isNotBlank;

/**
 * Deploy an application to Cloudhub or On-Prem/Hybrid
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Mojo(name = "deploy", requiresProject = false, defaultPhase = LifecyclePhase.DEPLOY)
public class DeployMojo extends LegacyDeployMojo {
    private static final Logger logger = LoggerFactory.getLogger(DeployMojo.class);
    private static final EMTLogger emtLogger = new EMTLogger(logger);
    public static final String VAR = "emt.var.";
    public static final String CLOUDHUB = "cloudhub";
    public static final String EMT_SECUREPROPERTIES = "emt.secureproperties";
    public static final String SECURE_PREFIX = "emt.secureproperty.";
    /**
     * If true API provisioning will be skipped
     */
    @Parameter
    protected boolean skipProvisioning;
    /**
     * If true deployment will be skipped
     */
    @Parameter
    protected boolean skipDeploy;
    /**
     * File to deploy (only needed when invoking standalone without a valid pom). To deploy from exchange use uri in the format
     * of <pre>exchange://[orgId]:[groupId]:[artifactId]:[version]</pre> or <pre>exchange://[groupId]:[artifactId]:[version]</pre>
     */
    @Parameter
    protected String appFile;
    /**
     * Filename (if not specified the file's name will be used)
     */
    @Parameter
    protected String appFilename;
    /**
     * Application properties
     */
    @Parameter(property = "anypoint.deploy.properties", required = false)
    protected Map<String, String> properties;
    /**
     * Application property file
     */
    @Parameter
    protected String propertyfile;
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
     * Is this is specified, the provided build number is used as a suffix to SNAPSHOT versions published to exchange
     */
    @Parameter(property = "anypoint.deploy.buildnumber")
    private String buildNumber;
    /**
     * If this is set to true, and a snapshot is deployed to RTF, older snapshots will be deleted
     */
    @Parameter(property = "emt.provisioning.deletesnapshots")
    private Boolean deleteSnapshots;
    @Parameter
    private String target;

    @Override
    protected void doExecute() throws Exception {
        skipDeploy = getMavenProperty("emt.deploy.skip", skipDeploy, "anypoint.deploy.skip");
        if (!skipDeploy) {
            EMTProperties emtProperties = getEMTProperties();
            propertyfile = emtProperties.getProperty("emt.deploy.properties.file", propertyfile, "anypoint.deploy.propertyfile");
            skipProvisioning = emtProperties.getProperty("emt.provisioning.skip", skipProvisioning, "emt.skipProvisioning", "anypoint.api.provisioning.skip");
            appFile = emtProperties.getProperty("emt.app.file", appFile, "anypoint.deploy.file");
            appFilename = emtProperties.getProperty("emt.app.filename", appFilename, "anypoint.deploy.filename");
            target = emtProperties.getProperty("emt.target", target, "anypoint.deploy.target", "anypoint.target");
            appName = emtProperties.getProperty("emt.app.name", appName, "anypoint.deploy.name");
            handleDeprecated();
            if (project.getArtifactId().equals("standalone-pom") && project.getGroupId().equals("org.apache.maven")) {
                project = null;
            }
            if (MavenUtils.isTemplateOrExample(project) && !force) {
                logger.warn("Project contains mule-application-template or mule-application-example, skipping deployment (use anypoint.deploy.force to force the deployment)");
                return;
            }
            if (appFile == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No deploy file defined");
                }
                if (project == null) {
                    throw new MojoExecutionException("File not specified while running out of project");
                }
                appFile = MavenUtils.getProjectJar(project).getPath();
            }
            ApplicationIdentifier applicationIdentifier = project != null ? new ApplicationIdentifier(project.getGroupId(), project.getArtifactId(), project.getVersion()) : null;
            Organization organization = getOrganization();
            final DeploymentServiceImpl deploymentService = new DeploymentServiceImpl(organization.getClient(), anypointClient);
            try (ApplicationSource source = ApplicationSource.create(organization.getId(), getLegacyClient(), appFile)) {
                if (target != null && target.equalsIgnoreCase("exchange")) {
                    final ExchangeDeploymentRequest req;
                    req = new ExchangeDeploymentRequest(buildNumber, applicationIdentifier, organization, source, null);
                    final ApplicationIdentifier appId = deploymentService.deployToExchange(req);
                    emtLogger.info(EMTLogger.Product.EXCHANGE, "Published application to exchange: " + appId.getGroupId() + ":" + appId.getArtifactId() + ":" + appId.getVersion());
                } else {
                    if (vars == null) {
                        vars = new HashMap<>();
                    }
                    vars.putAll(emtProperties.getPrefixedProperties(VAR, true));
                    logger.info("Variables in use: " + String.join(", ", vars.keySet()));
                    if (properties == null) {
                        properties = new HashMap<>();
                    }
                    properties.putAll(emtProperties.getPrefixedProperties("anypoint.deploy.properties.", true));
                    properties.putAll(emtProperties.getPrefixedProperties("emt.property.", true));
                    HashSet<String> secureProperties = new HashSet<>();
                    for (Map.Entry<String, String> e : emtProperties.getProperties().entrySet()) {
                        if (e.getKey().startsWith(SECURE_PREFIX) && "true".equalsIgnoreCase(e.getValue())) {
                            secureProperties.add(e.getKey().substring(SECURE_PREFIX.length()));
                        }
                    }
                    JsonNode deploymentParametersOverridesLegacy = getDeploymentParametersOverrides();
                    Environment environment = getEnvironment();
                    // todo remove this later
                    environment.getOrganization().validateObject();
                    final RuntimeDeploymentRequest request = new RuntimeDeploymentRequest(appFilename != null ? appFilename :
                            source.getFileName(), appName, source.getArtifactId(), buildNumber, vars, properties,
                            isNotBlank(propertyfile) ? new File(propertyfile) : null,
                            ignoreMissingPropertyFile, target, environment, injectEnvInfo, skipWait, skipProvisioning,
                            deploymentParametersOverridesLegacy);
                    request.addSecureProperties(secureProperties);
                    request.setFileProperties(fileProperties);
                    request.setFilePropertiesPath(filePropertiesPath);
                    request.setFilePropertiesSecure(filePropertiesSecure);
                    request.setDeleteSnapshots(deleteSnapshots != null && deleteSnapshots);
                    ObjectNode appDescJson = source.getAnypointDescriptor();
                    deploymentService.deploy(request, appDescJson, new DescriptorLayers(emtProperties), source);
                }
            }
        } else {
            logger.info("Deployment skipped");
        }
    }

    private void handleDeprecated() {
        if (chMuleVersionName == null && muleVersionName != null) {
            logger.warn("muleVersionName (anypoint.deploy.ch.muleversion) is deprecated, please use chMuleVersionName (anypoint.deploy.ch.runtime.version) instead");
            chMuleVersionName = muleVersionName;
        }
    }
}
