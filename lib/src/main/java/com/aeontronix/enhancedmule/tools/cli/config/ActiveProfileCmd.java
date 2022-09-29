/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.config.EMConfig;
import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import org.fusesource.jansi.Ansi;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "profile", description = "Change or show active profile")
public class ActiveProfileCmd implements Callable<Integer> {
    private EMTCli cli;
    @Option(names = {"-c", "--create"}, description = "Create profile if required")
    boolean create;
    @Parameters(arity = "0..1", description = "profile to activate")
    private String profile;

    public ActiveProfileCmd(EMTCli cli) {
        this.cli = cli;
    }

    @Override
    public Integer call() throws Exception {
        final EMConfig config = cli.getConfig();
        if( profile != null ) {
            if (create) {
                config.getOrCreateProfile(profile);
            } else {
                config.checkProfileExists(profile);
            }
            config.setActive(profile);
        }
        System.out.println(Ansi.ansi().a("Active profile: "+config.getActive()));
        return 0;
    }
}
