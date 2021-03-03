/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application.template;

import com.aeontronix.enhancedmule.tools.cli.application.ApplicationCreateCmd;
import picocli.CommandLine;

@CommandLine.Command(name = "template", mixinStandardHelpOptions = true, subcommands = {
        ApplicationTemplateCreateCmd.class
})
public class ApplicationTemplateCmd {
}
