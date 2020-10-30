/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.deploy;

import com.aeontronix.commons.UnexpectedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloudtek.util.FileUtils;
import com.kloudtek.util.StringUtils;
import com.kloudtek.util.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.io.File.separator;

public class ApplicationDeployerScriptBuilder {
    public static final String MVN_WRAPPER_PREFIX = ".mvn/wrapper/";
    public static final String MVNW = "mvnw";
    public static final String MVNW_CMD = "mvnw.cmd";
    public static final String DEFAULT_MVN_OPTIONS = "-B -ntp -e";
    private String version;
    private boolean mvnWrapper;
    private String groupId;
    private String artifactId;
    private String emtVersion;
    private String mvnOptions = DEFAULT_MVN_OPTIONS;
    private File mavenWrapperDir;

    public ApplicationDeployerScriptBuilder(@NotNull String groupId, @NotNull String artifactId, @NotNull String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        try {
            final InputStream pomProps = getClass().getResourceAsStream("/META-INF/maven/com.aeontronix.enhanced-mule/enhanced-mule-tools-lib/pom.properties");
            if( pomProps != null ) {
                final Properties emtRel = new Properties();
                emtRel.load(pomProps);
                this.emtVersion = emtRel.getProperty("version");
            }
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    public ApplicationDeployerScriptBuilder withMvnWrapper(boolean mvnWrapper) {
        this.mvnWrapper = mvnWrapper;
        return this;
    }

    public ApplicationDeployerScriptBuilder withMvnWrapperDir(File mavenWrapperDir) {
        this.mavenWrapperDir = mavenWrapperDir;
        mvnWrapper = new File(mavenWrapperDir, MVNW).exists() || new File(mavenWrapperDir, MVNW_CMD).exists();
        return this;
    }

    public ApplicationDeployerScriptBuilder withEMTVersion( String emtVersion ) {
        if( emtVersion != null ) {
            this.emtVersion = emtVersion;
        }
        return this;
    }

    public ApplicationDeployerScriptBuilder withMvnOptions( String mvnOptions ) {
        this.mvnOptions = mvnOptions;
        return this;
    }

    public void buildZipArchive(File zipFile ) throws IOException {
        if( zipFile.exists() ) {
            FileUtils.delete(zipFile);
        }
        try (final FileOutputStream fos = new FileOutputStream(zipFile);
             final ZipOutputStream zos = new ZipOutputStream(fos)) {
            if( mavenWrapperDir != null ) {
                copyFileToZip(zos, "", MVNW, mavenWrapperDir);
                copyFileToZip(zos, "", MVNW_CMD, mavenWrapperDir);
                final File mvnWrapperDir = new File(mavenWrapperDir + separator + ".mvn" + separator + "wrapper");
                copyFileToZip(zos, MVN_WRAPPER_PREFIX, "maven-wrapper.properties", mvnWrapperDir);
                copyFileToZip(zos, MVN_WRAPPER_PREFIX, "MavenWrapperDownloader.java", mvnWrapperDir);
            }
            writeFileToZip(zos, "deploy.sh", buildUnix());
            writeFileToZip(zos, "deploy.cmd", buildWin());
            writeFileToZip(zos, "deploy.json", buildJsonDescriptor());
            writeFileToZip(zos, "deploy.ps1", "start -wait -NoNewWindow '.\\deploy.cmd' -args \"$args\"");
        }
    }

    public String buildJsonDescriptor() {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(new ExchangeDeployDescriptor(groupId,artifactId,version));
        } catch (JsonProcessingException e) {
            throw new UnexpectedException(e);
        }
    }

    public String buildUnix() {
        try {
            String script = IOUtils.toString(getClass().getResourceAsStream("/deployer-unix.sh"));
            return replaceVars(script, "sh mvnw");
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    public String buildWin() {
        try {
            String script = IOUtils.toString(getClass().getResourceAsStream("/deployer-win.bat"));
            return replaceVars(script, "mvnw.cmd");
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    @NotNull
    private String replaceVars(String script, String wrapperScript) {
        script = script.replace("@MVN_CMD@", mvnWrapper ? wrapperScript : "mvn");
        script = script.replace("@GROUP_ID@", groupId);
        script = script.replace("@ARTIFACT_ID@", artifactId);
        script = script.replace("@VERSION@", version);
        script = script.replace("@MVN_OPTS@", mvnOptions);
        if (emtVersion == null) {
            throw new IllegalStateException("No EMT version found");
        }
        script = script.replace("@EMT_VERSION@", emtVersion);
        return script;
    }


    private void writeFileToZip(ZipOutputStream zipFile, String path, String content) throws IOException {
        final ZipEntry e = new ZipEntry(path);
        zipFile.putNextEntry(e);
        zipFile.write(StringUtils.utf8(content));
        zipFile.closeEntry();
    }

    private void copyFileToZip(ZipOutputStream zipFile, String prefix, String filename, File projectBasedir) throws IOException {
        final File file = new File(projectBasedir, filename);
        if (file.exists()) {
            final ZipEntry e = new ZipEntry(prefix + file.getName());
            zipFile.putNextEntry(e);
            try (FileInputStream fis = new FileInputStream(file)) {
                com.aeontronix.commons.io.IOUtils.copy(fis, zipFile);
            }
            zipFile.closeEntry();
        }
    }
}
