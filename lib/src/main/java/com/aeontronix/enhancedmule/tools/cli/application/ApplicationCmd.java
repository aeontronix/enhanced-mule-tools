/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application;

import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import com.aeontronix.enhancedmule.tools.cli.application.template.ApplicationTemplateCmd;
import picocli.CommandLine;
import picocli.CommandLine.ParentCommand;

@CommandLine.Command(name = "application", aliases = "app", mixinStandardHelpOptions = true,subcommands = {
        ApplicationCreateCmd.class,
        ApplicationTemplateCmd.class
})
public class ApplicationCmd {
    @ParentCommand
    private EMTCli cli;

    public EMTCli getCli() {
        return cli;
    }
}
