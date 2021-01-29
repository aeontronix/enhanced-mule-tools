/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application;

import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import com.aeontronix.enhancedmule.tools.template.EMTProjectTemplate;
import com.aeontronix.enhancedmule.tools.util.VersionHelper;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;
import static picocli.CommandLine.Help.Visibility.ALWAYS;

@Command(name = "deploy", aliases = "de", mixinStandardHelpOptions = true)
public class ApplicationDeployCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ApplicationDeployCmd.class);
    @CommandLine.ParentCommand
    private ApplicationCmd applicationCmd;

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
