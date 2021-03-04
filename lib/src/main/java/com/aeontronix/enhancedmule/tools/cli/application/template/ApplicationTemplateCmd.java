/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application.template;

import com.aeontronix.enhancedmule.tools.cli.application.ApplicationCmd;
import com.aeontronix.enhancedmule.tools.cli.application.ApplicationCreateCmd;
import picocli.CommandLine;

@CommandLine.Command(name = "template", mixinStandardHelpOptions = true, subcommands = {
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
