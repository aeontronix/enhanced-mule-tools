/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning;

import com.aeontronix.enhancedmule.tools.AnypointClient;
import com.aeontronix.enhancedmule.tools.NotFoundException;
import com.aeontronix.enhancedmule.tools.Organization;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.kloudtek.util.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class OrganizationDescriptor {
    private static final Logger logger = getLogger(OrganizationDescriptor.class);
    private String name;
    private String id;
    private String parentId;
    private String ownerId;
    private boolean createSubOrgs = true;
    private boolean createEnvironments = true;
    private boolean globalDeployment = true;
    private int vCoresProduction;
    private int vCoresSandbox;
    private int vCoresDesign;
    private int staticIps;
    private int vpcs;
    private int loadBalancer;
    private List<EnvironmentDescriptor> environments;
    private List<RoleDescriptor> roles;

    public Organization provision(AnypointClient client) throws NotFoundException, HttpException, ProvisioningException {
        Organization org;
        if( ownerId == null ) {
            ownerId = client.getUserId();
            logger.debug("No owner id specified, using {}",ownerId);
        }
        if( id != null ) {
            org = client.findOrganizationById(id);
        } else if( name != null ) {
            try {
                org = client.findOrganization(name);
            } catch (NotFoundException e) {
                logger.info("Organization not found, creating");
                if( parentId != null ) {
                    Organization parentOrg = client.findOrganizationById(parentId);
                    org = parentOrg.createSubOrganization( name, ownerId, createSubOrgs, createEnvironments, globalDeployment,
                            vCoresProduction, vCoresSandbox, vCoresDesign, staticIps, vpcs, loadBalancer);
                } else {
                    org = client.createOrganization( name, ownerId, createSubOrgs, createEnvironments, globalDeployment,
                            vCoresProduction, vCoresSandbox, vCoresDesign, staticIps, vpcs, loadBalancer);
                }
                logger.info("Organization not found, created with id "+org.getId());
            }
        } else {
            throw new IllegalArgumentException("Organization descriptor must have an id or a name");
        }
        if (environments != null) {
            for (EnvironmentDescriptor environment : environments) {
                environment.provision(org);
            }
        }
        if( roles != null ) {
            for (RoleDescriptor role : roles) {
                role.provision(org);
            }
        }
        return org;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isCreateSubOrgs() {
        return createSubOrgs;
    }

    public void setCreateSubOrgs(boolean createSubOrgs) {
        this.createSubOrgs = createSubOrgs;
    }

    public boolean isCreateEnvironments() {
        return createEnvironments;
    }

    public void setCreateEnvironments(boolean createEnvironments) {
        this.createEnvironments = createEnvironments;
    }

    public boolean isGlobalDeployment() {
        return globalDeployment;
    }

    public void setGlobalDeployment(boolean globalDeployment) {
        this.globalDeployment = globalDeployment;
    }

    public int getvCoresProduction() {
        return vCoresProduction;
    }

    public void setvCoresProduction(int vCoresProduction) {
        this.vCoresProduction = vCoresProduction;
    }

    public int getvCoresSandbox() {
        return vCoresSandbox;
    }

    public void setvCoresSandbox(int vCoresSandbox) {
        this.vCoresSandbox = vCoresSandbox;
    }

    public int getvCoresDesign() {
        return vCoresDesign;
    }

    public void setvCoresDesign(int vCoresDesign) {
        this.vCoresDesign = vCoresDesign;
    }

    public int getStaticIps() {
        return staticIps;
    }

    public void setStaticIps(int staticIps) {
        this.staticIps = staticIps;
    }

    public int getVpcs() {
        return vpcs;
    }

    public void setVpcs(int vpcs) {
        this.vpcs = vpcs;
    }

    public int getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(int loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public List<EnvironmentDescriptor> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<EnvironmentDescriptor> environments) {
        this.environments = environments;
    }

    public List<RoleDescriptor> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDescriptor> roles) {
        this.roles = roles;
    }

    public static Organization provision(AnypointClient client, File orgDescriptorFile, String orgName) throws NotFoundException, HttpException, IOException, ProvisioningException {
        OrganizationDescriptor org = client.getJsonHelper().getJsonMapper().readValue(orgDescriptorFile,
                OrganizationDescriptor.class);
        if(StringUtils.isNotBlank(orgName) ) {
            org.setName(orgName);
        }
        return org.provision(client);
    }
}
