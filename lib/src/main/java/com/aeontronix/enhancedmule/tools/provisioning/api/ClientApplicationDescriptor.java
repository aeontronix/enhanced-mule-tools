/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning.api;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.api.API;
import com.aeontronix.enhancedmule.tools.api.APIContract;
import com.aeontronix.enhancedmule.tools.api.ClientApplication;
import com.aeontronix.enhancedmule.tools.api.SLATier;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetInstance;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.provisioning.AnypointDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.UnauthorizedHttpException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.aeontronix.commons.StringUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class ClientApplicationDescriptor {
    private static final Logger logger = getLogger(ClientApplicationDescriptor.class);
    private String url;
    private String description;
    private String name;
    private List<APIAccessDescriptor> access;

    public ClientApplicationDescriptor() {
    }

    public ClientApplicationDescriptor(String url, String description, String name) {
        this.url = url;
        this.description = description;
        this.name = name;
    }

    @JsonProperty
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("access")
    public synchronized List<APIAccessDescriptor> getAccess() {
        return access != null ? access : Collections.emptyList();
    }

    public synchronized void setAccess(List<APIAccessDescriptor> access) {
        this.access = access;
    }

    public synchronized ClientApplicationDescriptor addAccess(APIAccessDescriptor accessDescriptor) {
        if (access == null) {
            access = new ArrayList<>();
        }
        access.add(accessDescriptor);
        return this;
    }

    public synchronized ClientApplicationDescriptor addAccess(API api) {
        addAccess(new APIAccessDescriptor(api, null));
        return this;
    }

    public synchronized ClientApplicationDescriptor addAccess(API api, String slaTier) {
        addAccess(new APIAccessDescriptor(api, slaTier));
        return this;
    }

    public void provision(AnypointDescriptor anypointDescriptor, Environment environment, APIProvisioningConfig config, APIProvisioningResult result) throws ProvisioningException {
        try {
            String appId = anypointDescriptor.getId();
            if (name == null) {
                name = appId + "-" + config.getVariables().get("environment.lname");
            }
            ClientApplication clientApplication = null;
            try {
                clientApplication = environment.getOrganization().findClientApplicationByName(name);
                logger.debug("Client application found: " + name);
            } catch (NotFoundException e) {
                //
            }
            if (clientApplication == null) {
                logger.debug("Client application not found, creating: " + name);
                String instanceId = null;
                if( access != null && ! access.isEmpty() ) {
                    instanceId = findAPIInstance(environment, access.get(0)).getId();
                }
                clientApplication = environment.getOrganization().createClientApplication(name, url, description, instanceId);
            }
            result.setClientApplication(clientApplication);
            if (access != null) {
                for (APIAccessDescriptor accessDescriptor : access) {
                    AssetInstance instance = findAPIInstance(environment, accessDescriptor);
                    logger.debug("Found instance {}", instance);
                    Environment apiEnv = new Environment(new Organization(environment.getClient(), instance.getOrganizationId()), instance.getEnvironmentId());
                    API accessedAPI = new API(apiEnv);
                    accessedAPI.setId(instance.getId());
                    logger.debug("Found apiEnv {} with id {}", apiEnv, apiEnv.getId());
                    APIContract contract = null;
                    try {
                        contract = accessedAPI.findContract(clientApplication);
                    } catch (NotFoundException e) {
                        //
                    } catch (UnauthorizedHttpException e) {
                        logger.warn("Unable to List contracts of api " + accessedAPI.getAssetId() + " due to lack of permissions: " + e.getMessage());
                    }
                    if (contract == null) {
                        SLATier slaTier = null;
                        if (accessDescriptor.getSlaTier() != null) {
                            slaTier = instance.findSLATier(accessDescriptor.getSlaTier());
                        } else {
                            List<SLATier> slaTiers = instance.findSLATiers();
                            if (slaTiers.size() == 1) {
                                slaTier = instance.findSLATier(slaTiers.iterator().next().getName());
                            }
                        }
                        contract = clientApplication.requestAPIAccess(accessedAPI, instance, slaTier);
                    }
                    if (!contract.isApproved() && config.isAutoApproveAPIAccessRequest()) {
                        try {
                            if (contract.isRevoked()) {
                                contract.restoreAccess();
                            } else {
                                contract.approveAccess();
                            }
                        } catch (HttpException e) {
                            if (e.getStatusCode() == 403) {
                                logger.warn("Unable to approve access to " + accessDescriptor.getAssetId() + " due to lack of permissions: " + e.getMessage());
                            } else {
                                throw e;
                            }
                        }
                    }
                }
            }
        } catch (HttpException | NotFoundException e) {
            throw new ProvisioningException(e);
        }
    }

    private AssetInstance findAPIInstance(Environment environment, APIAccessDescriptor accessDescriptor) throws HttpException, NotFoundException {
        logger.debug("Processing access descriptor : {}", accessDescriptor);
        Organization accessOrg;
        if (accessDescriptor.getOrgId() == null) {
            logger.debug("Access descriptor has no org id, getting the default org");
            accessOrg = environment.getOrganization();
            accessDescriptor.setOrgId(accessOrg.getId());
        } else {
            accessOrg = environment.getOrganization().getClient().findOrganizationById(accessDescriptor.getOrgId());
        }
        logger.debug("Access org = {}", accessOrg.getId());
        if (accessDescriptor.getGroupId() == null) {
            logger.debug("No group id found, using the org id instead");
            accessDescriptor.setGroupId(accessOrg.getId());
        }
        logger.debug("Access group id = {}", accessDescriptor.getGroupId());
        String accessEnvId;
        if (accessDescriptor.getEnvId() != null) {
            logger.debug("Env id set: {}", accessDescriptor.getEnvId());
            accessEnvId = accessDescriptor.getEnvId();
        } else if (StringUtils.isNotBlank(accessDescriptor.getEnv())) {
            accessEnvId = accessOrg.findEnvironmentByName(accessDescriptor.getEnv()).getId();
            logger.debug("access environment specified");
        } else {
            logger.debug("No access environment specified, using the API's environment");
            accessEnvId = environment.getId();
        }
        logger.debug("Access environment id = {}", accessEnvId);
        ExchangeAsset exchangeAsset = accessOrg.findExchangeAsset(accessDescriptor.getGroupId(), accessDescriptor.getAssetId());
        logger.debug("Found exchangeAsset {}", exchangeAsset);
        logger.debug("exchangeAsset instances: {}", exchangeAsset.getInstances());
        return exchangeAsset.findInstances(accessDescriptor.getLabel(), accessEnvId);
    }
}
