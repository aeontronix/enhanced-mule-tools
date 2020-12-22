/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning.api;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.TempFile;
import com.aeontronix.commons.validation.ValidationUtils;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetCategory;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetCreationException;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.api.API;
import com.aeontronix.enhancedmule.tools.api.APISpec;
import com.aeontronix.enhancedmule.tools.api.SLATier;
import com.aeontronix.enhancedmule.tools.api.SLATierLimits;
import com.aeontronix.enhancedmule.tools.exchange.ExchangeAssetDescriptor;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.provisioning.portal.PortalDescriptor;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toMap;

public class APIDescriptor {
    private static final Logger logger = LoggerFactory.getLogger(APIDescriptor.class);
    private static final EMTLogger plogger = new EMTLogger(logger);
    private ExchangeAssetDescriptor asset;
    private String implementationUrl;
    private String consumerUrl;
    private Map<String,Object> implementationUrlJson;
    private List<String> tags;
    private boolean addAutoDiscovery = false;
    private String autoDiscoveryFlow = "api-main";
    private List<PolicyDescriptor> policies;
    private List<String> accessedBy;
    private String label;
    private List<SLATierDescriptor> slaTiers;
    private String apiIdProperty;
    private boolean injectApiId = true;

    public APIDescriptor() {
    }

    public void provision(ApplicationDescriptor cfg, Environment environment, APIProvisioningConfig config, ApplicationSource applicationSource, APIProvisioningResult result) throws ProvisioningException {
        try {
            ValidationUtils.notNull(IllegalStateException.class, "API Descriptor missing value: asset", asset);
            final Organization organization = environment.getParent();
            logger.info("Provisioning " + asset.getId() + " within org " + organization.getName() + " env " + environment.getName());
            Boolean m3 = cfg.getMule3();
            if (m3 == null) {
                m3 = false;
            }
            API api;
            try {
                api = environment.findAPIByExchangeAssetIdOrNameAndVersion(asset.getId(), asset.getVersion(), label);
                logger.debug("API " + asset.getId() + " " + asset.getVersion() + " exists: " + api);
            } catch (NotFoundException e) {
                logger.debug("API " + asset.getId() + " " + asset.getVersion() + " not found, creating");
                APISpec apiSpec;
                try {
                    apiSpec = organization.findAPISpecsByIdOrNameAndVersion(asset.getId(), asset.getVersion());
                } catch (NotFoundException ex) {
                    if (asset != null) {
                        if (applicationSource == null) {
                            throw new AssetCreationException("Cannot create asset due to missing application source (standalone provisioning doesn't support REST asset creation)");
                        }
                        if(asset.getCreate()) {
                            asset.create(environment.getOrganization(), applicationSource);
                        }
                    } else {
                        throw ex;
                    }
                    apiSpec = organization.findAPISpecsByIdOrNameAndVersion(asset.getId(), asset.getVersion());
                }
                // now we need to check if there's an existing API with the same productAPIVersion
                String productAPIVersion = apiSpec.getProductAPIVersion();
                try {
                    logger.debug("findAPIByExchangeAssetIdOrNameAndProductAPIVersion: {} , {} , {}", asset.getId(), productAPIVersion, label);
                    api = environment.findAPIByExchangeAssetIdOrNameAndProductAPIVersion(asset.getId(), productAPIVersion, label);
                    final String currentAssetVersion = api.getAssetVersion();
                    if (!currentAssetVersion.equalsIgnoreCase(asset.getVersion())) {
                        api = api.updateVersion(asset.getVersion());
                        plogger.info(EMTLogger.Product.API_MANAGER, "Updated asset {} version to {}",api.getAssetId(),asset.getVersion());
                    }
                } catch (NotFoundException ex) {
                    logger.debug("Creating API");
                    if (implementationUrlJson != null) {
                        api = environment.createAPI(apiSpec, label, implementationUrlJson, consumerUrl );
                    } else {
                        api = environment.createAPI(apiSpec, !m3, implementationUrl, consumerUrl , label, asset.getType());
                    }
                    plogger.info(EMTLogger.Product.API_MANAGER, "Created api {}",api.getAssetId(),asset.getVersion());
                }
            }
            if (policies != null) {
                plogger.info(EMTLogger.Product.API_MANAGER, "Setting policies for {}",api.getAssetId());
                api.deletePolicies();
                for (PolicyDescriptor policyDescriptor : policies) {
                    api.createPolicy(policyDescriptor);
                }
            }
            if (slaTiers != null) {
                plogger.info(EMTLogger.Product.API_MANAGER, "Setting SLA Tiers for {}",api.getAssetId());
                for (SLATierDescriptor slaTierDescriptor : slaTiers) {
                    try {
                        SLATier slaTier = api.findSLATier(slaTierDescriptor.getName());
                        slaTier.setAutoApprove(slaTierDescriptor.isAutoApprove());
                        slaTier.setDescription(slaTierDescriptor.getDescription());
                        slaTier.setLimits(slaTierDescriptor.getLimits());
                        slaTier = slaTier.update();
                    } catch (NotFoundException e) {
                        api.createSLATier(slaTierDescriptor.getName(), slaTierDescriptor.getDescription(), slaTierDescriptor.isAutoApprove(), slaTierDescriptor.getLimits());
                    }
                }
            }
            if( consumerUrl != null ) {
                api.updateConsumerUrl(consumerUrl);
                plogger.info(EMTLogger.Product.API_MANAGER, "Updated consumer url to {}",consumerUrl);
            }
            if( implementationUrlJson !=null ) {
                api.updateImplementationUrl(implementationUrlJson);
                plogger.info(EMTLogger.Product.API_MANAGER, "Updated implementation url to {}", implementationUrlJson.toString());
            } else if( implementationUrl != null ) {
                api.updateImplementationUrl(implementationUrl, !m3, asset.getType() );
                plogger.info(EMTLogger.Product.API_MANAGER, "Updated implementation url to {}",implementationUrl);
            }
            api = api.refresh();
            result.setApi(api);
            if (logger.isDebugEnabled()) {
                logger.debug("api: {}", api.toString());
            }
            // exchange
            if(asset != null) {
                asset.provision(environment.getOrganization());
            }
        } catch (AssetCreationException | NotFoundException | IOException e) {
            throw new ProvisioningException(e);
        }
    }


