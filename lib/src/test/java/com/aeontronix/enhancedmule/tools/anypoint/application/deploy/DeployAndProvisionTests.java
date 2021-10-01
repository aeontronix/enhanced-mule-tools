/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.legacy.deploy.FileApplicationSource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class DeployAndProvisionTests {
    @Test
    public void deployAPI() throws Exception {
        final File appArchiveFile = new File(requireNonNull(getClass().getResource("/api-application.jar")).toURI());
        final AnypointClient anypointClient = DeploymentTestsHelper.createAnypointClient();
        final DeploymentServiceImpl deploymentService = new DeploymentServiceImpl(anypointClient);
        HashMap<String, String> vars = new HashMap<>();
        HashMap<String, String> properties = new HashMap<>();
        final FileApplicationSource applicationSource = new FileApplicationSource(anypointClient, appArchiveFile);
        final RuntimeDeploymentRequest request = new RuntimeDeploymentRequest(null,
                null, vars, properties, null, false, null, DeploymentTestsHelper.createMockEnvironment(anypointClient),
                true,
                true, true, null, applicationSource);
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
    }
}
