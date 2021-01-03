/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.tools.cli.application.ApplicationCmd;
import com.aeontronix.enhancedmule.tools.util.VersionHelper;
import org.jline.reader.LineReader;
import picocli.CommandLine.Command;

@Command(name = "emt", mixinStandardHelpOptions = true, versionProvider = VersionHelper.class, subcommands = {ApplicationCmd.class})
public class EMTCli {
    private LineReader reader;

    public void setReader(LineReader reader) {
        this.reader = reader;
    }
}
