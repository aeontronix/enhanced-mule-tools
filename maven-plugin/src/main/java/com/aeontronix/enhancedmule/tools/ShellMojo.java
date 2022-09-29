/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import com.aeontronix.enhancedmule.tools.cli.ShellCmd;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jline.reader.impl.DefaultParser;
import org.slf4j.Logger;
import picocli.CommandLine;

import static org.slf4j.LoggerFactory.getLogger;

@Mojo(name = "shell", requiresProject = false)
public class ShellMojo extends AbstractMojo {
    private static final Logger logger = getLogger(ShellMojo.class);
    @Parameter(name = "command", property = "cmd")
    private String command;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (StringUtils.isNotBlank(command)) {
            final EMTCli cli;
            try {
                cli = new EMTCli();
                final CommandLine commandLine = new CommandLine(cli);
                commandLine.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.ON));
                commandLine.setUsageHelpAutoWidth(true);
                commandLine.setCaseInsensitiveEnumValuesAllowed(true);
                commandLine.setPosixClusteredShortOptionsAllowed(false);
                logger.info("Executing command");
                final int ret = commandLine.execute(new DefaultParser().parse(command, 0).words().toArray(new String[0]));
                if (ret != 0) {
                    throw new MojoExecutionException("Command returned status code: " + ret);
                }
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        } else {
            try {
                new ShellCmd().call();
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }
}
