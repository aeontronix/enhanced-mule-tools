/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.commons.xml.XPathUtils;
import com.aeontronix.commons.xml.XmlUtils;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.RESTException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ApplicationSourceEnhancer {
    private RESTClient restClient;
    private File projectDir;

    public ApplicationSourceEnhancer(RESTClient restClient, File projectDir) {
        this.restClient = restClient;
        this.projectDir = projectDir;
    }

    public void execute() throws ApplicationSourceEnhancementException {
        try {
            final File pomFile = new File(projectDir, "pom.xml");
            if (!pomFile.exists()) {
                throw new IOException("pom.xml not found: " + pomFile.getPath());
            }
            final Document pomDoc = XmlUtils.parse(pomFile, false);
            setupEmtMavenPlugin(pomDoc);
            setupAnypointJson(projectDir);
            try (final FileOutputStream fos = new FileOutputStream(pomFile)) {
                XmlUtils.serialize(pomDoc, fos, true, true);
            }
        } catch (Exception e) {
            throw new ApplicationSourceEnhancementException(e);
        }
    }

    private void setupAnypointJson(File projectDir) throws IOException {
        final File file = new File(projectDir, "anypoint.json");
        if (!file.exists()) {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            final ObjectNode defs = (ObjectNode) ApplicationDescriptor.createDefault(objectMapper);
            final ObjectNode desc = objectMapper.createObjectNode();
            desc.put("$schema", "https://docs.enhanced-mule.com/schemas/emule-application-1.0.schema.json");
            desc.set("deploymentParams", defs.get("deploymentParams"));
            final ArrayNode overrides = objectMapper.createArrayNode();
            desc.set("overrides", overrides);
            final ObjectNode override = objectMapper.createObjectNode();
            overrides.add(override);
            override.put("type", "envType");
            override.put("value", "production");
            final ObjectNode overrideObj = (ObjectNode) ApplicationDescriptor.createDefault(objectMapper);
            override.set("override", overrideObj);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, desc);
        }
    }

    private void setupEmtMavenPlugin(Document pomDoc) throws XPathExpressionException, RESTException {
        String emtVersion = getLatestVersion("14801271");
        final Element emPluginVersion = XPathUtils.evalXPathElement("//build/plugins/plugin[ artifactId/text() = 'enhanced-mule-tools-maven-plugin' and groupId/text() = 'com.aeontronix.enhanced-mule']/version", pomDoc);
        if (emPluginVersion == null) {
            final Element build = XmlUtils.getChildElement(pomDoc.getDocumentElement(), "build", true);
            final Element plugins = XmlUtils.getChildElement(build, "plugins", true);
            final Element plugin = XmlUtils.createElement("plugin", plugins);
            XmlUtils.createElement("groupId", plugin).setTextContent("com.aeontronix.enhanced-mule");
            XmlUtils.createElement("artifactId", plugin).setTextContent("enhanced-mule-tools-maven-plugin");
            XmlUtils.createElement("version", plugin).setTextContent(emtVersion);
            final Element executions = XmlUtils.createElement("executions", plugin);
            final Element execution = XmlUtils.createElement("execution", executions);
            final Element goals = XmlUtils.createElement("goals", execution);
            XmlUtils.createElement("goal", goals).setTextContent("process-descriptor");
            XmlUtils.createElement("goal", goals).setTextContent("deploy");
        } else {
            emPluginVersion.setTextContent(emtVersion);
        }
    }

    public void setupEMPConnector() throws RESTException {
        String empVersion = getLatestVersion("39986379");
    }

    @SuppressWarnings("unchecked")
    private String getLatestVersion(String projectId) throws RESTException {
        final Map<String, String> rel = (Map<String, String>) restClient.get("https://gitlab.com/api/v4/projects/" + projectId + "/releases", List.class).get(0);
        return rel.get("tag_name").substring(1);
    }
}
