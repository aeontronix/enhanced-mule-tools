/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.enhancedmule.tools.*;
import com.aeontronix.enhancedmule.tools.api.API;
import com.aeontronix.enhancedmule.tools.api.DesignCenterProject;
import com.aeontronix.enhancedmule.tools.api.DesignCenterProjectExchange;
import com.aeontronix.enhancedmule.tools.api.policy.Policy;
import com.aeontronix.enhancedmule.tools.provisioning.AnypointDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.provisioning.api.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractAnypointTest {
    public static final String TESTPREFIX = "atutestdeleteme";
    public static final String TESTAPI1 = "testapi1";
    public static final String TESTAPI2 = "testapi2";
    public static final String V1 = "1.0.0";
    public static final String CLIENT_ID_ENFORCEMENT = "client-id-enforcement";
    public static final String ATTRIBUTES_HEADERS_CLIENT_SECRET = "#[attributes.headers['client_secret']]";
    public static final String ATTRIBUTES_HEADERS_CLIENT_SECRET2 = "#[attributes.headers['client_so_secret']]";
    public static final String CLIENT_SECRET_EXPRESSION = "clientSecretExpression";
    protected AnypointClient client;
    protected @NotNull Organization org;
    protected boolean integrationTest = true;
    protected Environment env;
    private File testRecordingFile;
    private HttpHelperRecorder httpHelperRecorder;
    private String orgName;

    @BeforeEach
    public void init(TestInfo testInfo) throws Exception {
        File resourceDir = new File("src/test/resources");
        if (!resourceDir.exists()) {
            throw new RuntimeException("Resource directory not found (make sure the test is using project root as working dir): " + resourceDir.getPath());
        }
        testRecordingFile = new File(resourceDir, "test-" + getTestName(testInfo) + ".json");
        if (integrationTest) {
            String testName = getTestName(testInfo);
            System.out.println("Recording test: " + testName);
            client = new AnypointClient();
            deleteTestOrgs();
            orgName = TESTPREFIX + String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", new Date());
            System.out.println("Creating test org " + orgName);
            org = client.createOrganization(orgName);
            env = org.createEnvironment(orgName, Environment.Type.SANDBOX);
//            httpHelperRecorder = new HttpHelperRecorder(client, username, password, orgName);
//            client.setHttpHelper(httpHelperRecorder);
            org = client.findOrganization(orgName);
            env = org.findEnvironmentByName(orgName);
        } else {
            client = new AnypointClient();
            HttpHelperReplayer httpHelper = new HttpHelperReplayer(testRecordingFile);
            orgName = httpHelper.getOrgName();
            client.setHttpHelper(httpHelper);
            org = client.findOrganization(orgName);
            env = org.findEnvironmentByName(orgName);
        }
    }

    private void deleteTestOrgs() throws HttpException {
        for (Organization organization : client.findOrganizations()) {
            if (organization.getName().startsWith(TESTPREFIX)) {
                try {
                    organization.delete();
                } catch (Exception e) {
                    System.out.println("Unable to delete org: " + organization.getName());
                }
            }
        }
    }

    @AfterEach
    public void cleanup() throws IOException, HttpException {
        if (httpHelperRecorder != null) {
            new ObjectMapper().writer().writeValue(testRecordingFile, httpHelperRecorder.getRecording());
            deleteTestOrgs();
        }
    }

    public static String getTestName(TestInfo testInfo) {
        return testInfo.getDisplayName().replaceAll("\\(\\)", "");
    }

    @Contract("_ -> param1")
    protected AnypointDescriptor addClientIdPolicy(AnypointDescriptor apd) {
        if (apd.getApi() != null) {
            apd.setApi(new APIDescriptor());
        }
        apd.getApi().addPolicy(new PolicyDescriptor("68ef9520-24e9-4cf2-b2f5-620025690913",
                APIProvisioningITCase.CLIENT_ID_ENFORCEMENT, "1.1.1", new HashMap<String, Object>() {
            {
                put("credentialsOriginHasHttpBasicAuthenticationHeader", "customExpression");
                put("clientIdExpression", "#[attributes.headers['client_id']]");
                put(APIProvisioningITCase.CLIENT_SECRET_EXPRESSION, APIProvisioningITCase.ATTRIBUTES_HEADERS_CLIENT_SECRET);
            }
        }));
        return apd;
    }

    protected void checkPolicy(String name, String version, String secretExpr) throws HttpException, NotFoundException {
        API api = env.findAPIByExchangeAssetNameAndVersion(name, version);
        List<Policy> policies = api.findPolicies();
        assertEquals(1, policies.size());
        Policy policy = policies.get(0);
        Assertions.assertEquals(APIProvisioningITCase.CLIENT_ID_ENFORCEMENT, policy.getAssetId());
        assertEquals(((Map) policy.getConfigurationData()).get("clientSecretExpression"), secretExpr);
    }

    protected DesignCenterProject createAPIAsset(String name, boolean publish) throws HttpException {
        DesignCenterProject apiProject;
        try {
            apiProject = org.findDesignCenterProject(name);
        } catch (NotFoundException e) {
            System.out.println("Creating API " + name);
            apiProject = org.createDesignCenterProject(name, "raml", false, client.getUser().getId());
        }
        if (publish) {
            DesignCenterProjectExchange exchange = apiProject.getExchange("master");
            if (!exchange.isPublishedVersion()) {
                exchange.publish(APIProvisioningITCase.V1, null);
            }
        }
        return apiProject;
    }

    protected APIProvisioningResult provision(APIProvisioningConfig config, AnypointDescriptor apd2) throws ProvisioningException {
        APIProvisioningResult provision = apd2.provision(env, config, null);
        // todo register client and delete them after tests
        return provision;
    }
}
