/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.legacy.deploy.FileApplicationSource;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeployAndProvisionTests {
    @Captor
    ArgumentCaptor<ApplicationDescriptor> applicationDescriptorArgumentCaptor;
    @Captor
    ArgumentCaptor<DeploymentRequest> runtimeDeploymentRequestArgumentCaptor;
    private ObjectNode expectedJson;

    @BeforeEach
    private void beforeEachTest(TestInfo testInfo) throws Exception {
        String testName = testInfo.getTestMethod().orElseThrow(RuntimeException::new).getName();
        final ObjectMapper objectMapper = new ObjectMapper();
        expectedJson = (ObjectNode) objectMapper.readTree(getClass().getResource("/app/deployAndProvision/" + testName + "-expected.json"));
    }

    @Test
    public void deployAPI() throws Exception {
        final File appArchiveFile = new File(requireNonNull(getClass().getResource("/api-application.jar")).toURI());
        final AnypointClient anypointClient = DeploymentTestsHelper.createAnypointClient();
        final DeploymentServiceImpl deploymentService = new DeploymentServiceImpl(anypointClient);
        HashMap<String, String> vars = new HashMap<>();
        HashMap<String, String> properties = new HashMap<>();
        final FileApplicationSource applicationSource = new FileApplicationSource(anypointClient, appArchiveFile);
        final Environment environment = DeploymentTestsHelper.createMockEnvironment(anypointClient);
        final DeploymentRequest request = new DeploymentRequest(null,
                null, vars, properties, null, false, null, null,
                environment,
                true,
                true, false, applicationSource);
        request.getOverrideParameters().put("description","Cool Application");
        //noinspection unchecked
        when(anypointClient.deployApplicationToCH(eq(false), any(Environment.class), eq(true), any(Map.class),
                eq("api-application.jar"), any(FileInputStream.class), eq("testapp-prod")))
                .thenReturn("{\n" +
                        "  \"domain\": \"mydomain\",\n" +
                        "  \"versionId\": \"234243243\",\n" +
                        "  \"status\": \"STARTED\",\n" +
                        "  \"region\": \"us-west2\",\n" +
                        "  \"deploymentUpdateStatus\": \"STARTED\",\n" +
                        "  \"lastUpdateTime\": 2342334423243,\n" +
                        "  \"properties\": {\n" +
                        "  }\n" +
                        "}\n");
        deploymentService.deploy(request);
        verify(anypointClient).provisionApplication(applicationDescriptorArgumentCaptor.capture(),
                runtimeDeploymentRequestArgumentCaptor.capture());
        final ApplicationDescriptor applicationDescriptor = applicationDescriptorArgumentCaptor.getValue();
        final ObjectMapper objectMapper = JsonHelper.createMapper();
        Assertions.assertEquals(expectedJson, objectMapper.valueToTree(applicationDescriptor));
    }
}
