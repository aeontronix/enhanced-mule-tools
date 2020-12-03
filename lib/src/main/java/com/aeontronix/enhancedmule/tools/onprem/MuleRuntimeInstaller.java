/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.onprem;

import com.aeontronix.commons.FileUtils;
import com.aeontronix.commons.ProcessExecutionFailedException;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.io.IOUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.slf4j.LoggerFactory.getLogger;

public class MuleRuntimeInstaller {
    public static final String INSTANCE_NAME = "instance-name.txt";
    public static final String INSTANCE_VERSION = "instance-version.txt";
    private static final Logger logger = getLogger(MuleRuntimeInstaller.class);
    private final String name;
    private final boolean windows;
    private File basedir;
    private File runtimeDir;
    private File runtimeArchive;
    private File archiveDir;
    private File target;
    private String registrationToken;
    private boolean upgrade;
    private File upgradeDir;
    private boolean force;
    private String version;
    private File licenseFile;

    public MuleRuntimeInstaller(String name, File runtimeArchive, File basedir, File runtimeDir, String registrationToken) {
        this.name = name;
        this.runtimeArchive = runtimeArchive;
        this.basedir = basedir;
        this.runtimeDir = runtimeDir;
        this.registrationToken = registrationToken;
        windows = System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static void copy(File from, File to) throws IOException {
        if (from.isDirectory()) {
            if (to.exists()) {
                if (!to.isDirectory()) {
                    throw new IOException(to.getAbsolutePath() + " already exists and is file rather than a directory");
                }
            } else {
                FileUtils.mkdirs(to);
            }
            final File[] children = from.listFiles();
            if (children != null) {
                for (File child : children) {
                    copy(child, new File(to, child.getName()));
                }
            }
        } else {
            FileUtils.copy(from, to);
        }
    }

    public void install() throws MuleRuntimeInstallationException {
        try {
            if (basedir == null) {
                basedir = new File(windows ? "C:\\mule" : "/opt/mule");
            }
            if (name == null) {
                throw new MuleRuntimeInstallationException("Name not specified nor couldn't be automatically generated");
            }
            if (runtimeDir == null) {
                runtimeDir = new File(basedir, name);
            }
            if( archiveDir == null ) {
                archiveDir = new File(basedir,"archive");
            }
            downloadArchive();
            upgrade();
            unzip(runtimeArchive, runtimeDir);
            configure();
        } catch (IOException | ProcessExecutionFailedException e) {
            throw new MuleRuntimeInstallationException(e);
        }
    }

    private void upgrade() throws IOException, ProcessExecutionFailedException, MuleRuntimeInstallationException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        upgradeDir = new File(runtimeDir.getPath() + ".old"+formatter.format(LocalDateTime.now()));
        if (upgrade) {
            runMuleCmd("mule", "stop");
            runMuleCmd("mule", "remove");
            logger.info("Moving {} to {}", runtimeDir.getPath(), upgradeDir.getPath());
            if (upgradeDir.exists()) {
                FileUtils.delete(upgradeDir);
            }
            Files.move(runtimeDir.toPath(), upgradeDir.toPath());
        } else {
            if (runtimeDir.exists()) {
                if (force) {
                    FileUtils.delete(runtimeDir);
                } else {
                    throw new MuleRuntimeInstallationException("Runtime already exists, use force install to delete prior to installation");
                }
            }
        }
    }

    private void downloadArchive() throws IOException, MuleRuntimeInstallationException {
        if (runtimeArchive == null) {
            if(version == null) {
                throw new MuleRuntimeInstallationException("either archive or version must be set");
            }
            String filename = "mule-ee-distribution-standalone-" + version + ".zip";
            if (!archiveDir.exists()) {
                FileUtils.mkdirs(archiveDir);
            }
            runtimeArchive = new File(archiveDir, filename);
            if( ! runtimeArchive.exists() ) {
                logger.info("Downloading runtime version " + version + " to " + runtimeArchive.getPath());
                URLConnection urlConnection = new URL("https://s3.amazonaws.com/new-mule-artifacts/" + filename).openConnection();
                try (final InputStream is = urlConnection.getInputStream();
                     FileOutputStream fos = new FileOutputStream(runtimeArchive)) {
                    IOUtils.copy(is, fos);
                }
                logger.info("Archive download complete");
            }
        }
    }

