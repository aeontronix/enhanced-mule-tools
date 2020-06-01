/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.api.provision;

import com.aeontronix.enhancedmule.tools.Environment;
import com.aeontronix.enhancedmule.tools.NotFoundException;
import com.aeontronix.enhancedmule.tools.api.API;
import com.aeontronix.enhancedmule.tools.api.APISpec;
import com.aeontronix.enhancedmule.tools.api.SLATier;
import com.aeontronix.enhancedmule.tools.api.SLATierLimits;
import com.aeontronix.enhancedmule.tools.exchange.AssetCreationException;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kloudtek.util.validation.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class APIDescriptor {
    private static final Logger logger = LoggerFactory.getLogger(APIDescriptor.class);
    private String assetId;
    private String assetVersion;
    private String endpoint;
    private boolean addAutoDescovery = false;
    private String autoDiscoveryFlow="api-main";
    private HashMap<String, Object> endpointJson;
    private List<PolicyDescriptor> policies;
    private List<String> accessedBy;
    private String label;
    private List<SLATierDescriptor> slaTiers;
    private API.Type type = API.Type.REST;
    private boolean assetCreate;

    public APIDescriptor() {
    }

    public APIDescriptor(String assetId, String version) {
        this.assetId = assetId;
        this.assetVersion = version;
    }

    public void provision(AnypointDescriptor cfg, Environment environment, APIProvisioningConfig config, APIProvisioningResult result) throws ProvisioningException {
        try {
            ValidationUtils.notEmpty(IllegalStateException.class, "API Descriptor missing value: assetId", assetId);
            ValidationUtils.notEmpty(IllegalStateException.class, "API Descriptor missing value: assetVersion", assetVersion);
            logger.debug("Provisioning " + this + " within org " + environment.getParent().getName() + " env " + environment.getName());
            logger.debug("Provisioning " + this.getAssetId());
            Boolean m3 = cfg.getMule3();
            if (m3 == null) {
                m3 = false;
            }
            API api = null;
            boolean updateEndpoint = true;
            try {
                api = environment.findAPIByExchangeAssetIdOrNameAndVersion(this.getAssetId(), this.getAssetVersion(), label);
                logger.debug("API " + this.getAssetId() + " " + this.getAssetVersion() + " exists: " + api);
            } catch (NotFoundException e) {
                logger.debug("API " + this.getAssetId() + " " + this.getAssetVersion() + " not found, creating");
                APISpec apiSpec = null;
                try {
                    apiSpec = environment.getParent().findAPISpecsByIdOrNameAndVersion(this.getAssetId(), this.getAssetVersion());
                } catch (NotFoundException ex) {
                    if (assetCreate) {
                        logger.debug("Asset not found, creating");
                        if (type == API.Type.HTTP) {
                            environment.getOrganization().createExchangeHTTPAPIAsset(null, assetId, assetId, assetVersion, "v1");
                        } else {
                            throw new RuntimeException("Only HTTP Asset creation supported at this time");
                        }
                    } else {
                        throw ex;
                    }
                    apiSpec = environment.getParent().findAPISpecsByIdOrNameAndVersion(this.getAssetId(), this.getAssetVersion());
                }
                // now we need to check if there's an existing API with the same productAPIVersion
                String productAPIVersion = apiSpec.getProductAPIVersion();
                try {
                    logger.debug("findAPIByExchangeAssetIdOrNameAndProductAPIVersion: {} , {} , {}", this.getAssetId(), productAPIVersion, label);
                    api = environment.findAPIByExchangeAssetIdOrNameAndProductAPIVersion(this.getAssetId(), productAPIVersion, label);
                    api = api.updateVersion(assetVersion);
                } catch (NotFoundException ex) {
                    logger.debug("Couldn't find, creating");
                    if( endpointJson != null ) {
                        api = environment.createAPI(apiSpec, label, endpointJson);
                    } else {
                        api = environment.createAPI(apiSpec, !m3, this.getEndpoint(), label, type);
                    }
                    updateEndpoint = false;
                }
            }
            if (updateEndpoint) {
                if (this.endpointJson != null) {
                    api = api.updateEndpoint(this.getEndpoint(), endpointJson );
                } else if (this.getEndpoint() != null) {
                    api = api.updateEndpoint(this.getEndpoint(), !m3, type);
                }
            }
            result.setApi(api);
            if (policies != null) {
                api.deletePolicies();
                for (PolicyDescriptor policyDescriptor : policies) {
                    api.createPolicy(policyDescriptor);
                }
            }
            if (slaTiers != null) {
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
        } catch (HttpException | AssetCreationException | NotFoundException e) {
            throw new ProvisioningException(e);
        }
    }

    @JsonProperty
    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    @JsonProperty
    public String getAssetVersion() {
        return assetVersion;
    }

    public void setAssetVersion(String assetVersion) {
        this.assetVersion = assetVersion;
    }

    @JsonProperty
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public HashMap<String, Object> getEndpointJson() {
        return endpointJson;
    }

    public void setEndpointJson(HashMap<String, Object> endpointJson) {
        this.endpointJson = endpointJson;
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

    public API.Type getType() {
        return type;
    }

    public void setType(API.Type type) {
        this.type = type;
    }

    public boolean isAssetCreate() {
        return assetCreate;
    }

    public void setAssetCreate(boolean assetCreate) {
        this.assetCreate = assetCreate;
    }

    public boolean isAddAutoDescovery() {
        return addAutoDescovery;
    }

    public void setAddAutoDescovery(boolean addAutoDescovery) {
        this.addAutoDescovery = addAutoDescovery;
    }

    public String getAutoDiscoveryFlow() {
        return autoDiscoveryFlow;
    }

    public void setAutoDiscoveryFlow(String autoDiscoveryFlow) {
        this.autoDiscoveryFlow = autoDiscoveryFlow;
    }
}
