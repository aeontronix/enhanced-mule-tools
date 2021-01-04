/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.tools.cli.application.ApplicationCmd;
import com.aeontronix.enhancedmule.tools.util.VersionHelper;
import org.jline.reader.LineReader;
import picocli.CommandLine.Command;

import java.io.File;

@Command(name = "emt", mixinStandardHelpOptions = true, versionProvider = VersionHelper.class, subcommands = {ApplicationCmd.class})
public class EMTCli {
    private File workDir = new File(".");
    private LineReader reader;

    public boolean isShell() {
        return reader != null;
    }

    public LineReader getReader() {
        return reader;
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public void setReader(LineReader reader) {
        this.reader = reader;
    }
}
