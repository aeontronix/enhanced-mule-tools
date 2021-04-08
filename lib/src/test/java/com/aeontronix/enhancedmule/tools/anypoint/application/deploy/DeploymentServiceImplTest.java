/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.cloudhub.CHMuleVersion;
import com.aeontronix.enhancedmule.tools.cloudhub.CHRegion;
import com.aeontronix.enhancedmule.tools.cloudhub.CHWorkerType;
import com.aeontronix.enhancedmule.tools.cloudhub.MuleVersionUpdate;
import com.aeontronix.enhancedmule.tools.fabric.Fabric;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.runtime.Target;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DeploymentServiceImplTest {
    public static final String FILE_JAR = "file.jar";
    public static final String APP_NAME = "myapp";
    public static final String ARTIFACT_ID = "appartid";
    public static final String BUILD_NUMBER = "1000";
    public static final String ORG_ID = "428536327643427636";
    public static final String MY_ORG = "MyOrg";
    public static final String MICRO = "micro";
    public static final String US_WEST_4 = "us-west-4";
    public static final String MYFABRIC = "myfabric";
    public static final String ENV_ID = "42798472398234-243432243-243432234";
    private static final String ENV_NAME = "ProdEnv";
    private AnypointClient anypointClient;
    private Environment environment;
    private ApplicationSource appSrc;
    private Organization organization;
    private HttpHelper httpHelper;
    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode expectedJson;
    private JsonNode descJson;
    private HashMap<String, String> vars;
    private HashMap<String, String> properties;
    private DeploymentServiceImpl deploymentService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    private void beforeAll(TestInfo testInfo) throws Exception {
        String testName = testInfo.getTestMethod().orElseThrow(RuntimeException::new).getName();
        descJson = objectMapper.readTree(getClass().getResource("/app/deploy/" + testName + ".json"));
        expectedJson = objectMapper.readTree(getClass().getResource("/app/deploy/" + testName + "-expected.json"));
        anypointClient = mock(AnypointClient.class);
        httpHelper = mock(HttpHelper.class);
        when(httpHelper.anypointHttpPost(any(),any(),any())).thenReturn("{}");
        final JsonHelper jsonHelper = new JsonHelper(anypointClient);
        when(anypointClient.getJsonHelper()).thenReturn(jsonHelper);
        when(anypointClient.getHttpHelper()).thenReturn(httpHelper);
        environment = mock(Environment.class);
        when(environment.getName()).thenReturn(ENV_NAME);
        when(environment.getId()).thenReturn(ENV_ID);
        when(environment.getType()).thenReturn(Environment.Type.PRODUCTION);
        when(environment.refresh()).thenReturn(environment);
        when(environment.findCHMuleVersion("1.2.3")).thenReturn(new CHMuleVersion("1.2.3", new MuleVersionUpdate("5323")));
        when(environment.findDefaultCHMuleVersion()).thenReturn(new CHMuleVersion("4.3.55", new MuleVersionUpdate("3434")));
        when(environment.findDefaultCHRegion()).thenReturn(new CHRegion(US_WEST_4,"zoomgalan"));
        when(environment.getClient()).thenReturn(anypointClient);
        when(environment.findWorkerTypeByName("huge")).thenReturn(new CHWorkerType("huge"));
        when(environment.findWorkerTypeByName("gigantic")).thenReturn(new CHWorkerType("gigantic"));
        when(environment.findSmallestWorkerType()).thenReturn(new CHWorkerType(MICRO));
        when(environment.getSuffix()).thenReturn("-prod");
        organization = mock(Organization.class);
        when(organization.getId()).thenReturn(ORG_ID);
        when(organization.getName()).thenReturn(MY_ORG);
        when(organization.findFabricByName(MYFABRIC)).thenReturn(new Fabric());
        final Target fabricTarget = new Target();
        fabricTarget.setRuntimes(singletonList(new Target.Runtime("mule", singletonList(new Target.RuntimeVersion("1.2.3")))));
        when(organization.findTargetById(any())).thenReturn(fabricTarget);
        when(environment.getOrganization()).thenReturn(organization);
        when(environment.findServerByName(MYFABRIC)).thenThrow(new NotFoundException());
        appSrc = mock(ApplicationSource.class);
        when(appSrc.getFileName()).thenReturn("testapp-1.0.0.jar");
        when(appSrc.getApplicationIdentifier()).thenReturn(new ApplicationIdentifier("com.mycompany","testapp","1.0.0"));
        vars = new HashMap<>();
        properties = new HashMap<>();
        deploymentService = new DeploymentServiceImpl(anypointClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    void deployOverrideValues() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        final HashMap<String, String> vars = new HashMap<>();
        final HashMap<String, String> properties = new HashMap<>();
        properties.put("color", "blue");
        final RuntimeDeploymentRequest request = new RuntimeDeploymentRequest(FILE_JAR, APP_NAME, ARTIFACT_ID,
                BUILD_NUMBER, vars, properties, null, null, environment, true,
                true, true, null);
        new DeploymentServiceImpl(anypointClient).deploy(request, objectMapper.createObjectNode(), appSrc);
        verifyCHNewDeploymentJson();
    }

    @Test
    void deployCHDefault() throws Exception {
        final RuntimeDeploymentRequest request = new RuntimeDeploymentRequest(null, null, ARTIFACT_ID,
                null, vars, properties, null, null, environment, true,
                true, true, null);
        deploymentService.deploy(request, (ObjectNode) descJson, appSrc);
        verifyCHNewDeploymentJson();
    }

    @Test
    void deployCHCustomLogger() throws Exception {
        final RuntimeDeploymentRequest request = new RuntimeDeploymentRequest(null, null, ARTIFACT_ID,
                null, vars, properties, null, "cloudhub", environment, true,
                true, true, null);
        deploymentService.deploy(request, (ObjectNode) descJson, appSrc);
        verifyCHNewDeploymentJson();
    }

    @Test
    void deployCHOverrideDescriptor() throws Exception {
        final RuntimeDeploymentRequest request = new RuntimeDeploymentRequest(null, null, ARTIFACT_ID,
                null, vars, properties, null, "cloudhub", environment, true,
                true, true, null);
        deploymentService.deploy(request, (ObjectNode) descJson, appSrc);
        verifyCHNewDeploymentJson();
    }

    @Test
    void deployRTF() throws Exception {
        final RuntimeDeploymentRequest request = new RuntimeDeploymentRequest(null, null, ARTIFACT_ID,
                null, vars, properties, null, null, environment, true,
                true, true, null);
        deploymentService.deploy(request, (ObjectNode) descJson, appSrc);
        verifyRTFNewDeploymentJson();
    }

    private void verifyCHNewDeploymentJson() throws IOException {
        Assertions.assertEquals(expectedJson, captureDeploymentNewApp());
    }

    @SuppressWarnings("unchecked")
    private JsonNode captureDeploymentNewApp() throws HttpException {
        ArgumentCaptor<Map<String,Object>> jsonCapt = ArgumentCaptor.forClass(Map.class);
        verify(httpHelper).anypointHttpPost(anyString(),jsonCapt.capture(),any());
        return objectMapper.valueToTree(jsonCapt.getValue());
    }

    private void verifyRTFNewDeploymentJson() throws IOException {
        Assertions.assertEquals(expectedJson, captureRTFDeploymentNewApp());
    }

    @SuppressWarnings("unchecked")
    private JsonNode captureRTFDeploymentNewApp() throws HttpException {
        ArgumentCaptor<Map<String,Object>> jsonCapt = ArgumentCaptor.forClass(Map.class);
        verify(httpHelper).httpPost(anyString(),jsonCapt.capture());
        return objectMapper.valueToTree(jsonCapt.getValue());
    }
}