    @JsonProperty(defaultValue = "true")
    public boolean isInjectApiId() {
        return injectApiId;
    }

    public void setInjectApiId(boolean injectApiId) {
        this.injectApiId = injectApiId;
    }

    public void addPolicy(PolicyDescriptor policy) {
        getPolicies().add(policy);
    }

    @JsonProperty
    public synchronized List<PolicyDescriptor> getPolicies() {
        if (policies == null) {
            policies = new ArrayList<>();
        }
        return policies;
    }

    public void setPolicies(List<PolicyDescriptor> policies) {
        this.policies = policies;
    }

    @JsonProperty
    public List<String> getAccessedBy() {
        return accessedBy;
    }

    public void setAccessedBy(List<String> accessedBy) {
        this.accessedBy = accessedBy;
    }

    public synchronized List<SLATierDescriptor> getSlaTiers() {
        return slaTiers;
    }

    public synchronized void setSlaTiers(List<SLATierDescriptor> slaTiers) {
        this.slaTiers = slaTiers;
    }

    public synchronized APIDescriptor addSlaTier(String name, String description, boolean autoApprove, SLATierLimits... limits) {
        return addSlaTier(new SLATierDescriptor(name, description, autoApprove, limits));
    }

    public synchronized APIDescriptor addSlaTier(String name, boolean autoApprove, SLATierLimits... limits) {
        return addSlaTier(name, null, autoApprove, limits);
    }

    public synchronized APIDescriptor addSlaTier(SLATierDescriptor slaTierDescriptor) {
        if (slaTiers == null) {
            slaTiers = new ArrayList<>();
        }
        slaTiers.add(slaTierDescriptor);
        return this;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isAddAutoDiscovery() {
        return addAutoDiscovery;
    }

    public void setAddAutoDiscovery(boolean addAutoDiscovery) {
        this.addAutoDiscovery = addAutoDiscovery;
    }

    public String getAutoDiscoveryFlow() {
        return autoDiscoveryFlow;
    }

    public void setAutoDiscoveryFlow(String autoDiscoveryFlow) {
        this.autoDiscoveryFlow = autoDiscoveryFlow;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @JsonProperty
    public String getImplementationUrl() {
        return implementationUrl;
    }

    public void setImplementationUrl(String implementationUrl) {
        this.implementationUrl = implementationUrl;
    }

    @JsonProperty
    public String getConsumerUrl() {
        return consumerUrl;
    }

    public void setConsumerUrl(String consumerUrl) {
        this.consumerUrl = consumerUrl;
    }

    @JsonProperty
    public Map<String, Object> getImplementationUrlJson() {
        return implementationUrlJson;
    }

    public void setImplementationUrlJson(Map<String, Object> implementationUrlJson) {
        this.implementationUrlJson = implementationUrlJson;
    }

    public String getApiIdProperty() {
        return apiIdProperty;
    }

    public void setApiIdProperty(String apiIdProperty) {
        this.apiIdProperty = apiIdProperty;
    }

    @JsonIgnore
    public String getAssetVersion() {
        return asset != null ? asset.getVersion() : null;
    }

    @JsonIgnore
    public synchronized void setAssetVersion(String version) {
        if( asset == null ) {
            asset = new ExchangeAssetDescriptor();
        }
        asset.setVersion(version);
    }

    @JsonIgnore
    public String getAssetId() {
        return asset != null ? asset.getId() : null;
    }

    public synchronized void setAssetId(String id) {
        if( asset == null ) {
            asset = new ExchangeAssetDescriptor();
        }
        asset.setId(id);
    }
    @JsonIgnore
    public boolean isAssetCreate() {
        return asset != null ? asset.getCreate() : null;
    }

    public ExchangeAssetDescriptor getAsset() {
        return asset;
    }


}
