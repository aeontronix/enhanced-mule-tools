/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.deploy;

import com.aeontronix.enhancedmule.tools.AnypointClient;
import com.aeontronix.enhancedmule.tools.Environment;
import com.aeontronix.enhancedmule.tools.HttpException;
import com.aeontronix.enhancedmule.tools.NotFoundException;
import com.aeontronix.enhancedmule.tools.api.ClientApplication;
import com.aeontronix.enhancedmule.tools.api.provision.*;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloudtek.unpack.*;
import com.kloudtek.unpack.transformer.SetPropertyTransformer;
import com.kloudtek.unpack.transformer.Transformer;
import com.kloudtek.util.TempFile;
import com.kloudtek.util.UnexpectedException;
import com.kloudtek.util.io.IOUtils;
import com.kloudtek.util.io.InMemInputFilterStream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class DeploymentRequest {
    private static final Logger logger = LoggerFactory.getLogger(DeploymentRequest.class);
    public static final String META_INF_MULE_ARTIFACT_MULE_ARTIFACT_JSON = "META-INF/mule-artifact/mule-artifact.json";
    public static final String ANYPOINT_PLATFORM_CLIENT_ID = "anypoint.platform.client_id";
    public static final String ANYPOINT_PLATFORM_CLIENT_SECRET = "anypoint.platform.client_secret";
    public static final String SECURE_PROPERTIES = "secureProperties";
    protected Environment environment;
    protected String appName;
    protected ApplicationSource source;
    protected String filename;
    protected APIProvisioningConfig apiProvisioningConfig;
    protected DeploymentConfig deploymentConfig;
    protected AnypointConfigFileDescriptor apiProvisioningDescriptor;

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
                apiProvisioningConfig.setVariable("environment.id", environment.getId());
                apiProvisioningConfig.setVariable("environment.name", environment.getName());
                apiProvisioningConfig.setVariable("environment.lname", environment.getName().replace(" ", "_").toLowerCase());
                apiProvisioningConfig.setVariable("organization.name", environment.getOrganization().getName());
                apiProvisioningConfig.setVariable("organization.lname", environment.getOrganization().getName().replace(" ", "_").toLowerCase());
                apiProvisioningDescriptor = source.getAPIProvisioningDescriptor(apiProvisioningConfig);
                if (apiProvisioningDescriptor != null) {
                    logger.debug("Found anypoint provisioning file, provisioning");
                    provisioningResult = apiProvisioningDescriptor.provision(environment, apiProvisioningConfig);
                    if (provisioningResult.getApi() != null && apiProvisioningConfig.isInjectApiId()) {
                        deploymentConfig.setOverrideProperty(apiProvisioningConfig.getInjectApiIdKey(), provisioningResult.getApi().getId());
                        apiProvisioningDescriptor.addProperty(apiProvisioningConfig.getInjectApiIdKey(),false);
                        deploymentConfig.setOverrideProperty(ANYPOINT_PLATFORM_CLIENT_ID, environment.getClientId());
                        apiProvisioningDescriptor.addProperty(ANYPOINT_PLATFORM_CLIENT_ID,false);
                        apiProvisioningDescriptor.addProperty(ANYPOINT_PLATFORM_CLIENT_SECRET,true);
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
                        apiProvisioningDescriptor.addProperty(keyId,false);
                        deploymentConfig.setOverrideProperty(keyId, clientApp.getClientId());
                        String keySecret = apiProvisioningConfig.getInjectClientIdSecretKey() + ".secret";
                        deploymentConfig.setOverrideProperty(keySecret, clientApp.getClientSecret());
                        apiProvisioningDescriptor.addProperty(keySecret,true);
                    }
                    Transformer secureProperties = new Transformer() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void apply(Source source, Destination destination) throws UnpackException {
                            SourceFile file = (SourceFile) source.getFile(META_INF_MULE_ARTIFACT_MULE_ARTIFACT_JSON);
                            if (file == null) {
                                throw new UnpackException(META_INF_MULE_ARTIFACT_MULE_ARTIFACT_JSON + " not found");
                            }
                            file.setInputStream(new InMemInputFilterStream(file.getInputStream()) {
                                @Override
                                protected byte[] transform(byte[] data) throws IOException {
                                    return IOUtils.toByteArray(os -> {
                                        Map json;
                                        ObjectMapper om = new ObjectMapper();
                                        json = om.readValue(data, Map.class);
                                        List<String> securePropertiesList = (List<String>) json.get(SECURE_PROPERTIES);
                                        if (securePropertiesList == null) {
                                            securePropertiesList = new ArrayList();
                                        }
                                        HashSet<String> secProps = new HashSet<>(securePropertiesList);
                                        HashMap<String, PropertyDescriptor> propDesc = apiProvisioningDescriptor.getProperties();
                                        if( propDesc != null ) {
                                            for (PropertyDescriptor propertyDescriptor : propDesc.values()) {
                                                if(propertyDescriptor.isSecure()) {
                                                    secProps.add(propertyDescriptor.getName());
                                                }
                                            }
                                            json.put(SECURE_PROPERTIES, new ArrayList<>(secProps));
                                        }
                                        om.writeValue(os,json);
                                    });
                                }
                            });
                        }
                    };
                    transformers.add(secureProperties);
                } else {
                    logger.info("no anypoint.json found, skipping provisioning");
                }
            }
            if (deploymentConfig.isFilePropertiesSecure() &&
                    apiProvisioningDescriptor.getProperties() != null) {
                for (PropertyDescriptor propertyDescriptor : apiProvisioningDescriptor.getProperties().values()) {
                    if (propertyDescriptor.isSecure()) {
                        String pVal = deploymentConfig.getProperties().remove(propertyDescriptor.getName());
                        deploymentConfig.addFileProperty(propertyDescriptor.getName(),pVal);
                    }
                }
            }
            if (deploymentConfig.getFileProperties() != null && !deploymentConfig.getFileProperties().isEmpty()) {
                transformers.add(new SetPropertyTransformer(deploymentConfig.getFilePropertiesPath(),
                        new HashMap<>(deploymentConfig.getFileProperties())));
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
