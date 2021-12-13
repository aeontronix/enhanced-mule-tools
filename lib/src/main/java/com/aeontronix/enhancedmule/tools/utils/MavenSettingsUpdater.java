/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.utils;

import com.aeontronix.commons.xml.XPathUtils;
import com.aeontronix.commons.xml.XmlUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.aeontronix.commons.xml.XmlUtils.getChildElement;
import static java.io.File.separator;
import static org.slf4j.LoggerFactory.getLogger;

public class MavenSettingsUpdater {
    private static final Logger logger = getLogger(MavenSettingsUpdater.class);
    private final Document settingsDoc;
    private final Element settings;
    private File mvnSettingsFile;

    public MavenSettingsUpdater(File mvnSettingsFile) throws IOException, SAXException {
        if( mvnSettingsFile == null ) {
            mvnSettingsFile = new File(System.getProperty("user.home") + separator + ".m2" + separator + "settings.xml");
        }
        this.mvnSettingsFile = mvnSettingsFile;
        if (mvnSettingsFile.exists()) {
            settingsDoc = XmlUtils.parse(mvnSettingsFile, true);
            settings = settingsDoc.getDocumentElement();
            logger.debug("Loaded " + mvnSettingsFile.getPath());
        } else {
            settingsDoc = XmlUtils.createDocument(true);
            settings = settingsDoc.createElementNS("http://maven.apache.org/SETTINGS/1.0.0", "settings");
            settingsDoc.appendChild(settings);
            settings.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            settings.setAttribute("xsi:schemaLocation", "http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd");
            logger.debug("Created " + mvnSettingsFile.getPath());
        }
    }

    public void addBearerToken(String mavenSettingsId, String bearerToken) throws XPathExpressionException {
        // Add profile and property
        if (mavenSettingsId == null) {
            mavenSettingsId = "anypoint";
        }
        final Element profiles = getChildElement(settings, "profiles", true);
        Element profile = XPathUtils.evalXPathElement("*[local-name()='profile']/*[local-name()='id'][text() = '" + mavenSettingsId + "']/..", profiles);
        if (profile == null) {
            profile = XmlUtils.createElement("profile", profiles);
            XmlUtils.createElement("activeByDefault", XmlUtils.createElement("activation", profile))
                    .setTextContent("true");
            XmlUtils.createElement("id", profile).setTextContent(mavenSettingsId);
        }
        final Element properties = getChildElement(profile, "properties", true);
        getChildElement(properties, mavenSettingsId+".token", true).setTextContent(bearerToken);
        // add servers
        final Element servers = getChildElement(settings, "servers", true);
        Element server = XPathUtils.evalXPathElement("*[local-name()='server']/*[local-name()='id'][text() = '" + mavenSettingsId + "']/..", servers);
        if (server == null) {
            server = XmlUtils.createElement("server", servers);
            XmlUtils.createElement("id", server).setTextContent(mavenSettingsId);
        }
        getChildElement(server, "username", true).setTextContent("~~~Token~~~");
        getChildElement(server, "password", true).setTextContent(bearerToken);
    }

    public void addPluginGroup() throws XPathExpressionException {
        if (XPathUtils.evalXPathElement("*[local-name()='pluginGroups']/*[local-name()='pluginGroup'][text() = 'com.aeontronix.enhanced-mule']", settings) == null) {
            getChildElement(getChildElement(settings, "pluginGroups", true), "pluginGroup", true)
                    .setTextContent("com.aeontronix.enhanced-mule");
        }
    }

    public void saveSettings() throws IOException {
        try (final FileWriter w = new FileWriter(mvnSettingsFile)) {
            XmlUtils.serialize(settingsDoc, w, true, true);
        }
    }
}
