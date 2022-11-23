/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application.template;

import com.aeontronix.commons.file.FileUtils;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "create")
public class ApplicationTemplateCreateCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ApplicationTemplateCreateCmd.class);
    @Parameters(description = "Artifact id")
    private String artifactId;

    @Override
    public Integer call() throws Exception {
        File directory = new File(artifactId);
        if (!directory.exists()) {
            FileUtils.mkdirs(directory);
        }
        final ObjectMapper mapper = JsonHelper.createMapper();
        final URL resource = getClass().getResource("/template/genesis-template.json");
        final JsonNode json = mapper.readTree(resource);
        List<String> files = new ArrayList<>();
        files.add("genesis-template.json");
        for (JsonNode file : json.get("files")) {
            final JsonNode res = file.get("resource");
            final String filename;
            if( JsonHelper.isNotNull(res) ) {
                filename = res.textValue();
            } else {
                filename = file.get("path").textValue();
            }
            files.add(filename);
        }
        for (String file : files) {
            try(InputStream is = getClass().getResourceAsStream("/template/"+ file);
                FileOutputStream fw = new FileOutputStream(new File(directory,file))) {
                IOUtils.copy(is,fw);
            }
        }
        System.out.println("Application template created: "+directory.getAbsolutePath());
        return 0;
    }

    private void writeFile(File directory, String name) {

    }
}
