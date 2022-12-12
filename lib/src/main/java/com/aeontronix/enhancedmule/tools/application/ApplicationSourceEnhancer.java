/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.commons.file.FileUtils;
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
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class ApplicationSourceEnhancer {
    private static final Logger logger = getLogger(ApplicationSourceEnhancer.class);
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
            disableExchangePreDeploy(pomDoc);
            setupAnypointJson(projectDir);
            setupEnhancedMuleProperties(projectDir, pomDoc);
            try (final FileOutputStream fos = new FileOutputStream(pomFile)) {
                XmlUtils.serialize(pomDoc, fos, true, true);
            }
        } catch (Exception e) {
            throw new ApplicationSourceEnhancementException(e);
        }
        logger.info("Application enhancement completed");
    }

    private void setupEnhancedMuleProperties(File projectDir, Document pomDoc) throws XPathExpressionException, IOException, RESTException {
        String artifactId = "enhanced-mule-properties-provider";
        String groupId = "com.aeontronix.enhanced-mule";
        String newVersion = getLatestVersion("39986379");
        final Element depVersion = XPathUtils.evalXPathElement("//dependencies/dependency[ artifactId/text() = '"
                + artifactId + "' and groupId/text() = '" + groupId + "']/version", pomDoc);
        if (depVersion != null) {
            final String oldVersion = depVersion.getTextContent().trim();
            if (!oldVersion.equals(newVersion)) {
                depVersion.setTextContent(newVersion);
                logger.info("Updated version of " + groupId + ":" + artifactId + " from " + oldVersion + " to " + newVersion);
            }
        } else {
            final Element dependencies = XmlUtils.getChildElement(pomDoc.getDocumentElement(), "dependencies", true);
            final Element dependency = XmlUtils.createElement("dependency", dependencies);
            XmlUtils.createElement("groupId", dependency).setTextContent(groupId);
            XmlUtils.createElement("artifactId", dependency).setTextContent(artifactId);
            XmlUtils.createElement("version", dependency).setTextContent(newVersion);
            XmlUtils.createElement("classifier", dependency).setTextContent("mule-plugin");
            logger.info("Added dependency " + groupId + ":" + artifactId + ":" + newVersion);
            final File propertiesXml = new File(projectDir.getPath() + File.separator + "src" + File.separator + "main" +
                    File.separator + "mule" + File.separator + "properties.xml");
            if (!propertiesXml.exists()) {
                FileUtils.write(propertiesXml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "\n" +
                        "<mule xmlns:enhanced-mule-properties=\"http://www.mulesoft.org/schema/mule/enhanced-mule-properties\" xmlns=\"http://www.mulesoft.org/schema/mule/core\"\n" +
                        "\txmlns:doc=\"http://www.mulesoft.org/schema/mule/documentation\"\n" +
                        "\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd\n" +
                        "http://www.mulesoft.org/schema/mule/enhanced-mule-properties http://www.mulesoft.org/schema/mule/enhanced-mule-properties/current/mule-enhanced-mule-properties.xsd\">\n" +
                        "\t<enhanced-mule-properties:config name=\"Enhanced_Mule_Properties_Config\" doc:name=\"Enhanced Mule Properties Config\" doc:id=\"a35802ca-2aa9-43c0-8c16-11db87f94841\" />\n" +
                        "</mule>\n");
                logger.info("Added src/main/mule/properties.xml");
            }
        }
        final File propFile = new File(projectDir.getPath() + File.separator + "src" + File.separator + "main" +
                File.separator + "resources" + File.separator + "properties.yaml");
        if (!propFile.exists()) {
            FileUtils.write(propFile, "http.listener:\n" +
                    "  type: https\n" +
                    "  name: HTTPS Listener properties\n" +
                    "  description: HTTP listener properties plus self-signed cert");
        }
    }

    private void setupAnypointJson(File projectDir) throws IOException {
        final File file = new File(projectDir, "anypoint.json");
        if (!file.exists()) {
            logger.info("anypoint.json missing, creating");
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
        } else {
            logger.info("anypoint.json exists, skipping");
        }
    }

    private void setupEmtMavenPlugin(Document pomDoc) throws XPathExpressionException, RESTException {
        String emtVersion = getLatestVersion("14801271");
        final Element emPluginVersion = XPathUtils.evalXPathElement("//build/plugins/plugin[ artifactId/text() = 'enhanced-mule-tools-maven-plugin' and groupId/text() = 'com.aeontronix.enhanced-mule']/version", pomDoc);
        final Element mvnProject = pomDoc.getDocumentElement();
        if (emPluginVersion == null) {
            logger.info("EMT maven plugin missing, adding");
            final Element build = XmlUtils.getChildElement(mvnProject, "build", true);
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
            logger.info("EMT maven plugin found, updating version to " + emtVersion);
            emPluginVersion.setTextContent(emtVersion);
        }
        final Element properties = XmlUtils.getChildElement(mvnProject, "properties", true);
        XmlUtils.getChildElement(properties, "maven.deploy.skip", true).setTextContent("true");
    }

    private void disableExchangePreDeploy(Document pomDoc) throws XPathExpressionException, RESTException {
        Element plugin = XPathUtils.evalXPathElement("//build/plugins/plugin[ artifactId/text() = 'exchange-mule-maven-plugin' and groupId/text() = 'org.mule.tools.maven']", pomDoc);
        if (plugin == null) {
            logger.info("adding exchange-mule-maven-plugin");
            final Element mvnProject = pomDoc.getDocumentElement();
            final Element build = XmlUtils.getChildElement(mvnProject, "build", true);
            final Element plugins = XmlUtils.getChildElement(build, "plugins", true);
            plugin = XmlUtils.createElement("plugin", plugins);
            XmlUtils.createElement("groupId", plugin).setTextContent("org.mule.tools.maven");
            XmlUtils.createElement("artifactId", plugin).setTextContent("exchange-mule-maven-plugin");
            XmlUtils.createElement("version", plugin).setTextContent("0.0.17");
        }
        logger.info("Setting exchange-mule-maven-plugin to skip");
        final Element config = XmlUtils.getChildElement(plugin, "configuration", true);
        XmlUtils.getChildElement(config, "skip", true).setTextContent("true");
    }

    @SuppressWarnings("unchecked")
    private String getLatestVersion(String projectId) throws RESTException {
        final Map<String, String> rel = (Map<String, String>) restClient.get("https://gitlab.com/api/v4/projects/" + projectId + "/releases", List.class).get(0);
        return rel.get("tag_name").substring(1);
    }
}
