/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli.application;

import com.aeontronix.enhancedmule.tools.application.ApplicationSourceEnhancer;
import com.aeontronix.enhancedmule.tools.cli.AbstractCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "enhance", aliases = "enh", description = "Add enhanced mule support to mule application")
public class EnhanceApplicationCmd extends AbstractCommand implements Callable<Integer> {
    @Option(names = "-d", description = "Project directory", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private File projectDir = new File(".");

    @Override
    public Integer call() throws Exception {
        new ApplicationSourceEnhancer(getCli().createEMClient().getRestClient(), projectDir).execute();
        return 0;
    }
}
