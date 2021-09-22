/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationDescriptorProcessorTest {
//    @Test
//    public void testDefaultWithAPIDependency() throws Exception {
//        final Helper helper = new Helper("defaultWithAPIDep", true);
//        helper.addDependency("company.com", "myproject-spec", "3.2.1", "oas", "anypoint-sys-api-spec-1.0.0-oas.zip");
//        helper.testDefaultValues();
//    }
//
//    @Test
//    public void testWithAPISpec() throws Exception {
//        final Helper helper = new Helper("withramlspec", true);
//        helper.testDefaultValues();
//    }
//
//    @Test
//    public void testWithAPISpecSnapshot() throws Exception {
//        final Helper helper = new Helper("withramlspecSnapshot", true);
//        helper.testDefaultValues();
//    }

    public class Helper {
        private final File baseDir;
        MavenProject project;
        private final ApplicationDescriptorProcessorImpl processor;

        public Helper(String test, boolean apikit) throws URISyntaxException, IOException {
            project = Mockito.mock(MavenProject.class);
            Mockito.when(project.getArtifactId()).then((Answer<String>) invocationOnMock -> "myproject");
            Mockito.when(project.getVersion()).then((Answer<String>) invocationOnMock -> "2.0.1");
            Mockito.when(project.getName()).then((Answer<String>) invocationOnMock -> "My Project");
            final String anypointFileName = "/" + test + "/anypoint.json";
            final File descriptorFile = new File(getClass().getResource(anypointFileName).toURI());
            baseDir = descriptorFile.getParentFile();
            Mockito.when(project.getBasedir()).then(new Returns(baseDir));
            processor = new ApplicationDescriptorProcessorImpl(descriptorFile.getPath(), project,
                    new File(baseDir, "pages"), new File(baseDir, "api"),
                    new ApplicationSourceMetadataProjectSourceImpl(project, new File(baseDir, "pages"), new File(baseDir, "api")));
        }

        public void addDependency(String groupId, String artifactId, String version, String classification, String file) {
            final Dependency apiDependency = new Dependency();
            apiDependency.setArtifactId(artifactId);
            apiDependency.setVersion(version);
            apiDependency.setClassifier(classification);
            apiDependency.setGroupId(groupId);
            Mockito.when(project.getDependencies()).then(new Returns(Collections.singletonList(apiDependency)));
            Artifact artifact = new DefaultArtifact(groupId, artifactId, version, "compile", "jar", null, new DefaultArtifactHandler());
            artifact.setFile(new File(baseDir, file));
            Mockito.when(project.getArtifacts()).then(new Returns(new HashSet<>(Collections.singletonList(artifact))));
        }

        public void testDefaultValues() throws IOException {
            processor.setDefaultValues(true);
            final ObjectNode descriptor = processor.getApplicationDescriptorJson();
            final JsonNode expectedJson = new ObjectMapper().readTree(new File(baseDir, "expected.json"));
            assertEquals(expectedJson, descriptor);
        }
    }
}
