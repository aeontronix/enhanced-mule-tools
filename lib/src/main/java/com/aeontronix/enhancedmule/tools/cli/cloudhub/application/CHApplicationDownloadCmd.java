/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli.cloudhub.application;

import com.aeontronix.anypointsdk.AnypointClient;
import com.aeontronix.anypointsdk.cloudhub.CHApplication;
import com.aeontronix.anypointsdk.cloudhub.CloudhubClient;
import com.aeontronix.enhancedmule.tools.cli.AbstractEnvCommand;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Optional;
import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;
import static picocli.CommandLine.Option;

@Command(name = "download", description = "Download a cloudhub application")
public class CHApplicationDownloadCmd extends AbstractEnvCommand implements Callable<Integer> {
    Logger logger = getLogger(CHApplicationDownloadCmd.class);
    @Parameters(arity = "1", description = "Application id")
    private String appDomain;
    @Option(names = {"-f", "--file"}, description = "File to save the application as (if directory is specified, it will be saved in that directory with the original application filename)",
            defaultValue = ".")
    private File file;

    @Override
    public Integer call() throws Exception {
        AnypointClient anypointClient = getCli().getAnypointClient();
        CloudhubClient cloudhubClient = anypointClient.getCloudhubClient();
        Optional<CHApplication> chApplication = cloudhubClient.describeApplication(getOrgId(), getEnvId(), appDomain);
        String filename = chApplication.orElseThrow(() -> new IllegalStateException("Application not found")).getData().getFileName();
        if (file.isDirectory()) {
            file = new File(file, filename);
        }
        logger.info("Downloading application file to : "+file.getPath());
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            cloudhubClient.downloadApplicationFile(getOrgId(), getEnvId(), appDomain, filename, fileOutputStream);
        }
        logger.info("Downloading complete");
        return 0;
    }
}
