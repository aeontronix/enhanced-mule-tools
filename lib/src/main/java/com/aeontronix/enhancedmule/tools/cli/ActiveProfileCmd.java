/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.config.EMConfig;
import org.fusesource.jansi.Ansi;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "profile",description = "Change or show active profile")
public class ActiveProfileCmd implements Callable<Integer> {
    private EMTCli cli;
    @CommandLine.Option(names = {"?", "-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;
    @Parameters(arity = "0..1",description = "profile to activate")
    private String profile;

    public ActiveProfileCmd(EMTCli cli) {
        this.cli = cli;
    }

    @Override
    public Integer call() throws Exception {
        final EMConfig config = cli.getConfig();
        if( profile != null ) {
            config.checkProfileExists(profile);
            config.setActive(profile);
        }
        System.out.println(Ansi.ansi().a("Active profile: "+config.getActive()));
        return 0;
    }
}
