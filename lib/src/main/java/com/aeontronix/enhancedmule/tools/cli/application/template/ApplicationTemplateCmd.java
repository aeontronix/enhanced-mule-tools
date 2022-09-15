/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application.template;

import com.aeontronix.enhancedmule.tools.cli.application.ApplicationCmd;
import picocli.CommandLine;

@CommandLine.Command(name = "template", subcommands = {
        ApplicationTemplateCreateCmd.class,
        ApplicationTemplatePublishCmd.class
})
public class ApplicationTemplateCmd {
    @CommandLine.ParentCommand
    private ApplicationCmd parent;

    public ApplicationCmd getParent() {
        return parent;
    }
}
