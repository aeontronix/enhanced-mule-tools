/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.file.FileUtils;
import com.aeontronix.commons.file.TempFile;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetFile;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.cli.application.template.ApplicationTemplatePublishCmd;
import com.aeontronix.enhancedmule.tools.util.VersionHelper;
import com.aeontronix.genesis.Template;
import com.aeontronix.genesis.TemplateExecutionException;
import com.aeontronix.genesis.TemplateExecutor;
import com.aeontronix.genesis.ZipResourceLoader;
import com.aeontronix.restclient.RESTResponse;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "create")
public class ApplicationCreateCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ApplicationCreateCmd.class);
    public static final String EMT_VERSION_KEY = "emtVersion";
    public static final String TEMPLATE_FILE = "genesis-template.json";
    @Option(names = "-D", description = "Template variables")
    private HashMap<String,String> vars = new HashMap<>();
    @Option(names = "-o", description = "Organization name or id")
    private String organization;
    @Option(names = "-i", description = "Template asset id")
    private String templateAssetId = ApplicationTemplatePublishCmd.EMT_TEMPLATE_ASSET_ID;
    @CommandLine.Parameters(description = "Project directory")
    private File dir;
    @CommandLine.ParentCommand
    private ApplicationCmd applicationCmd;

    public ApplicationCreateCmd() {
    }

    public ApplicationCmd getApplicationCmd() {
        return applicationCmd;
    }

    @Override
    public Integer call() throws Exception {
        if( ! vars.containsKey(EMT_VERSION_KEY) ) {
            vars.put(EMT_VERSION_KEY,VersionHelper.EMT_VERSION);
        }
        final Organization org = getApplicationCmd().getCli().findOrganization(organization);
        final ExchangeAsset exchangeAsset;
        Template template = null;
        try {
            exchangeAsset = org.findExchangeAsset(org.getId(), templateAssetId);
            final Optional<AssetFile> file = exchangeAsset.getFiles().stream()
                    .filter(f -> "custom".equalsIgnoreCase(f.getClassifier())).findFirst();
            final AssetFile assetFile = file.orElseThrow(() -> new RuntimeException("Asset is a not an EMT template"));
            try (TempFile tempFile = new TempFile("emttemplate")) {
                try (final RESTResponse response = org.getClient().getRestClient().get(URI.create(assetFile.getExternalLink()))
                        .execute()) {
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        IOUtils.copy(response.getContentStream(), fos);
                    }
                }
                template = Template.loadTemplate(new ZipResourceLoader(tempFile));
                template.setResourcePath("/");
                execute(template);
            }
        } catch (NotFoundException e) {
            logger.warn("Exchange template not found, using built-in");
            template = Template.createFromClasspath("/template", TEMPLATE_FILE);
            execute(template);
        }
        return 0;
    }

    private void execute(Template template) throws TemplateExecutionException {
        final TemplateExecutor templateExecutor = new TemplateExecutor(template, dir);
        templateExecutor.setVariables(vars);
        templateExecutor.execute();
    }

    public static void unzip(File zipFile, File destDir) throws IOException {
        logger.info("Unzipping archive");
        if (!destDir.exists()) {
            FileUtils.mkdirs(destDir);
        }
        try (FileInputStream fis = new FileInputStream(zipFile)) {
            ZipInputStream zis = new ZipInputStream(fis);
            for (ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()) {
                String fileName = ze.getName().replaceFirst(".*?/", "").replace("/", File.separator);
                boolean valid = StringUtils.isNotBlank(fileName);
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
                }
            }
            zis.closeEntry();
            zis.close();
        }
    }
}
