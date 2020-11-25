/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy;

import com.aeontronix.commons.Required;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.provisioning.api.*;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.api.ClientApplication;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.commons.TempFile;
import com.aeontronix.commons.UnexpectedException;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.unpack.FileType;
import com.aeontronix.unpack.Unpacker;
import com.aeontronix.unpack.transformer.FileMatcher;
import com.aeontronix.unpack.transformer.SetPropertyTransformer;
import com.aeontronix.unpack.transformer.Transformer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.aeontronix.commons.Required.CREATE;

public abstract class DeploymentRequest {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentRequest.class);
    private static final EMTLogger elogger = new EMTLogger(logger);
    public static final String ANYPOINT_PLATFORM_CLIENT_ID = "anypoint.platform.client_id";
    public static final String ANYPOINT_PLATFORM_CLIENT_SECRET = "anypoint.platform.client_secret";
    protected Environment environment;
    protected String appName;
    protected ApplicationSource source;
    protected String filename;
    protected APIProvisioningConfig apiProvisioningConfig;
    protected DeploymentConfig deploymentConfig;
    protected ApplicationDescriptor applicationDescriptor;

    public DeploymentRequest() {
    }

    public DeploymentRequest(Environment environment, String appName, ApplicationSource source, String filename,
                             APIProvisioningConfig apiProvisioningConfig,
                             @NotNull DeploymentConfig deploymentConfig) {
        this.environment = environment;
        this.appName = appName;
        this.source = source;
        this.filename = filename;
        this.apiProvisioningConfig = apiProvisioningConfig;
        this.deploymentConfig = deploymentConfig;
    }

    public DeploymentResult deploy() throws ProvisioningException, IOException, HttpException {
        AnypointClient client = environment.getClient();
        boolean tmpFile = false;
        try {
            environment = environment.refresh();
            APIProvisioningResult provisioningResult = null;
            List<Transformer> transformers = new ArrayList<>();
            if (apiProvisioningConfig != null) {
                applicationDescriptor = source.getAnypointDescriptor(apiProvisioningConfig);
                if (applicationDescriptor != null) {
                    logger.debug("Found anypoint provisioning file, provisioning");
                    provisioningResult = applicationDescriptor.provision(environment, apiProvisioningConfig, source);
                    if (provisioningResult.getApi() != null && apiProvisioningConfig.isInjectApiId()) {
                        deploymentConfig.setOverrideProperty(apiProvisioningConfig.getInjectApiIdKey(), provisioningResult.getApi().getId());
                        applicationDescriptor.addProperty(apiProvisioningConfig.getInjectApiIdKey(),false);
                        deploymentConfig.setOverrideProperty(ANYPOINT_PLATFORM_CLIENT_ID, environment.getClientId());
                        applicationDescriptor.addProperty(ANYPOINT_PLATFORM_CLIENT_ID,false);
                        applicationDescriptor.addProperty(ANYPOINT_PLATFORM_CLIENT_SECRET,true);
                        try {
                            deploymentConfig.setOverrideProperty(ANYPOINT_PLATFORM_CLIENT_SECRET, environment.getClientSecret());
                        } catch (HttpException e) {
                            if (e.getStatusCode() != 401) {
                                throw e;
                            }
                        }
                    }
                    ClientApplication clientApp = provisioningResult.getClientApplication();
                    if (clientApp != null && apiProvisioningConfig.isInjectClientIdSecret()) {
                        String keyId = apiProvisioningConfig.getInjectClientIdSecretKey() + ".id";
                        applicationDescriptor.addProperty(keyId,false);
                        deploymentConfig.setOverrideProperty(keyId, clientApp.getClientId());
                        String keySecret = apiProvisioningConfig.getInjectClientIdSecretKey() + ".secret";
                        deploymentConfig.setOverrideProperty(keySecret, clientApp.getClientSecret());
                        applicationDescriptor.addProperty(keySecret,true);
                    }
                } else {
                    logger.info("no anypoint.json found, skipping provisioning");
                }
            }
            if (deploymentConfig.isFilePropertiesSecure() &&
                    applicationDescriptor.getProperties() != null) {
                for (PropertyDescriptor propertyDescriptor : applicationDescriptor.getProperties().values()) {
                    if (propertyDescriptor.isSecure()) {
                        String pVal = deploymentConfig.getProperties().remove(propertyDescriptor.getName());
                        deploymentConfig.addFileProperty(propertyDescriptor.getName(),pVal);
                    }
                }
            }
            if (deploymentConfig.getFileProperties() != null && !deploymentConfig.getFileProperties().isEmpty()) {
                transformers.add(new SetPropertyTransformer(deploymentConfig.getFilePropertiesPath(), CREATE,
                        new HashMap<>(deploymentConfig.getFileProperties())));
                logger.info("Added properties file to application archive");
            }
            if (!transformers.isEmpty()) {
                try {
                    if (source instanceof FileApplicationSource || source.getLocalFile() != null) {
                        File oldFile = source.getLocalFile();
                        File newFile = new TempFile("transformed", filename);
                        source = new FileApplicationSource(client, newFile);
                        Unpacker unpacker = new Unpacker(oldFile, FileType.ZIP, newFile, FileType.ZIP);
                        unpacker.addTransformers(transformers);
                        unpacker.unpack();
                        logger.info("Enhanced application archive");
                    } else if (source instanceof ExchangeApplicationSource) {
                        throw new ProvisioningException("Transformations on exchange sources not supported at this (so OnPrem provisioned deployments won't work with exchange sources until this feature is added)");
                    }
                } catch (Exception e) {
                    throw new ProvisioningException("An error occurred while applying application " + appName + " transformations: " + e.getMessage(), e);
                }
                tmpFile = true;
            }
            return doDeploy();
        } catch (NotFoundException e) {
            throw new UnexpectedException(e);
        } finally {
            if (tmpFile) {
                IOUtils.close((TempFile) source.getLocalFile());
            }
        }
    }

    protected abstract DeploymentResult doDeploy() throws IOException, HttpException;

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public ApplicationSource getSource() {
        return source;
    }

    public void setSource(ApplicationSource source) {
        this.source = source;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    protected String executeRequest(long start, HttpHelper.MultiPartRequest multiPartRequest) throws HttpException, IOException {
        String json = multiPartRequest.execute();
        if (logger.isDebugEnabled()) {
            logger.debug("File upload took " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds");
        }
        return json;
    }
}
