/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.commons.file.TempFile;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.api.ClientApplication;
import com.aeontronix.enhancedmule.tools.anypoint.application.DeploymentException;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ApplicationProvisioningService;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ExchangeManagementClient;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.APIProvisioningResult;
import com.aeontronix.enhancedmule.tools.application.api.ClientApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.PropertyDescriptor;
import com.aeontronix.enhancedmule.tools.exchange.ExchangeAssetDescriptor;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ExchangeApplicationSource;
import com.aeontronix.enhancedmule.tools.legacy.deploy.FileApplicationSource;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.unpack.FileType;
import com.aeontronix.unpack.Unpacker;
import com.aeontronix.unpack.transformer.SetPropertyTransformer;
import com.aeontronix.unpack.transformer.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.aeontronix.commons.Required.CREATE;

public abstract class DeploymentOperation {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentOperation.class);
    private static final EMTLogger elogger = new EMTLogger(logger);
    public static final String ANYPOINT_PLATFORM_CLIENT_ID = "anypoint.platform.client_id";
    public static final String ANYPOINT_PLATFORM_CLIENT_SECRET = "anypoint.platform.client_secret";
    protected RuntimeDeploymentRequest deploymentRequest;
    protected Environment environment;
    protected ApplicationSource source;

    public DeploymentOperation() {
    }

    public DeploymentOperation(RuntimeDeploymentRequest request, Environment environment, ApplicationSource source) {
        this.deploymentRequest = request;
        this.environment = environment;
        this.source = source;
    }

    public DeploymentResult deploy() throws ProvisioningException, IOException, HttpException, DeploymentException {
        AnypointClient client = environment.getClient();
        boolean tmpFile = false;
        try {
            environment = environment.refresh();
            APIProvisioningResult provisioningResult = null;
            List<Transformer> transformers = new ArrayList<>();
            final ApplicationDescriptor applicationDescriptor = deploymentRequest.getApplicationDescriptor();
            if (applicationDescriptor == null) {
                logger.info("no anypoint.json found, skipping provisioning");
            }
            if (applicationDescriptor != null && !deploymentRequest.isSkipProvisioning() ) {
                logger.debug("Found anypoint provisioning file, provisioning");
                final Organization organization = environment.getOrganization();
                final ApplicationProvisioningService applicationProvisioningService = new ApplicationProvisioningService(client);
                boolean assetPublished = false;
                final ExchangeManagementClient exchangeManagementClient = new ExchangeManagementClient();
                if (applicationDescriptor.isAssetPublish()) {
                    final ExchangeAssetDescriptor asset = applicationDescriptor.getApi().getAsset();
                    assetPublished = exchangeManagementClient.publish(asset, organization, source, deploymentRequest);
                }
                provisioningResult = applicationProvisioningService.provision(applicationDescriptor, environment, deploymentRequest);
                final APIDescriptor apiDescriptor = applicationDescriptor.getApi();
                if (provisioningResult.getApi() != null && apiDescriptor.isInjectApiId()) {
                    final String apiIdProperty = apiDescriptor.getApiIdProperty();
                    if (apiIdProperty == null) {
                        throw new IllegalArgumentException("apiIdProperty musn't be null");
                    }
                    deploymentRequest.setOverrideProperty(apiIdProperty, provisioningResult.getApi().getId());
                    deploymentRequest.setOverrideProperty(ANYPOINT_PLATFORM_CLIENT_ID, environment.getClientId());
                    try {
                        final String clientSecret = environment.getClientSecret();
                        if (clientSecret != null) {
                            deploymentRequest.setOverrideProperty(ANYPOINT_PLATFORM_CLIENT_SECRET, clientSecret);
                        }
                    } catch (HttpException e) {
                        if (e.getStatusCode() != 401) {
                            throw e;
                        }
                    }
                }
                if (assetPublished && deploymentRequest.isDeleteSnapshots()) {
                    exchangeManagementClient.deleteSnapshots(organization, applicationDescriptor.getApi().getAsset());
                }
                final ClientApplicationDescriptor clientDescriptor = applicationDescriptor.getClient();
                ClientApplication clientApp = provisioningResult.getClientApplication();
                if (clientApp != null && clientDescriptor != null && clientDescriptor.isInjectClientIdSec()) {
                    final String clientIdProperty = clientDescriptor.getClientIdProperty();
                    if (clientIdProperty == null) {
                        throw new IllegalStateException("client descriptor id property musn't be null");
                    }
                    deploymentRequest.setOverrideProperty(clientIdProperty, clientApp.getClientId());
                    final String clientSecretProperty = clientDescriptor.getClientSecretProperty();
                    if (clientSecretProperty == null) {
                        throw new IllegalStateException("client descriptor id property musn't be null");
                    }
                    deploymentRequest.setOverrideProperty(clientSecretProperty, clientApp.getClientSecret());
                }
            }
            if (deploymentRequest.isFilePropertiesSecure() && applicationDescriptor != null &&
                    applicationDescriptor.getProperties() != null) {
                for (PropertyDescriptor propertyDescriptor : applicationDescriptor.getProperties().values()) {
                    if (propertyDescriptor.isSecure()) {
                        String pVal = deploymentRequest.getProperties().remove(propertyDescriptor.getKey());
                        deploymentRequest.addFileProperty(propertyDescriptor.getKey(), pVal);
                    }
                }
            }
            final Map<String, String> fileProperties = deploymentRequest.getFileProperties();
            if (fileProperties != null && !fileProperties.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("File properties injected into application: {}", fileProperties);
                    for (Map.Entry<String, String> entry : fileProperties.entrySet()) {
                        logger.debug("> {} = {}", entry.getKey(), entry.getValue());
                    }
                    logger.debug("filePropertiesPath= {}", deploymentRequest.getFilePropertiesPath());
                }
                transformers.add(new SetPropertyTransformer(deploymentRequest.getFilePropertiesPath(), CREATE,
                        new HashMap<>(fileProperties)));
                logger.info("Added properties file to application archive");
            }
            if (!transformers.isEmpty()) {
                try {
                    if (source instanceof FileApplicationSource || source.getLocalFile() != null) {
                        File oldFile = source.getLocalFile();
                        File newFile = new TempFile("transformed", deploymentRequest.getFilename());
                        source = new FileApplicationSource(client, newFile);
                        Unpacker unpacker = new Unpacker(oldFile, FileType.ZIP, newFile, FileType.ZIP);
                        unpacker.addTransformers(transformers);
                        unpacker.unpack();
                        logger.info("Enhanced application archive");
                    } else if (source instanceof ExchangeApplicationSource) {
                        throw new ProvisioningException("Transformations on exchange sources not supported at this (so OnPrem provisioned deployments won't work with exchange sources until this feature is added)");
                    }
                } catch (Exception e) {
                    throw new ProvisioningException("An error occurred while applying application " + deploymentRequest.getAppName() + " transformations: " + e.getMessage(), e);
                }
                tmpFile = true;
            }
            return doDeploy(deploymentRequest);
        } catch (NotFoundException e) {
            throw new DeploymentException(e);
        } finally {
            if (tmpFile) {
                IOUtils.close((TempFile) source.getLocalFile());
            }
        }
    }

    protected abstract DeploymentResult doDeploy(RuntimeDeploymentRequest request) throws IOException, HttpException, DeploymentException;

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public ApplicationSource getSource() {
        return source;
    }

    public void setSource(ApplicationSource source) {
        this.source = source;
    }

    protected String executeRequest(long start, HttpHelper.MultiPartRequest multiPartRequest) throws HttpException, IOException {
        String json = multiPartRequest.execute();
        if (logger.isDebugEnabled()) {
            logger.debug("File upload took " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds");
        }
        return json;
    }

    public String processAppName(String appName) {
        return appName != null ? appName : deploymentRequest.getArtifactId();
    }
}
