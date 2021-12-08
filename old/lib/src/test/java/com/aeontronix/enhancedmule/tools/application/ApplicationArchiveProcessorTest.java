/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationEnhancer;
import com.aeontronix.enhancedmule.tools.application.api.apikit.DependencyAPIKitSpec;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.unpack.UnpackException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.when;

class ApplicationArchiveProcessorTest {
    public static final String APP_NAME = "My Application";
    public static final String APP_ID = "my-application";
    public static final String APP_VERS = "1.1.1";
    public static final String API_GROUP_ID = "api-group-id";
    public static final String API_ARTIFACT_ID = "api-artifact-id";
    public static final String API_VERSION = "5.5";
    private ObjectMapper objectMapper = new ObjectMapper();
    private ApplicationSource src;
    private String testMethod;
    private ApplicationSourceMetadata mSrc;

    @BeforeEach
    public void init(TestInfo testInfo) throws IOException {
        mSrc = Mockito.mock(ApplicationSourceMetadata.class);
        when(mSrc.getName()).thenReturn(APP_NAME);
        when(mSrc.getArtifactId()).thenReturn(APP_ID);
        when(mSrc.getVersion()).thenReturn(APP_VERS);
        src = Mockito.mock(ApplicationSource.class);
        when(src.getApplicationSourceMetadata()).thenReturn(mSrc);
        testMethod = testInfo.getDisplayName().replace("(", "").replace(")", "");
    }

    private void process() throws IOException {
        final ObjectNode appDesc = getResource(testMethod + ".json");
        ApplicationDescriptorDefaultValues.setDefaultValues(src.getApplicationSourceMetadata(), appDesc, objectMapper);
        Assertions.assertEquals(appDesc, getResource(testMethod + "-expected.json"));
    }

    @Test
    public void testMinimal() throws IOException {
        process();
    }

    @Test
    public void testMavenDepAPI() throws IOException {
        ramlDependencyExists("v5");
        process();
    }

    private void ramlDependencyExists(String apiVersion) throws IOException {
        when(mSrc.findAPIKitSpecs()).thenReturn(Collections.singletonList(new DependencyAPIKitSpec(API_ARTIFACT_ID,
                API_GROUP_ID, API_ARTIFACT_ID, API_VERSION, null, null, null)));
        if (apiVersion != null) {
            final ObjectNode exchangeJson = objectMapper.createObjectNode();
            exchangeJson.put("apiVersion", apiVersion);
            when(mSrc.getJsonContentFromDependencyArchive(API_GROUP_ID, API_ARTIFACT_ID, API_VERSION, "exchange.json"))
                    .thenReturn(exchangeJson);
        }
    }

    private ObjectNode getResource(String path) throws IOException {
        return (ObjectNode) objectMapper.readTree(getClass().getResource("/application-descriptor-default-values/" + path));
    }
}
