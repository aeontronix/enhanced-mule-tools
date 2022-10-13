/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.exception.UnexpectedException;
import com.aeontronix.commons.file.TempFile;
import com.aeontronix.commons.xml.XmlUtils;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationArchiveVersionTransformer;
import com.aeontronix.enhancedmule.tools.anypoint.application.ApplicationIdentifier;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.unpack.FileType;
import com.aeontronix.unpack.UnpackException;
import com.aeontronix.unpack.Unpacker;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipFile;

import static com.aeontronix.commons.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class MavenHelper {
    private static final Logger logger = getLogger(MavenHelper.class);
    public static final String SETTINGS_NS = "http://maven.apache.org/SETTINGS/1.1.0";

    @SuppressWarnings("unchecked")
    public static ApplicationIdentifier uploadToMaven(ApplicationIdentifier appId, Organization org, ApplicationSource applicationSource,
                                                      String newVersion, String buildNumber) throws IOException, UnpackException {
        if (buildNumber == null) {
            buildNumber = generateTimestampString();
        }
        final File appArchFile = applicationSource.getLocalFile();
        if (appId == null) {
            try (final ZipFile zipFile = new ZipFile(appArchFile)) {
                appId = getApplicationIdentifier(org, zipFile);
            }
        }
        final ApplicationIdentifier newAppId;
        if (newVersion == null && appId.getVersion().toLowerCase().endsWith("-snapshot")) {
            newVersion = appId.getVersion() + "-" + buildNumber;
            newAppId = new ApplicationIdentifier(org.getId(), appId.getArtifactId(), newVersion);
            logger.info(Ansi.ansi().fgBrightYellow().a("Snapshot version: ").reset().a(newVersion).toString());
        } else {
            newAppId = new ApplicationIdentifier(org.getId(), appId.getArtifactId(), appId.getVersion());
        }
        if (!org.getId().equals(appId.getGroupId()) || newVersion != null) {
            try (final TempFile emteh = new TempFile("emteh")) {
                Unpacker unpacker = new Unpacker(appArchFile, FileType.ZIP, emteh, FileType.ZIP);
                unpacker.addTransformers(ApplicationArchiveVersionTransformer.getTransformers(appId, org.getId(), newVersion, buildNumber));
                unpacker.unpack();
                publishArchive(newAppId, org, emteh);
            }
        } else {
            publishArchive(newAppId, org, appArchFile);
        }
        return new ApplicationIdentifier(org.getId(), appId.getArtifactId(), newVersion != null ? newVersion : appId.getVersion());
    }

    public static void publishArchive(ApplicationIdentifier appId, Organization org, File appArchFile) throws IOException {
        try (final ZipFile zipFile = new ZipFile(appArchFile)) {
            publishFile(org, appId, zipFile, pomPath(appId, org.getId()), ".pom");
            logger.debug("Uploaded POM");
            publishFile(org, appId, zipFile, "anypoint.json", "-anypoint-descriptor.json");
            logger.debug("Uploaded anypoint.json");
        }
        try (final FileInputStream is = new FileInputStream(appArchFile)) {
            org.getClient().getHttpHelper().httpPutBasicAuth(createMavenUrl(org, appId)
                    .path(appId.getArtifactId() + "-" + appId.getVersion() + "-mule-application.jar").toString(), is, null);
            logger.debug("Uploaded Application");
        }
    }

    @NotNull
    public static String pomPath(ApplicationIdentifier appId, String orgId) {
        return mavenMetaPath(appId, orgId, "pom.xml");
    }

    @NotNull
    public static String mavenMetaPath(ApplicationIdentifier appId, String orgId, String file) {
        return "META-INF/maven/" + orgId + "/" + appId.getArtifactId() + "/" + file;
    }

    @SuppressWarnings("unchecked")
    private static ApplicationIdentifier getApplicationIdentifier(Organization org, ZipFile zipFile) throws IOException {
        final Map<String, Object> classloaderModel = org.getClient().getJsonHelper().getJsonMapper().readValue(zipFile.getInputStream(zipFile.getEntry("META-INF/mule-artifact/classloader-model.json")), Map.class);
        final Map<String, String> artifactCoordinates = (Map<String, String>) classloaderModel.get("artifactCoordinates");
        if (artifactCoordinates == null) {
            throw new UnexpectedException("Invalid classloader-model.json: artifactCoordinates missing");
        }
        final String groupId = artifactCoordinates.get("groupId");
        final String artifactId = artifactCoordinates.get("artifactId");
        final String version = artifactCoordinates.get("version");
        if (isBlank(groupId) || isBlank(artifactId) || isBlank(version)) {
            throw new UnexpectedException("Invalid classloader-model.json: groupId or artifactId missing");
        }
        return new ApplicationIdentifier(groupId, artifactId, version);
    }

    private static void publishFile(Organization org, ApplicationIdentifier appId, ZipFile zipFile, String path, String ext) throws IOException {
        try (final InputStream pomData = zipFile.getInputStream(zipFile.getEntry(path))) {
            org.getClient().getHttpHelper().httpPutBasicAuth(createMavenUrl(org, appId).path(appId.getArtifactId() + "-" + appId.getVersion() + ext).toString(), pomData, null);
        }
    }

    private static URLBuilder createMavenUrl(Organization org, ApplicationIdentifier appId) {
        return new URLBuilder("https://maven.anypoint.mulesoft.com/api/v2/organizations/" + org.getId() + "/maven")
                .path(org.getId(), true).path(appId.getArtifactId(), true)
                .path(appId.getVersion(), true);
    }

    @NotNull
    public static String generateTimestampString() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS").format(LocalDateTime.now());
    }

    public static void updateMavenSettings(@NotNull File mavenSettingsFile, String activeProfileId, String bearerToken) throws SAXException, IOException {
        String serverId;
        if (activeProfileId.equals("default")) {
            serverId = "anypoint-exchange";
        } else {
            serverId = "anypoint-exchange-" + activeProfileId;
        }
        final Document settingsDoc;
        final Element root;
        if (mavenSettingsFile.exists()) {
            settingsDoc = XmlUtils.parse(mavenSettingsFile, true);
            root = XmlUtils.getChildElement(settingsDoc, "settings", true);
        } else {
            settingsDoc = XmlUtils.createDocument(true);
            root = settingsDoc.createElementNS(SETTINGS_NS, "settings");
            root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xs:schemaLocation", SETTINGS_NS + " http://maven.apache.org/xsd/settings-1.1.0.xsd");
        }
        final Element servers = XmlUtils.getChildElement(root, "servers", true);
        final Optional<Element> serverOpt = XmlUtils.getChildNodes(servers, Element.class).stream().filter(n -> {
            final Element id = XmlUtils.getChildElement(n, "id", false);
            return id != null && id.getTextContent().trim().equals(serverId);
        }).findFirst();
        final Element server;
        if (!serverOpt.isPresent()) {
            server = XmlUtils.createElement("server", servers);
            XmlUtils.createElement("id", server).setTextContent(serverId);
        } else {
            server = serverOpt.get();
        }
        XmlUtils.getChildElement(server, "username", true).setTextContent("~~~Token~~~");
        XmlUtils.getChildElement(server, "password", true).setTextContent(bearerToken);
        try (FileOutputStream stream = new FileOutputStream(mavenSettingsFile)) {
            XmlUtils.serialize(settingsDoc, stream, false, true);
        }
    }
}
