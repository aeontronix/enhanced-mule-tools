/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.application.MavenHelper;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment.DeploymentParameters;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.application.DeploymentException;
import com.aeontronix.enhancedmule.tools.fabric.Fabric;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.legacy.deploy.rtf.RTFDeploymentOperation;
import com.aeontronix.enhancedmule.tools.runtime.ApplicationDeploymentFailedException;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Server;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.aeontronix.unpack.UnpackException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class DeploymentServiceImpl {
    private static final Logger logger = getLogger(DeploymentServiceImpl.class);
    private static final EMTLogger elogger = new EMTLogger(logger);
    private AnypointClient client;

    public DeploymentServiceImpl(AnypointClient client) {
        this.client = client;
    }

    public void deployToExchange(ExchangeDeploymentRequest req) throws IOException, UnpackException {
        MavenHelper.uploadToMaven(req.getAppId(), req.getOrg(), req.getApplicationSource(), null, req.getBuildNumber());
    }

    public void deploy(RuntimeDeploymentRequest request, ObjectNode appDescJson, ApplicationSource source) throws DeploymentException, ProvisioningException {
        final String target = request.getTarget();
        final Environment environment = request.getEnvironment();
        final Organization organization = environment.getOrganization();
        try {
            DeploymentOperation op = createDeploymentOperation(request, source, target, environment, organization);
            request.setAppName(op.processAppName(request.getAppName()));
            JsonHelper.processVariables(appDescJson, request.getVars());
            ApplicationDescriptor applicationDescriptor = client.getJsonHelper().getJsonMapper().readerFor(ApplicationDescriptor.class).readValue(appDescJson);
            request.setApplicationDescriptor(applicationDescriptor);
            logger.info(Ansi.ansi().fgBrightYellow().a("Deploying application").toString());
            logger.info(Ansi.ansi().fgBrightYellow().a("Organization: ").reset().a(organization.getName()).toString());
            logger.info(Ansi.ansi().fgBrightYellow().a("Environment: ").reset().a(environment.getName()).toString());
            logger.info(Ansi.ansi().fgBrightYellow().a("Target: ").reset().a(target).toString());
            logger.info(Ansi.ansi().fgBrightYellow().a("App Name: ").reset().a(request.getAppName()).toString());
            final DeploymentResult result = op.deploy();
            waitForApplicationStart(request, result);
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Deployment completed");
        } catch (NotFoundException e) {
            throw new DeploymentException("Target " + target + " not found in env " + environment.getName() + " in business group " + organization.getName());
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
    }

    //    public DeploymentResult deploy(ApplicationDescriptor applicationDescriptor, RuntimeDeploymentRequest deploymentRequest,
//                                   DeploymentOperation op, ApplicationSource source) throws ProvisioningException, IOException, HttpException, DeploymentException {
//        final Environment environment = deploymentRequest.getEnvironment();
//        AnypointClient client = environment.getClient();
//        boolean tmpFile = false;
//        try {
//            environment = environment.refresh();
//            APIProvisioningResult provisioningResult = null;
//            List<Transformer> transformers = new ArrayList<>();
//            if (applicationDescriptor != null && !deploymentRequest.isSkipProvisioning() ) {
//                logger.debug("Found anypoint provisioning file, provisioning");
//                final Organization organization = environment.getOrganization();
//                final ApplicationProvisioningService applicationProvisioningService = new ApplicationProvisioningService(client);
//                boolean assetPublished = false;
//                final ExchangeManagementClient exchangeManagementClient = new ExchangeManagementClient();
//                if (applicationDescriptor.isAssetPublish()) {
//                    final ExchangeAssetDescriptor asset = applicationDescriptor.getApi().getAsset();
//                    assetPublished = exchangeManagementClient.publish(asset, organization, source,  );
//                }
//                provisioningResult = applicationProvisioningService.provision(applicationDescriptor, environment, apiProvisioningConfig);
//                final APIDescriptor apiDescriptor = applicationDescriptor.getApi();
//                if (provisioningResult.getApi() != null && apiDescriptor.isInjectApiId()) {
//                    final String apiIdProperty = apiDescriptor.getApiIdProperty();
//                    if (apiIdProperty == null) {
//                        throw new IllegalArgumentException("apiIdProperty musn't be null");
//                    }
//                    deploymentRequest.setOverrideProperty(apiIdProperty, provisioningResult.getApi().getId());
//                    deploymentRequest.setOverrideProperty(ANYPOINT_PLATFORM_CLIENT_ID, environment.getClientId());
//                    try {
//                        final String clientSecret = environment.getClientSecret();
//                        if (clientSecret != null) {
//                            deploymentRequest.setOverrideProperty(ANYPOINT_PLATFORM_CLIENT_SECRET, clientSecret);
//                        }
//                    } catch (HttpException e) {
//                        if (e.getStatusCode() != 401) {
//                            throw e;
//                        }
//                    }
//                }
//                if (assetPublished && provisioningRequest.isDeleteSnapshots()) {
//                    exchangeManagementClient.deleteSnapshots(organization, applicationDescriptor.getApi().getAsset());
//                }
//                final ClientApplicationDescriptor clientDescriptor = applicationDescriptor.getClient();
//                ClientApplication clientApp = provisioningResult.getClientApplication();
//                if (clientApp != null && clientDescriptor != null && clientDescriptor.isInjectClientIdSec()) {
//                    final String clientIdProperty = clientDescriptor.getClientIdProperty();
//                    if (clientIdProperty == null) {
//                        throw new IllegalStateException("client descriptor id property musn't be null");
//                    }
//                    deploymentRequest.setOverrideProperty(clientIdProperty, clientApp.getClientId());
//                    final String clientSecretProperty = clientDescriptor.getClientSecretProperty();
//                    if (clientSecretProperty == null) {
//                        throw new IllegalStateException("client descriptor id property musn't be null");
//                    }
//                    deploymentRequest.setOverrideProperty(clientSecretProperty, clientApp.getClientSecret());
//                }
//            } else {
//                logger.info("no anypoint.json found, skipping provisioning");
//            }
//            if (deploymentRequest.isFilePropertiesSecure() &&
//                    applicationDescriptor.getProperties() != null) {
//                for (PropertyDescriptor propertyDescriptor : applicationDescriptor.getProperties().values()) {
//                    if (propertyDescriptor.isSecure()) {
//                        String pVal = deploymentRequest.getProperties().remove(propertyDescriptor.getKey());
//                        deploymentRequest.addFileProperty(propertyDescriptor.getKey(), pVal);
//                    }
//                }
//            }
//            final Map<String, String> fileProperties = deploymentRequest.getFileProperties();
//            if (fileProperties != null && !fileProperties.isEmpty()) {
//                if (logger.isDebugEnabled()) {
//                    logger.debug("File properties injected into application: {}", fileProperties);
//                    for (Map.Entry<String, String> entry : fileProperties.entrySet()) {
//                        logger.debug("> {} = {}", entry.getKey(), entry.getValue());
//                    }
//                    logger.debug("filePropertiesPath= {}", deploymentRequest.getFilePropertiesPath());
//                }
//                transformers.add(new SetPropertyTransformer(deploymentRequest.getFilePropertiesPath(), CREATE,
//                        new HashMap<>(fileProperties)));
//                logger.info("Added properties file to application archive");
//            }
//            if (!transformers.isEmpty()) {
//                try {
//                    if (source instanceof FileApplicationSource || source.getLocalFile() != null) {
//                        File oldFile = source.getLocalFile();
//                        File newFile = new TempFile("transformed", filename);
//                        source = new FileApplicationSource(client, newFile);
//                        Unpacker unpacker = new Unpacker(oldFile, FileType.ZIP, newFile, FileType.ZIP);
//                        unpacker.addTransformers(transformers);
//                        unpacker.unpack();
//                        logger.info("Enhanced application archive");
//                    } else if (source instanceof ExchangeApplicationSource) {
//                        throw new ProvisioningException("Transformations on exchange sources not supported at this (so OnPrem provisioned deployments won't work with exchange sources until this feature is added)");
//                    }
//                } catch (Exception e) {
//                    throw new ProvisioningException("An error occurred while applying application " + appName + " transformations: " + e.getMessage(), e);
//                }
//                tmpFile = true;
//            }
//            return doDeploy(deploymentRequest);
//        } catch (NotFoundException e) {
//            throw new DeploymentException(e);
//        } finally {
//            if (tmpFile) {
//                IOUtils.close((TempFile) source.getLocalFile());
//            }
//        }
//    }


    private void waitForApplicationStart(RuntimeDeploymentRequest request, DeploymentResult result) throws HttpException, ApplicationDeploymentFailedException {
        if (result != null && !request.isSkipWait()) {
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Waiting for application start");
            final DeploymentParameters deploymentParameters = request.getDeploymentParameters();
            result.waitDeployed(deploymentParameters.getDeployTimeout().toMillis(), deploymentParameters.getDeployRetryDelay().toMillis());
            elogger.info(EMTLogger.Product.RUNTIME_MANAGER, "Application started successfully");
        }
    }

    @NotNull
    private DeploymentOperation createDeploymentOperation(RuntimeDeploymentRequest request, ApplicationSource source, String target, Environment environment, Organization organization) throws HttpException, NotFoundException {
        DeploymentOperation op;
        if (target.equalsIgnoreCase("cloudhub")) {
            op = new CHDeploymentOperation(request, environment, source);
        } else {
            try {
                Server server = environment.findServerByName(target);
                op = new HDeploymentOperation(request, server, source);
            } catch (NotFoundException e) {
                final Fabric fabric = organization.findFabricByName(target);
                op = new RTFDeploymentOperation(fabric, request, environment, source);
            }
        }
        return op;
    }
}
