/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import org.fusesource.jansi.Ansi;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "profile",description = "Change or show active profile")
public class ActiveProfileCmd implements Callable<Integer> {
    private EMTCli cli;
    @Parameters(arity = "0..1",description = "profile to activate")
    private String profile;

    public ActiveProfileCmd(EMTCli cli) {
        this.cli = cli;
    }

    @Override
    public Integer call() throws Exception {
        if( profile != null ) {
            cli.setActiveProfile(profile);
        }
        System.out.println(Ansi.ansi().a("Active profile: "+cli.getActiveProfile()));
        return 0;
    }
}
