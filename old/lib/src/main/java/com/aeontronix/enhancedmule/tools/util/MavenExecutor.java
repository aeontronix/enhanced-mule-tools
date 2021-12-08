/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.commons.SystemUtils;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import org.apache.commons.exec.DefaultExecutor;
import org.jline.console.CmdDesc;
import org.jline.console.CommandRegistry;
import org.jline.reader.impl.completer.SystemCompleter;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

public class MavenExecutor implements CommandRegistry {
    private static final Logger logger = getLogger(MavenExecutor.class);
    public static final String MVN = "mvn";
    private final Set<String> cmdNames;
    private EMTCli cli;

    public MavenExecutor(EMTCli cli) {
        this.cli = cli;
        cmdNames = new HashSet<>();
        cmdNames.add(MVN);
    }

    @Override
    public Set<String> commandNames() {
        return cmdNames;
    }

    @Override
    public Map<String, String> commandAliases() {
        return Collections.emptyMap();
    }

    @Override
    public List<String> commandInfo(String command) {
        if (command.equalsIgnoreCase(MVN)) {
            return Collections.singletonList("Invokes maven in the working directory");
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean hasCommand(String command) {
        return MVN.equalsIgnoreCase(command);
    }

    @Override
    public SystemCompleter compileCompleters() {
        return new SystemCompleter();
    }

    @Override
    public CmdDesc commandDescription(List<String> args) {
        return null;
    }

    @Override
    public Object invoke(CommandSession session, String command, Object... args) throws Exception {
        if (MVN.equals(command)) {
            final ArrayList<String> argsList = new ArrayList<>();
            if (args != null) {
                for (Object arg : args) {
                    argsList.add(arg.toString());
                }
            }
            execute(cli.getWorkDir(),argsList);
        }
        return null;
    }

    public static int execute(File workDir, List<String> args) throws IOException {
        File mvnw = new File("mvnw");
        if (mvnw.exists()) {
            org.apache.commons.exec.CommandLine cmd;
            if( System.getProperty("os.name").toLowerCase().contains("win") ) {
                cmd = new org.apache.commons.exec.CommandLine("mvnw.cmd");
            } else {
                cmd = new org.apache.commons.exec.CommandLine("sh");
                cmd.addArgument("mvnw");
            }
            for (String arg : args) {
                cmd.addArgument(arg);
            }
            final DefaultExecutor defaultExecutor = new DefaultExecutor();
            defaultExecutor.setWorkingDirectory(workDir);
            defaultExecutor.setExitValues(null);
            return defaultExecutor.execute(cmd);
        } else {
            logger.error("maven wrapper script not found");
        }
        return -1;
    }
}
