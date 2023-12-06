/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application.api;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.api.API;
import com.aeontronix.enhancedmule.tools.anypoint.api.APIContract;
import com.aeontronix.enhancedmule.tools.anypoint.api.ClientApplication;
import com.aeontronix.enhancedmule.tools.anypoint.api.SLATier;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetInstance;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningRequest;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.UnauthorizedHttpException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.aeontronix.enhancedmule.tools.util.EMTLogger.Product.API_MANAGER;
import static org.slf4j.LoggerFactory.getLogger;

public class ClientApplicationDescriptor {
    private static final Logger logger = getLogger(ClientApplicationDescriptor.class);
    private static final EMTLogger plogger = new EMTLogger(logger);
    private String url;
    private String description;
    private String name;
    private boolean injectClientIdSec = true;
    private String clientIdProperty;
    private String clientSecretProperty;
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

    @JsonProperty(defaultValue = "true")
    public boolean isInjectClientIdSec() {
        return injectClientIdSec;
    }

    public void setInjectClientIdSec(boolean injectClientIdSec) {
        this.injectClientIdSec = injectClientIdSec;
    }

    @JsonProperty
    public String getClientIdProperty() {
        return clientIdProperty;
    }

    public void setClientIdProperty(String clientIdProperty) {
        this.clientIdProperty = clientIdProperty;
    }

    @JsonProperty
    public String getClientSecretProperty() {
        return clientSecretProperty;
    }

    public void setClientSecretProperty(String clientSecretProperty) {
        this.clientSecretProperty = clientSecretProperty;
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

    public void provision(ApplicationDescriptor applicationDescriptor, Environment environment, ProvisioningRequest request, APIProvisioningResult result) throws ProvisioningException {
        try {
            String appId = applicationDescriptor.getId();
            if (name == null) {
                name = appId + "-" + environment.getLName();
            }
            ClientApplication clientApplication = null;
            try {
                clientApplication = environment.getOrganization().findClientApplicationByName(name);
            } catch (NotFoundException e) {
                //
            }
            if (clientApplication == null) {
                plogger.info(API_MANAGER, "Client application: {}", name);
                logger.debug("Client application not found, creating: " + name);
                String instanceId = null;
                if (access != null && !access.isEmpty()) {
                    instanceId = findAPIInstance(environment, access.get(0)).getId();
                }
                try {
                    clientApplication = environment.getOrganization().createClientApplication(name, url, description, instanceId);
                } catch (HttpException e) {
                    if (e.getStatusCode() == 502) {
                        clientApplication = handleClientAppCreatedInWrongOrg(environment);
                        if (clientApplication == null) {
                            throw e;
                        }
                    } else {
                        throw e;
                    }
                }
                plogger.info(API_MANAGER, "Created client application: {}", name);
            } else {
                plogger.info(API_MANAGER, "Client application already exists: {}", name);
            }
            result.setClientApplication(clientApplication);
            if (access != null) {
                for (APIAccessDescriptor accessDescriptor : access) {
                    String labelLogStr = StringUtils.isNotBlank(accessDescriptor.getLabel()) ? (" with label " + accessDescriptor.getLabel()) : "";
                    String apiAccessLogStr = "API contract to " + accessDescriptor.getAssetId() + labelLogStr + " using client application " + clientApplication.getName();
                    plogger.info(API_MANAGER, "Client application access: {}", apiAccessLogStr);
                    AssetInstance instance = findAPIInstance(environment, accessDescriptor);
                    logger.debug("Found instance {}", instance);
                    Environment apiEnv = new Environment(new Organization(environment.getClient(), instance.getOrganizationId()), instance.getEnvironmentId());
                    API accessedAPI = new API(apiEnv);
                    accessedAPI.setId(instance.getId());
                    accessedAPI.setAssetId(instance.getAssetId());
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
                        plogger.info(API_MANAGER, "Client application access missing, requesting: {}", apiAccessLogStr);
                        SLATier slaTier = null;
                        if (accessDescriptor.getSlaTier() != null) {
                            slaTier = instance.findSLATier(accessDescriptor.getSlaTier());
                        } else {
                            List<SLATier> slaTiers = instance.findSLATiers();
                            if (slaTiers.size() == 1) {
                                slaTier = instance.findSLATier(slaTiers.iterator().next().getName());
                            } else if (slaTiers.size() > 1) {
                                throw new ProvisioningException("Accessed API " + instance.getAssetId() + " has multiple SLA tiers, you must specify which is to be used");
                            }
                        }
                        contract = clientApplication.requestAPIAccess(accessedAPI, instance, slaTier);
                        plogger.info(API_MANAGER, "Created {}", apiAccessLogStr);
                    } else {
                        plogger.info(API_MANAGER, "Client application contract already exists: {}", apiAccessLogStr);
                    }
                    boolean approve = accessDescriptor.getApprove() != null ? accessDescriptor.getApprove() :
                            request.isAutoApproveAPIAccessRequest();
                    if (!contract.isApproved() && approve) {
                        try {
                            if (contract.isRevoked()) {
                                contract.restoreAccess();
                                plogger.info(API_MANAGER, "Restored approval to {}", apiAccessLogStr);
                            } else {
                                contract.approveAccess();
                                plogger.info(API_MANAGER, "Approved to {}", apiAccessLogStr);
                            }
                        } catch (HttpException e) {
                            if (e.getStatusCode() == 403) {
                                plogger.info(API_MANAGER, "Unable to approve access {} due to lack of permissions: {}", apiAccessLogStr, e.getMessage());
                            } else {
                                throw e;
                            }
                        }
                    } else {
                        plogger.info(API_MANAGER, "Contract for {} already exists and is pending approval (id={})", apiAccessLogStr, contract.getId());
                    }
                }
            }
        } catch (HttpException | NotFoundException e) {
            throw new ProvisioningException(e);
        }
    }

