/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DeployOnlyTests {
    private AnypointClient anypointClient;
    private Environment environment;
    private ApplicationSource appSrc;
    private Organization organization;
    private HttpHelper httpHelper;
    private ObjectMapper objectMapper = new ObjectMapper();
    private ObjectNode expectedJson;
    private ObjectNode descJson;
    private HashMap<String, String> vars;
    private HashMap<String, String> properties;
    private DeploymentServiceImpl deploymentService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    private void beforeEachTest(TestInfo testInfo) throws Exception {
        String testName = testInfo.getTestMethod().orElseThrow(RuntimeException::new).getName();
        descJson = (ObjectNode) objectMapper.readTree(getClass().getResource("/app/deploy/" + testName + ".json"));
        expectedJson = (ObjectNode) objectMapper.readTree(getClass().getResource("/app/deploy/" + testName + "-expected.json"));
        anypointClient = DeploymentTestsHelper.createAnypointClient();
        environment = DeploymentTestsHelper.createMockEnvironment(anypointClient);
        appSrc = mock(ApplicationSource.class);
        when(appSrc.getAnypointDescriptor()).thenReturn(descJson);
        when(appSrc.getFileName()).thenReturn("testapp-1.0.0.jar");
        when(appSrc.getArtifactId()).thenReturn("testapp");
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
        final DeploymentRequest request = new DeploymentRequest(DeploymentTestsHelper.FILE_JAR,
                DeploymentTestsHelper.BUILD_NUMBER, vars, properties, null, false, null, organization, environment, true,
                true, true, appSrc);
        new DeploymentServiceImpl(anypointClient).deploy(request);
        verifyCHNewDeploymentJson();
    }

    @Test
    void deployCHDefault() throws Exception {
        final DeploymentRequest request = new DeploymentRequest(null,
                null, vars, properties, null, false, null, organization, environment,
                true,
                true, true, appSrc);
        deploymentService.deploy(request);
        verifyCHNewDeploymentJson();
    }

    @Test
    void deployCHCustomLogger() throws Exception {
        final DeploymentRequest request = new DeploymentRequest(null,
                null, vars, properties, null, false, "cloudhub", organization, environment, true,
                true, true, appSrc);
        deploymentService.deploy(request);
        verifyCHNewDeploymentJson();
    }

    @Test
    void deployCHOverrideDescriptor() throws Exception {
        final DeploymentRequest request = new DeploymentRequest(null,
                null, vars, properties, null, false, "cloudhub", organization, environment, true,
                true, true, appSrc);
        deploymentService.deploy(request);
        verifyCHNewDeploymentJson();
    }

    @Test
    void deployRTF() throws Exception {
        final DeploymentRequest request = new DeploymentRequest(null,
                null, vars, properties, null, false, "rtf:"+ DeploymentTestsHelper.MYFABRIC, organization, environment, true,
                true, true, appSrc);
        deploymentService.deploy(request);
        verifyRTFNewDeploymentJson();
    }

    private void verifyCHNewDeploymentJson() throws IOException {
        Assertions.assertEquals(expectedJson, captureDeploymentNewApp());
    }

    @SuppressWarnings("unchecked")
    private JsonNode captureDeploymentNewApp() throws HttpException {
        ArgumentCaptor<Map<String,Object>> jsonCapt = ArgumentCaptor.forClass(Map.class);
        verify(anypointClient.getHttpHelper()).anypointHttpPost(anyString(),jsonCapt.capture(),any());
        return objectMapper.valueToTree(jsonCapt.getValue());
    }

    private void verifyRTFNewDeploymentJson() throws IOException {
        Assertions.assertEquals(expectedJson, captureRTFDeploymentNewApp());
    }

    @SuppressWarnings("unchecked")
    private JsonNode captureRTFDeploymentNewApp() throws HttpException {
        ArgumentCaptor<Map<String,Object>> jsonCapt = ArgumentCaptor.forClass(Map.class);
        verify(anypointClient.getHttpHelper()).httpPost(anyString(),jsonCapt.capture());
        return objectMapper.valueToTree(jsonCapt.getValue());
    }
}
