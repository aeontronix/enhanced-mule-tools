/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.commons.TempFile;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.*;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.*;
import com.aeontronix.enhancedmule.tools.anypoint.application.DeploymentException;
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
                client.provisionApplication(applicationDescriptor, deploymentRequest);
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
}