    private void configure() throws IOException, MuleRuntimeInstallationException, ProcessExecutionFailedException {
        logger.info("Configuring runtime");
        FileUtils.write(new File(runtimeDir, INSTANCE_NAME), name);
        FileUtils.write(new File(runtimeDir, INSTANCE_VERSION), version);
        String winFullName = "Mule " + name;
        modifyRuntimeFile("conf/wrapper.conf",
                txt -> txt.replaceFirst("wrapper\\.ntservice\\.name=.*", "wrapper.ntservice.name=" + winFullName.replaceAll("\\s", ""))
                        .replaceFirst("wrapper\\.ntservice\\.displayname=.*", "wrapper.ntservice.displayname=" + winFullName));
        if (registrationToken != null) {
            if (upgrade) {
                throw new MuleRuntimeInstallationException("Upgrade mustn't be used in conjunction with registrationToken");
            }
            runMuleCmd("amc_setup", "-H", registrationToken, name);
        }
        if (upgrade) {
            upgradeCopy("conf/mule-agent.yml", false);
            upgradeCopy("domains", false);
            upgradeCopy("conf/mule-agent.jks", false);
            upgradeCopy("conf/anypoint-truststore.jks", false);
            upgradeCopy("conf/truststore.jks", true);
            upgradeCopy("server-plugins/mule-agent-plugin", false);
            runMuleCmd("amc_setup", "-U");
        }
        final File archivedLicense = new File(archiveDir, "license.lic");
        if (licenseFile == null && archivedLicense.exists()) {
            licenseFile = archivedLicense;
        }
        if (licenseFile != null) {
            if (!licenseFile.exists()) {
                throw new MuleRuntimeInstallationException("License file not found:" + licenseFile.getPath());
            }
            if( ! archivedLicense.getParentFile().exists()) {
                FileUtils.mkdirs(archivedLicense.getParentFile());
            }
            FileUtils.copy(licenseFile, archivedLicense);
            FileUtils.copy(licenseFile, new File(runtimeDir + File.separator + "bin" + File.separator + "license.lic"));
            runMuleCmd("mule", "-installLicense license.lic");
        }
        if (windows) {
            runMuleCmd("mule", "install");
            runCmd(new File("."), "net start \"" + winFullName + "\"");
        }
    }

    private void runMuleCmd(String command, String... params) throws IOException, ProcessExecutionFailedException {
        StringBuilder buf = new StringBuilder();
        if (windows) {
            final File cmd = new File(runtimeDir + File.separator + "bin" + File.separator + command + ".bat");
            if (!cmd.exists()) {
                throw new IOException("file not found: " + cmd.getPath());
            }
            buf.append(cmd.getAbsolutePath());
        } else {
            buf.append("sh ").append(command);
        }
        buf.append(" ").append(String.join(" ", params));
        File workDir = new File(runtimeDir, "bin");
        runCmd(workDir, buf.toString());
    }

    private void runCmd(File workDir, String cmdLineStr) throws IOException {
        CommandLine cmdLine = CommandLine.parse(cmdLineStr);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(workDir);
        executor.setExitValue(0);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(new PumpStreamHandler(System.out));
        final int execute = executor.execute(cmdLine);
    }

    private void upgradeCopy(String path, boolean optional) throws MuleRuntimeInstallationException, IOException {
        final String npath = path.replace("/", File.separator);
        final File old = new File(upgradeDir.getPath() + File.separator + npath);
        if (!old.exists()) {
            if (optional) {
                return;
            } else {
                throw new MuleRuntimeInstallationException("File not found: " + old.getPath());
            }
        }
        final File newFile = new File(runtimeDir + File.separator + npath);
        copy(old, newFile);
    }

    public void modifyRuntimeFile(String path, Function<String, String> function) throws IOException {
        final File runtimeFile = new File(runtimeDir.getPath() + File.separator + path.replace("/", File.separator));
        final String content = FileUtils.toString(runtimeFile);
        FileUtils.write(runtimeFile, function.apply(content));
    }

    public String getName() {
        return name;
    }

    public File getRuntimeDir() {
        return runtimeDir;
    }

    public boolean isWindows() {
        return windows;
    }

    public File getRuntimeArchive() {
        return runtimeArchive;
    }

    public void setRuntimeArchive(File runtimeArchive) {
        this.runtimeArchive = runtimeArchive;
    }

    public File getTarget() {
        return target;
    }

    public void setTarget(File target) {
        this.target = target;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isUpgrade() {
        return upgrade;
    }

    public void setUpgrade(boolean upgrade) {
        this.upgrade = upgrade;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public File getLicenseFile() {
        return licenseFile;
    }

    public void setLicenseFile(File licenseFile) {
        this.licenseFile = licenseFile;
    }

    public File getArchiveDir() {
        return archiveDir;
    }

    public void setArchiveDir(File archiveDir) {
        this.archiveDir = archiveDir;
    }

    public void unzip(File zipFile, File destDir) throws IOException {
        logger.info("Unzipping archive");
        if (!destDir.exists()) {
            FileUtils.mkdirs(destDir);
        }
        try (FileInputStream fis = new FileInputStream(zipFile)) {
            ZipInputStream zis = new ZipInputStream(fis);
            for (ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()) {
                String fileName = ze.getName().replaceFirst(".*?/", "").replace("/", File.separator);
                boolean valid = StringUtils.isNotBlank(fileName);
                final boolean binFile = fileName.length() > 4 && fileName.startsWith("bin/");
                if (binFile && !windows && fileName.endsWith(".bat")) {
                    valid = false;
                }
                if (valid) {
                    File newFile = new File(destDir + File.separator + fileName);
                    if (ze.isDirectory()) {
                        if (!newFile.exists()) {
                            FileUtils.mkdirs(newFile);
                        }
                    } else {
                        final File parent = newFile.getParentFile();
                        if (!parent.exists()) {
                            FileUtils.mkdirs(parent);
                        }
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            IOUtils.copy(zis, fos);
                        }
                    }
                    zis.closeEntry();
                    if (!windows && !fileName.contains(".")) {
                        newFile.setExecutable(true);
                    }
                }
            }
            zis.closeEntry();
            zis.close();
        }
    }
}