    private ClientApplication handleClientAppCreatedInWrongOrg(Environment environment) throws HttpException, ProvisioningException {
        for (Organization org : environment.getClient().findOrganizations()) {
            if (!org.getId().equals(environment.getOrganization().getId())) {
                try {
                    ClientApplication clientApp = org.findClientApplicationByName(name);
                    logger.warn("Client application was erroneously created under org " + org.getName() + ", please delete it so that it may be recreated under master org");
                    return clientApp;
                } catch (NotFoundException ex) {
                    //
                }
            }
        }
        return null;
    }

    private AssetInstance findAPIInstance(Environment environment, APIAccessDescriptor accessDescriptor) throws HttpException, NotFoundException {
        logger.debug("Processing access descriptor : {}", accessDescriptor);
        Organization accessOrg;
        if (accessDescriptor.getOrgId() == null) {
            if (accessDescriptor.getGroupId() != null) {
                accessDescriptor.setOrgId(accessDescriptor.getGroupId());
            }
            logger.debug("Access descriptor has no org id, getting the default org");
            accessOrg = environment.getOrganization();
            accessDescriptor.setOrgId(accessOrg.getId());
        } else {
            accessOrg = environment.getOrganization().getClient().findOrganizationById(accessDescriptor.getOrgId());
        }
        if( StringUtils.isBlank(accessDescriptor.getAssetId()) ) {
            throw new IllegalArgumentException("access descriptor missing assetId");
        }
        logger.debug("Access org = {}", accessOrg.getId());
        if (accessDescriptor.getGroupId() == null) {
            logger.debug("No group id found, using the org id instead");
            accessDescriptor.setGroupId(accessOrg.getId());
        }
        logger.debug("Access group id = {}", accessDescriptor.getGroupId());
        String accessEnvId;
        if (StringUtils.isNotBlank(accessDescriptor.getEnv())) {
            accessEnvId = accessOrg.findEnvironmentByNameOrId(accessDescriptor.getEnv()).getId();
            logger.debug("access environment specified");
        } else {
            logger.debug("No access environment specified, using the API's environment");
            accessEnvId = environment.getId();
        }
        logger.debug("Access environment id = {}", accessEnvId);
        ExchangeAsset exchangeAsset = accessOrg.findExchangeAsset(accessDescriptor.getGroupId(), accessDescriptor.getAssetId(), accessDescriptor.getAssetVersion());
        logger.debug("Found exchangeAsset {}", exchangeAsset);
        logger.debug("exchangeAsset instances: {}", exchangeAsset.getInstances());
        try {
            return exchangeAsset.findInstances(accessDescriptor.getLabel(), accessEnvId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Unable to find instances for accessed API " + accessDescriptor);
        }
    }
}
