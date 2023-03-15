/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.cli.properties;

import com.aeontronix.enhancedmule.tools.cli.AbstractCommand;
import com.aeontronix.enhancedmule.tools.util.JacksonHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "generate-descriptor", aliases = {"gendesc"}, description = "Generate properties.xml descriptor from config file")
public class PropertiesGenerateDescriptorCmd extends AbstractCommand implements Callable<Integer> {
    private static final Logger logger = getLogger(PropertiesGenerateDescriptorCmd.class);
    @Parameters(index = "0")
    private File file;
    @Parameters(defaultValue = "properties.yaml", index = "1")
    private File output;
    @CommandLine.Option(names = {"-t", "--tokenizer-file"}, description = "If specified also generate a tokenizer pass-through property file")
    private File tokenizerFile;

    @Override
    public Integer call() throws Exception {
        JavaPropsMapper javaPropsMapper = new JavaPropsMapper();
        Map<String, String> properties = javaPropsMapper.writeValueAsMap(JacksonHelper.readTree(file));
        Map<String, HashMap<String, String>> descriptor = properties.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> new HashMap<>()));
        ObjectMapper yamlMapper = new ObjectMapper(YAMLFactory.builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER).build());
        yamlMapper.writeValue(output, descriptor);
        logger.info("Generated file " + output.getPath());
        if (tokenizerFile != null) {
            Map<String, String> map = properties.entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey(), e -> "__" + e.getKey() + "__"));
            javaPropsMapper.writeValue(tokenizerFile, map);
        }
        return null;
    }
}
