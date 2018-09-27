package com.kloudtek.anypoint;

import com.kloudtek.anypoint.api.provision.APIProvisioningConfig;
import com.kloudtek.anypoint.runtime.DeploymentResult;
import com.kloudtek.anypoint.util.MavenUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Map;

public abstract class AbstractDeployMojo extends AbstractMojo {
    /**
     * Anypoint username
     */
    @Parameter(property = "anypoint.username", required = true)
    protected String username;
    /**
     * Anypoint password
     */
    @Parameter(property = "anypoint.password", required = true)
    protected String password;
    /**
     * Anypoint organization name
     */
    @Parameter(name = "org", property = "anypoint.org", required = true)
    protected String org;
    /**
     * Anypoint Environment name
     */
    @Parameter(name = "env", property = "anypoint.env", required = true)
    protected String env;
    /**
     * If true API provisioning will be skipped
     */
    @Parameter(property = "anypoint.api.provisioning.skip", required = false)
    protected boolean skipApiProvisioning;
    /**
     * If true deployment will be skipped
     */
    @Parameter(property = "anypoint.deploy.skip", required = false)
    protected boolean skipDeploy;
    /**
     * File to deploy (only needed when invoking standalone without a valid pom)
     */
    @Parameter(property = "anypoint.deploy.file", required = false)
    protected File file;
    /**
     * Filename (if not specified the file's name will be used)
     */
    @Parameter(property = "anypoint.deploy.filename", required = false)
    protected String filename;
    /**
     * Application name
     */
    @Parameter(property = "anypoint.deploy.name", required = false)
    protected String appName;
    /**
     * Force deployment even if same already deployed application exists
     */
    @Parameter(property = "anypoint.deploy.force", required = false)
    protected boolean force;
    /**
     * If true will skip wait for application to start (successfully or not)
     */
    @Parameter(property = "anypoint.deploy.skipwait", required = false)
    protected boolean skipWait;
    /**
     * Deployment timeout
     */
    @Parameter(property = "anypoint.deploy.timeout", required = false)
    protected long deployTimeout = 120000L;
    /**
     * Delay (in milliseconds) in retrying a deployment
     */
    @Parameter(property = "anypoint.deploy.retrydelay", required = false)
    protected long deployRetryDelay = 2500L;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        if (!skipDeploy) {
            try {
                MavenProject project = (MavenProject) getPluginContext().get("project");
                if (MavenUtils.isTemplateOrExample(project) && !force) {
                    log.warn("Project contains mule-application-template or mule-application-example, skipping deployment (use anypoint.deploy.force to force the deployment)");
                    return;
                }
                if (file == null) {
                    log.debug("No deploy file defined");
                    if (project == null) {
                        throw new MojoExecutionException("File not specified while running out of project");
                    }
                    file = MavenUtils.getProjectJar(project, log);
                }
                if (filename == null) {
                    filename = file.getName();
                }
                if (appName == null) {
                    if (project != null) {
                        appName = project.getArtifactId();
                    } else {
                        appName = file.getName();
                    }
                }
                AnypointClient client = new AnypointClient(username, password);
                log.debug("Searching for org " + org);
                Organization o = client.findOrganization(org);
                log.debug("Found org " + org + " : " + o.getId());
                log.debug("Searching for env " + env);
                Environment e = o.findEnvironment(env);
                log.debug("Found env " + env + " : " + e.getId());
                APIProvisioningConfig apiProvisioningConfig = skipApiProvisioning ? null : new APIProvisioningConfig();
                DeploymentResult app = deploy(e, apiProvisioningConfig);
                if (!skipWait) {
                    log.info("Waiting for application start");
                    app.waitDeployed(deployTimeout, deployRetryDelay);
                    log.info("Application started successfully");
                }
                log.info("Deployment completed successfully");
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    protected abstract DeploymentResult deploy(Environment env, APIProvisioningConfig apiProvisioningConfig) throws MojoExecutionException, HttpException;
}