/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.config;

import com.aeontronix.enhancedmule.tools.cli.EMTCli;
import com.aeontronix.enhancedmule.tools.config.EMConfig;
import org.fusesource.jansi.Ansi;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "profile", description = "Change or show active profile")
public class ActiveProfileCmd implements Callable<Integer> {
    @ParentCommand
    private EMTCli cli;
    @Option(names = {"-c", "--create"}, description = "Create profile if required")
    boolean create;
    @Parameters(arity = "0..1", description = "profile to activate")
    private String profile;

    public ActiveProfileCmd() {
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
            config.getOrCreateProfile(profile);
            config.save();
        }
        System.out.println(Ansi.ansi().a("Active profile: " + config.getActive()));
        System.out.println("Available profiles: " + String.join(", ", config.getProfiles().keySet()));
        return 0;
    }
}
