/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.UnexpectedException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.unpack.FileType;
import com.aeontronix.unpack.UnpackException;
import com.aeontronix.unpack.Unpacker;
import com.kloudtek.util.TempFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.zip.ZipFile;

import static com.kloudtek.util.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class ApplicationArchiveHelper {
    private static final Logger logger = getLogger(ApplicationArchiveHelper.class);

    @SuppressWarnings("unchecked")
    public static void uploadToMaven(ApplicationIdentifier appId, Organization org, ApplicationSource applicationSource,
                                     String newVersion, String buildNumber) throws IOException, UnpackException {
        final File appArchFile = applicationSource.getLocalFile();
        if (appId == null) {
            try (final ZipFile zipFile = new ZipFile(appArchFile)) {
                appId = getApplicationIdentifier(org, zipFile);
            }
        }
        if (newVersion == null && appId.getVersion().toLowerCase().endsWith("-snapshot")) {
            newVersion = appId.getVersion() + "-" + buildNumber;
        }
        if (!org.getId().equals(appId.getGroupId()) || newVersion != null) {
            try (final TempFile emteh = new TempFile("emteh")) {
                Unpacker unpacker = new Unpacker(appArchFile, FileType.ZIP, emteh, FileType.ZIP);
                unpacker.addTransformers(ApplicationArchiveVersionTransformer.getTransformers(appId, org.getId(), newVersion, buildNumber));
                unpacker.unpack();
                publishArchive(appId, org, emteh);
            }
        } else {
            publishArchive(appId, org, appArchFile);
        }
    }

    private static void publishArchive(ApplicationIdentifier appId, Organization org, File appArchFile) throws IOException {
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
        return new URLBuilder("https://maven.anypoint.mulesoft.com/api/v2/maven")
                .path(org.getId(), true).path(appId.getArtifactId(), true)
                .path(appId.getVersion(), true);
    }

}
