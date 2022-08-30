/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint;

import com.aeontronix.commons.ThreadUtils;
import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.file.TempFile;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.alert.Alert;
import com.aeontronix.enhancedmule.tools.anypoint.api.*;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationArchiveVersionTransformer;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.anypoint.application.MavenHelper;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetList;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetProvisioningException;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetVersion;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.*;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import com.aeontronix.enhancedmule.tools.fabric.Fabric;
import com.aeontronix.enhancedmule.tools.legacy.deploy.FileApplicationSource;
import com.aeontronix.enhancedmule.tools.role.*;
import com.aeontronix.enhancedmule.tools.runtime.Target;
import com.aeontronix.enhancedmule.tools.runtime.manifest.ReleaseManifest;
import com.aeontronix.enhancedmule.tools.util.*;
import com.aeontronix.enhancedmule.tools.util.restclient.RESTException;
import com.aeontronix.unpack.FileType;
import com.aeontronix.unpack.UnpackException;
import com.aeontronix.unpack.Unpacker;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Organization extends AnypointObject {
    public static final Pattern MAJORVERSION_REGEX = Pattern.compile("(\\d*)\\..*");
    private static final Logger logger = LoggerFactory.getLogger(Organization.class);
    private static final EMTLogger eLogger = new EMTLogger(logger);
    @JsonProperty
    protected String id;
    @JsonProperty
    protected String name;
    @JsonProperty
    protected String parentId;
    @JsonProperty("isFederated")
    protected boolean federated;

    public Organization() {
    }

    public Organization(AnypointClient client) {
        super(client);
    }

    public Organization(AnypointClient client, String id) {
        super(client);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFederated() {
        return federated;
    }

    public void setFederated(boolean federated) {
        this.federated = federated;
    }

    public List<Environment> findAllEnvironments() throws HttpException {
        return Environment.findEnvironmentsByOrg(client, this);
    }


    public Environment findEnvironmentByNameOrId(@NotNull String env) throws NotFoundException, HttpException {
        logger.debug("Finding environment by name or id: {}",env);
        try {
            logger.debug("Attempting to find by name: {}",env);
            return findEnvironmentByName(env);
        } catch (Exception e) {
            try {
                logger.debug("Unable to find by name, searching by id instead: {}",env);
                return findEnvironmentById(env);
            } catch (NotFoundException notFoundException) {
                throw new NotFoundException("Environment not found, if using name this could be a permission issue, try using the environment id instead.",e);
            }
        }
    }

    @NotNull
    public Environment findEnvironmentByName(@NotNull String name) throws NotFoundException, HttpException {
        return Environment.findEnvironmentByName(name, client, this);
    }

    @NotNull
    public Environment findEnvironmentById(@NotNull String id) throws NotFoundException, HttpException {
        return Environment.findEnvironmentById(id, client, this);
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Organization getParentOrganization() throws HttpException {
        if (parentId != null) {
            try {
                return client.findOrganizationById(parentId);
            } catch (NotFoundException e) {
                throw (HttpException) e.getCause();
            }
        } else {
            return null;
        }
    }

    public Organization getRootOrganization() throws HttpException {
        if (parentId != null) {
            return getParentOrganization().getRootOrganization();
        } else {
            return this;
        }
    }

    public Environment createEnvironment(@NotNull String name, @NotNull Environment.Type type) throws HttpException {
        HashMap<String, String> request = new HashMap<>();
        request.put("name", name);
        request.put("type", type.name().toLowerCase());
        request.put("organizationId", id);
        String json = client.getHttpHelper().httpPost("https://anypoint.mulesoft.com/accounts/api/organizations/" + id + "/environments", request);
        return jsonHelper.readJson(createEnvironmentObject(), json);
    }

    public ClientApplication createClientApplication(String name, String url, String description) throws HttpException {
        return createClientApplication(name, url, description, null);
    }

    public ClientApplication createClientApplication(String name, String url, String description, String accessedAPIInstanceId) throws HttpException {
        // must always create the application in the root org because anypoint sucks
        return ClientApplication.create(getRootOrganization(), name, url, description, Collections.emptyList(), null, accessedAPIInstanceId);
    }

    public ClientApplication createClientApplication(String name, String url, String description, List<String> redirectUri, String apiEndpoints,
                                                     String accessedAPIInstanceId) throws HttpException {
        return ClientApplication.create(getRootOrganization(), name, url, description, redirectUri, apiEndpoints, accessedAPIInstanceId);
    }

    public Organization createSubOrganization(String name, String ownerId, boolean createSubOrgs, boolean createEnvironments) throws HttpException {
        return createSubOrganization(name, ownerId, createSubOrgs, createEnvironments, false,
                0, 0, 0, 0, 0, 0);
    }

    public List<ClientApplication> findAllClientApplications() throws HttpException {
        return findAllClientApplications(null);
    }

    public List<ClientApplication> findAllClientApplications(@Nullable String filter) throws HttpException {
        return ClientApplication.find(getRootOrganization(), filter);
    }

    public ClientApplication findClientApplicationById(@NotNull String id) throws HttpException, NotFoundException {
        final String json = httpHelper.httpGet("/exchange/api/v1/organizations/" + this.id + "/applications/" + id);
        return jsonHelper.readJson(new ClientApplication(this), json);
    }

    public ClientApplication findClientApplicationByName(@NotNull String name) throws HttpException, NotFoundException {
        return findClientApplicationByName(name, true);
    }

    public ClientApplication findClientApplicationByName(@NotNull String name, boolean fullData) throws HttpException, NotFoundException {
        ClientApplication app = null;
//        try {
//            app = findClientApplicationByName(new ClientApplicationList(this, name), name, fullData);
//            if (app == null) {
//                // #@$@##@$ anypoint filtering sometimes doesn't work
//                app = findClientApplicationByName(findAllClientApplications(name), name, fullData);
//            }
//        } catch (HttpException e) {
//            if( fullData && e.getStatusCode() == 401 ) {
        app = findClientApplicationByName(new ClientApplicationList(this, name), name, false);
        if (app == null) {
            // #@$@##@$ anypoint filtering sometimes doesn't work
            app = findClientApplicationByName(findAllClientApplications(name), name, false);
        }
        if (app != null) {
            app = findClientApplicationById(app.getId().toString());
        }
//            } else {
//                throw e;
//            }
//        }
        if (app == null) {
            throw new NotFoundException("Client application not found: " + name);
        } else {
            return app;
        }
    }

    @Nullable
    private ClientApplication findClientApplicationByName(Iterable<ClientApplication> list, @NotNull String name, boolean fullData) throws HttpException {
        for (ClientApplication app : list) {
            if (name.equals(app.getName())) {
                if (fullData) {
                    return jsonHelper.readJson(app, httpHelper.httpGet(app.getUriPath()));
                } else {
                    return app;
                }
            }
        }
        return null;
    }

    @NotNull
    public APISpecList findAPISpecs() throws HttpException {
        return findAPISpecsByFilter(null);
    }

    @NotNull
    public APISpecList findAPISpecsByFilter(@Nullable String filter) throws HttpException {
        return new APISpecList(this, filter);
    }

    public APISpec findAPISpecsByIdOrNameAndVersion(String idOrName, String version) throws NotFoundException, HttpException {
        for (APISpec apiSpec : findAPISpecs()) {
            if (apiSpec.getAssetId().equalsIgnoreCase(idOrName) && apiSpec.getVersion().equalsIgnoreCase(version)) {
                return apiSpec;
            }
        }
        return findAPISpecsByNameAndVersion(idOrName, version);
    }

    public APISpec findAPISpecsByNameAndVersion(String name, String version) throws NotFoundException, HttpException {
        for (APISpec apiSpec : findAPISpecsByFilter(name)) {
            if (apiSpec.getName().equalsIgnoreCase(name) && apiSpec.getVersion().equalsIgnoreCase(version)) {
                return apiSpec;
            }
        }
        throw new NotFoundException("Couldn't find api spec " + name + " " + version);
    }

    public Organization createSubOrganization(String name, String ownerId, boolean createSubOrgs, boolean createEnvironments,
                                              boolean globalDeployment, int vCoresProduction, int vCoresSandbox, int vCoresDesign,
                                              int staticIps, int vpcs, int loadBalancer) throws HttpException {
        JsonHelper.MapBuilder builder = client.getJsonHelper().buildJsonMap().set("name", name).set("parentOrganizationId", id).set("ownerId", ownerId);
        Map<String, Object> req = builder.addMap("entitlements").set("createSubOrgs", createSubOrgs).set("createEnvironments", createEnvironments)
                .set("globalDeployment", globalDeployment)
                .setNested("vCoresProduction", "assigned", vCoresProduction)
                .setNested("vCoresSandbox", "assigned", vCoresSandbox)
                .setNested("vCoresDesign", "assigned", vCoresDesign)
                .setNested("staticIps", "assigned", staticIps)
                .setNested("vpcs", "assigned", vpcs)
                .setNested("loadBalancer", "assigned", loadBalancer)
                .setNested("staticIps", "assigned", staticIps).toMap();
        String json = httpHelper.httpPost("/accounts/api/organizations", req);
        return jsonHelper.readJson(new Organization(client), json);
    }

    public void delete() throws HttpException {
        deleteSubElements();
        long timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60);
        for (; ; ) {
            try {
                httpHelper.httpDelete("/accounts/api/organizations/" + id);
                break;
            } catch (HttpException e) {
                if (System.currentTimeMillis() > timeout) {
                    throw e;
                } else {
                    deleteSubElements();
                    ThreadUtils.sleep(1500);
                }
            }
        }
    }

    private void deleteSubElements() throws HttpException {
        for (Environment environment : findAllEnvironments()) {
            for (APIAsset api : environment.findAPIs(null)) {
                api.delete();
            }
        }
        findExchangeAssets().delete();
    }

//    public RequestAPIAccessResult requestAPIAccess(String clientApplicationName, String apiName, String apiVersionName, boolean autoApprove, boolean autoRestore, String slaTier) throws HttpException, RequestAPIAccessException, NotFoundException {
//        ClientApplication clientApplication;
//        try {
//            clientApplication = findClientApplicationByName(clientApplicationName);
//        } catch (NotFoundException e) {
//            clientApplication = createClientApplication(clientApplicationName, "", "");
//        }
//        return requestAPIAccess(clientApplication, apiName, apiVersionName, autoApprove, autoRestore, slaTier);
//    }

//    public RequestAPIAccessResult requestAPIAccess(ClientApplication clientApplication, String apiName, String apiVersionName, boolean autoApprove, boolean autoRestore, String slaTier) throws HttpException, RequestAPIAccessException, NotFoundException {
//        logger.info("Requesting access from client application {} to api {} version {} with autoApprove {} autoRestore {} slaTier {}",
//                clientApplication.getName(),apiName,apiVersionName,autoApprove,autoRestore,slaTier);
//        APIVersion version = getAPI(apiName).getVersion(apiVersionName);
//        APIAccessContract contract;
//        try {
//            contract = clientApplication.findContract(version);
//        } catch (NotFoundException e) {
//            if (StringUtils.isEmpty(slaTier)) {
//                List<SLATier> tiers = version.getSLATiers();
//                slaTier = tiers.size() == 1 ? tiers.get(0).getName() : null;
//            }
//            SLATier tier = slaTier != null ? version.getSLATier(slaTier) : null;
//            contract = clientApplication.requestAPIAccess(version, tier);
//        }
//        if (contract.isPending()) {
//            if (autoApprove) {
//                contract = contract.approveAccess();
//                if (contract.isApproved()) {
//                    return RequestAPIAccessResult.GRANTED;
//                } else {
//                    throw new RequestAPIAccessException("Failed to auto-approve API access (status: " + contract.getStatus() + " )");
//                }
//            } else {
//                return RequestAPIAccessResult.PENDING;
//            }
//        } else if (contract.isRevoked()) {
//            if (autoRestore) {
//                contract = contract.restoreAccess();
//                if (contract.isApproved()) {
//                    return RequestAPIAccessResult.RESTORED;
//                } else {
//                    throw new RequestAPIAccessException("Failed to restore access to client application");
//                }
//            } else {
//                throw new RequestAPIAccessException("API access is currently revoked, cannot grant access");
//            }
//        } else if (contract.isApproved()) {
//            return RequestAPIAccessResult.GRANTED;
//        } else {
//            throw new RequestAPIAccessException("Unknown contract status: " + contract.getStatus());
//        }
//    }

    @JsonIgnore
    public String getUriPath() {
        return new URLBuilder("/apiplatform/repository/v2/organizations/").path(id).toString();
    }

    @Override
    public String toString() {
        return "Organization{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                "} " + super.toString();
    }

    public List<DesignCenterProject> findDesignCenterProjects() throws HttpException {
        // TODO implement pagination !!!!!!!
        String json = httpHelper.httpGet("/designcenter/api/v1/organizations/" + id + "/projects?pageSize=500&pageIndex=0");
        return jsonHelper.readJsonList(DesignCenterProject.class, json, this);
    }

    public DesignCenterProject findDesignCenterProject(String name) throws NotFoundException, HttpException {
        for (DesignCenterProject project : findDesignCenterProjects()) {
            if (name.equalsIgnoreCase(project.getName())) {
                return project;
            }
        }
        throw new NotFoundException();
    }

    public DesignCenterProject createDesignCenterProject(String name, String type, boolean visualDesignerMode, String ownerId) throws HttpException {
        return DesignCenterProject.create(this, name, type, visualDesignerMode, ownerId);
    }

    public AssetList findExchangeAssets() throws HttpException {
        return new AssetList(this, null, 50);
    }

    public AssetList findExchangeAssets(String filter, int limit) throws HttpException {
        return new AssetList(this, filter, limit);
    }

    public ExchangeAsset findExchangeAsset(@NotNull String groupId, @NotNull String assetId) throws HttpException, NotFoundException {
        logger.debug("searching exchange asset, groupId=" + groupId + " assetId=" + assetId);
        try {
            final String json = httpHelper.httpGet("/exchange/api/v2/assets/" + groupId + "/" + assetId);
            return jsonHelper.readJson(new ExchangeAsset(this), json);
        } catch (HttpException e) {
            if (e.getStatusCode() == 404) {
                throw new NotFoundException("Asset not found: " + groupId + ":" + assetId);
            } else {
                throw e;
            }
        }
    }

    public AssetVersion findExchangeAssetVersion(@NotNull String groupId, @NotNull String assetId, @NotNull String version) throws HttpException, NotFoundException {
        for (AssetVersion assetVersion : findExchangeAsset(groupId, assetId).getVersions()) {
            if (version.equals(assetVersion.getVersion())) {
                return assetVersion;
            }
        }
        throw new NotFoundException("Asset not found: " + groupId + ":" + assetId + ":" + version);
    }

    @NotNull
    protected Environment createEnvironmentObject() {
        return new Environment(this);
    }

    @NotNull
    protected Class<? extends Environment> getEnvironmentClass() {
        return Environment.class;
    }

    public VPC provisionVPC(VPCProvisioningDescriptor vd, boolean deleteExisting) throws NotFoundException, HttpException {
        if (deleteExisting) {
            try {
                VPC preExistingVPC = findVPCByName(vd.getName());
                preExistingVPC.delete();
            } catch (NotFoundException e) {
                logger.debug("No pre-existing VPC exists");
            }
        }
        VPC vpc = new VPC(vd.getName(), vd.getCidrBlock(), vd.isDefaultVpc(), vd.getRegion());
        List<String> envIds = new ArrayList<>();
        List<String> orgIds = new ArrayList<>();
        for (String envName : vd.getEnvironments()) {
            Environment env = findEnvironmentByName(envName);
            envIds.add(env.getId());
        }
        for (VPCOrgProvisioningDescriptor o : vd.getOrganizations()) {
            Organization subOrg = client.findOrganizationByNameOrId(o.getName());
            orgIds.add(subOrg.getId());
        }
        vpc.setAssociatedEnvironments(envIds);
        vpc.setSharedWith(orgIds);
        vpc.setFirewallRules(vd.getFirewallRules());
        vpc.setInternalDns(new VPCInternalDns(vd.getDnsServers(), vd.getDnsDomains()));
        String json = client.getHttpHelper().httpPost("/cloudhub/api/organizations/" + id + "/vpcs", vpc);
        vpc = client.getJsonHelper().readJson(new VPC(), json);
        for (VPCOrgProvisioningDescriptor o : vd.getOrganizations()) {
            Organization subOrg = client.findOrganizationByNameOrId(o.getName());
            List<String> eId = new ArrayList<>();
            for (String e : o.getEnvironments()) {
                Environment env = subOrg.findEnvironmentByName(e);
                eId.add(env.getId());
                Map<String, Object> req = jsonHelper.buildJsonMap().set("id", vpc.getId()).set("isDefault", vpc.isDefaultVpc()).set("associatedEnvironments", eId).toMap();
                client.httpHelper.anypointHttpPut("/cloudhub/api/organizations/" + subOrg.getId() + "/vpcs/" + vpc.getId(), req, env);
            }
        }
        return client.getJsonHelper().readJson(new VPC(), client.httpHelper.httpGet("/cloudhub/api/organizations/" + id + "/vpcs/" + vpc.getId()));
    }

    public void provisionVPC(File file, boolean deleteExisting) throws NotFoundException, HttpException, IOException {
        VPCProvisioningDescriptor vpcProvisioningDescriptor = jsonHelper.getJsonMapper().readValue(file, VPCProvisioningDescriptor.class);
        provisionVPC(vpcProvisioningDescriptor, deleteExisting);
    }

    public List<VPC> findVPCs() throws HttpException {
        String json = httpHelper.httpGet("/cloudhub/api/organizations/" + id + "/vpcs/");
        return jsonHelper.readJsonList(VPC.class, json, this, "/data");
    }

    public VPC findVPCByName(String name) throws NotFoundException, HttpException {
        for (VPC vpc : findVPCs()) {
            if (vpc.getName().equals(name)) {
                return vpc;
            }
        }
        throw new NotFoundException("VPC " + name + " not found");
    }

    public List<Target> findAllTargets() throws HttpException {
        String json = httpHelper.httpGet("/runtimefabric/api/organizations/" + id + "/targets");
        return jsonHelper.readJsonList(Target.class, json, this);
    }

    public Target findTargetById(String id) throws NotFoundException, HttpException {
        for (Target target : findAllTargets()) {
            if (target.getId().equals(id)) {
                return target;
            }
        }
        throw new NotFoundException("Target not found: " + id);
    }

    public ReleaseManifest findExchangeReleaseManifest(String uri) throws RESTException {
        String name = "Release Manifest: " + id;
        String artifactId = "relmanifest-" + id;
        String version;
        try {
            logger.debug("Searching exchange assets " + id + " : " + artifactId);
            ExchangeAsset asset = findExchangeAsset(id, artifactId);
            int oldestVersion = 0;
            for (AssetVersion assetVersion : asset.getVersions()) {
                String v = assetVersion.getVersion();
                Matcher m = MAJORVERSION_REGEX.matcher(v);
                logger.debug("Found version" + v);
                if (!m.find()) {
                    throw new IllegalStateException("Invalid manifest version in exchange: " + v);
                }
                try {
                    int vNb = Integer.parseInt(m.group(1));
                    if (vNb > oldestVersion) {
                        oldestVersion = vNb;
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Invalid manifest version in exchange: " + v);
                }
            }
            version = oldestVersion + ".0.0";
        } catch (HttpException e) {
            throw new RESTException(e, e.getStatusCode());
        } catch (NotFoundException e) {
            version = "1.0.0";
        }
        return null;
    }

    public @NotNull
    RoleGroupList findAllRoleGroups() throws HttpException {
        return new RoleGroupList(this);
    }

    public RoleGroup findRoleGroupById(String roleId) throws HttpException, NotFoundException {
        return RoleGroup.findById(this, httpHelper, jsonHelper, roleId);
    }

    public List<ProductPermissions> findAllProductPermissions() throws HttpException {
        String json = httpHelper.httpGet("/accounts/api/cs/organizations/" + id + "/permissions/products");
        return jsonHelper.readJsonList(ProductPermissions.class, json, this);
    }

    public List<Role> findAllRoles() throws HttpException {
        ArrayList<Role> results = new ArrayList<>();
        for (ProductPermissions pp : findAllProductPermissions()) {
            results.addAll(pp.getRoles());
        }
        return results;
    }

    public Map<String, Role> findAllRolesIndexedByName() throws HttpException {
        return findAllRoles().stream().collect(Collectors.toMap(Role::getName, v -> v));
    }

    public RoleGroup createRoleGroup(String name, String description) throws HttpException {
        HashMap<String, String> req = new HashMap<>();
        req.put("name", name);
        req.put("description", description);
        String json = httpHelper.httpPost("/accounts/api/organizations/" + id + "/rolegroups?include_internal=false", req);
        return jsonHelper.readJson(new RoleGroup(), json, this);
    }

    public RoleGroup findRoleGroupByName(String name) throws HttpException, NotFoundException {
        for (RoleGroup roleGroup : findAllRoleGroups()) {
            if (roleGroup.getName().equals(name)) {
                return roleGroup;
            }
        }
        throw new NotFoundException("Rolegroup named " + name + " not found");
    }

    public void createExchangeHTTPAPIAsset(String groupId, String name, String assetId, String version, String apiVersion) throws AssetProvisioningException {
        HttpHelper.MultiPartRequest req = httpHelper.createAnypointMultiPartPostRequest("/exchange/api/v1/assets", null);
        req.addText("organizationId", id);
        req.addText("groupId", groupId == null ? id : groupId);
        req.addText("assetId", assetId);
        req.addText("version", version);
        req.addText("name", name);
        req.addText("classifier", "http");
        req.addText("apiVersion", apiVersion);
        req.addText("asset", "undefined");
        try {
            req.execute();
        } catch (IOException e) {
            throw new AssetProvisioningException(e);
        }
    }

    public OrganizationDescriptor export(boolean stripIds) throws HttpException {
//        try {
        ModelMapper mapper = client.getModelMapper();
        OrganizationDescriptor org = new OrganizationDescriptor();
        mapper.map(this, org);
        if (stripIds) {
            org.setParentId(null);
            org.setId(null);
        }
//            exportEnvironments(mapper, org, stripIds);
//            exportRoleGroups(mapper, org, stripIds);
        exportAlerts(mapper, org, stripIds);
        return org;
//        } catch (NotFoundException e) {
//            throw new UnexpectedException(e);
//        }
    }

    private void exportAlerts(ModelMapper mapper, OrganizationDescriptor org, boolean stripIds) throws HttpException {
        ArrayList<AlertDescriptor> alerts = new ArrayList<>();
        ;
        org.setRuntimeAlerts(alerts);
        for (Environment environment : findAllEnvironments()) {
            for (Alert alert : environment.findAlerts()) {
                AlertDescriptor alertDesc = new AlertDescriptor();
                mapper.map(alert, alertDesc);
                if (stripIds) {
                    alertDesc.setId(null);
                }
                alerts.add(alertDesc);
            }
        }
    }

    private void exportEnvironments(ModelMapper mapper, OrganizationDescriptor org, boolean stripIds) throws HttpException {
        List<EnvironmentDescriptor> environments = mapper.map(findAllEnvironments(), new TypeToken<List<EnvironmentDescriptor>>() {
        }.getType());
        org.setEnvironments(environments);
    }

    private void exportRoleGroups(ModelMapper mapper, OrganizationDescriptor org, boolean stripIds) throws HttpException, NotFoundException {
        ArrayList<RoleDescriptor> roles = new ArrayList<>();
        for (RoleGroup roleGroup : findAllRoleGroups()) {
            if (roleGroup.isEditable()) {
                RoleDescriptor role = new RoleDescriptor();
                roles.add(role);
                mapper.map(roleGroup, role);
                if (stripIds) {
                    role.setId(null);
                }
                ArrayList<RolePermissionDescriptor> rolePermissions = new ArrayList<>();
                role.setPermissions(rolePermissions);
                Map<String, RolePermissionDescriptor> rolePermissionsIdx = new HashMap<>();
                for (RoleAssignment roleAssignment : roleGroup.findRoleAssignments()) {
                    String roleId = roleAssignment.getRoleId();
                    RolePermissionDescriptor rp = rolePermissionsIdx.get(roleId);
                    if (rp == null) {
                        rp = new RolePermissionDescriptor();
                        mapper.map(roleAssignment, rp);
                        if (stripIds) {
                            rp.setId(null);
                            rp.setRoleId(null);
                        }
                        rolePermissionsIdx.put(roleId, rp);
                        rolePermissions.add(rp);
                    }
                    String raOrgId = roleAssignment.getContextParams().get("org");
                    String raEnvId = roleAssignment.getContextParams().get("envId");
                    HashMap<String, Environment> envCache = new HashMap<>();
                    if (raEnvId != null) {
                        Environment raEnv = envCache.get(raEnvId);
                        if (raEnv == null) {
                            Organization raOrg = id.equals(raOrgId) ? this : client.findOrganizationById(raOrgId);
                            raEnv = raOrg.findEnvironmentById(raEnvId);
                            envCache.put(raEnvId, raEnv);
                        }
                        rp.addEnvironment(raEnv.getName());
                    }
                }
            }
        }
        org.setRoles(roles);
    }

    @NotNull
    private Role findRoleById(String roleId) throws HttpException, NotFoundException {
        return findAllRoles().stream().filter(r -> r.getId().equals(roleId)).findFirst().orElseThrow(() -> new NotFoundException("Role " + roleId + " not found"));
    }

    public ExchangeAsset publishExchangeAPIAsset(String name, String assetId, String assetVersion, String assetAPIVersion, String assetClassifier, String assetMainFile, File file) throws IOException, HttpException {
        return publishExchangeAsset(assetId, name, assetVersion, assetAPIVersion, assetClassifier, new FileStreamSource(file),
                new ExchangePublishingPart("main", assetMainFile));
    }

    public ExchangeAsset publishExchangeCustomAsset(String assetId, String name, String version, StreamSource streamSource) throws IOException, HttpException {
        return publishExchangeAsset(assetId, name, version, "v1", "custom", streamSource);
    }

    public ExchangeAsset publishExchangeAsset(String assetId, String name, String version, String apiVersion, String classifier, StreamSource streamSource, ExchangePublishingPart... parts) throws IOException, HttpException {
        final HttpHelper.MultiPartRequest req = getClient().getHttpHelper().createAnypointMultiPartPostRequest("/exchange/api/v1/assets", null);
        req.addText("classifier", classifier);
        req.addText("version", version);
        req.addText("name", name);
        req.addText("organizationId", id);
        req.addText("groupId", id);
        req.addText("assetId", assetId);
        req.addText("apiVersion", apiVersion);
        req.addBinary("asset", streamSource);
        if (parts != null) {
            for (ExchangePublishingPart part : parts) {
                req.addText(part.name, part.value);
            }
        }
        final String json = req.execute();
        return getClient().getJsonHelper().readJson(new ExchangeAsset(this), json);
    }

    private void publishExchangeAsset(ApplicationIdentifier applicationIdentifier, TempFile newFile) throws IOException {
        MavenHelper.publishArchive(applicationIdentifier, this, newFile);
    }

    public Fabric findFabricByName(String name) throws NotFoundException, HttpException {
        for (Fabric fabric : findAllFabric()) {
            if (fabric.getName().equalsIgnoreCase(name)) {
                return fabric;
            }
        }
        throw new NotFoundException("Fabric not found: " + name);
    }

    public List<Fabric> findAllFabric() throws HttpException {
        final String json = httpHelper.httpGet("/runtimefabric/api/organizations/" + id + "/fabrics");
        return jsonHelper.readJsonList(Fabric.class, json, parent);
    }

    public void promoteExchangeApplication(EnhancedMuleClient emClient, String groupId, String artifactId, String version, String newVersion) throws IOException, NotFoundException {
        if (groupId == null) {
            groupId = id;
        }
        boolean snapshotPromotion = false;
        if (newVersion == null) {
            int idx = version.toLowerCase().indexOf("-snapshot");
            if (idx != -1) {
                newVersion = version.substring(0, idx);
                snapshotPromotion = true;
            } else {
                throw new IllegalArgumentException("newVersion not set and version isn't a snapshot");
            }
        }
        try (final TempFile file = new TempFile("orig"); final TempFile newFile = new TempFile("new")) {
            final ApplicationIdentifier appId = new ApplicationIdentifier(groupId, artifactId, version);
            try (final FileOutputStream os = new FileOutputStream(file)) {
                IOUtils.copy(emClient.getExchangeClient().getAsset(groupId, artifactId, version, "mule-application", "jar"), os);
            }
            final ObjectNode descriptorJson = new FileApplicationSource(client, file, appId).getAnypointDescriptor();
            final ApplicationDescriptor anypointDescriptor = client.getJsonHelper().getJsonMapper().readerFor(ApplicationDescriptor.class).readValue(descriptorJson);
            final APIDescriptor apiDescriptor = anypointDescriptor.getApi();
            String snapshotApiVersion = null;
            if (apiDescriptor != null && apiDescriptor.getAsset() != null && apiDescriptor.getAsset().getCreate() &&
                    apiDescriptor.getAsset().getVersion() != null &&
                    apiDescriptor.getAsset().getVersion().toLowerCase().contains("-snapshot")) {
                snapshotApiVersion = apiDescriptor.getAsset().getVersion();
            }
            final Unpacker unpacker = new Unpacker(file, FileType.ZIP, newFile, FileType.ZIP);
            unpacker.addTransformers(ApplicationArchiveVersionTransformer.getTransformers(appId, groupId, newVersion, null));
            unpacker.unpack();
            publishExchangeAsset(new ApplicationIdentifier(groupId, artifactId, newVersion), newFile);
            if (snapshotPromotion) {
                String snapshotPrefix = newVersion.toLowerCase() + "-snapshot";
                deleteSnapshotAssets(groupId, artifactId, snapshotPrefix);
            }
            if (snapshotApiVersion != null) {
                int idx = snapshotApiVersion.toLowerCase().indexOf("-snapshot");
                snapshotApiVersion = snapshotApiVersion.substring(0, idx + 9);
                deleteSnapshotAssets(groupId, apiDescriptor.getAsset().getId(), snapshotApiVersion);
            }
        } catch (UnpackException e) {
            throw new IOException(e);
        }
    }

    private void deleteSnapshotAssets(String groupId, String artifactId, String snapshotPrefix) throws HttpException, NotFoundException {
        final ExchangeAsset exchangeAsset = findExchangeAsset(groupId, artifactId);
        for (AssetVersion exchangeAssetVersion : exchangeAsset.getVersions()) {
            final String v = exchangeAssetVersion.getVersion();
            if (v.toLowerCase().startsWith(snapshotPrefix)) {
                try {
                    exchangeAsset.deleteVersion(v);
                    eLogger.info(EMTLogger.Product.EXCHANGE, "Deleted exchange asset {} version {}", artifactId, v);
                } catch (Exception e) {
                    logger.warn("Unable to deleted exchange asset {} version {}: {}", artifactId, v, e.getMessage());
                }
            }
        }
    }

    public enum RequestAPIAccessResult {
        GRANTED, RESTORED, PENDING
    }

    public class ExchangePublishingPart {
        private String name;
        private String value;

        public ExchangePublishingPart(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
