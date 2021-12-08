/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.enhancedmule.tools.client.EMTClientStatus;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "status")
public class StatusCmd implements Callable {
    private static final Logger logger = getLogger(StatusCmd.class);
    @ParentCommand
    private EMTCli cli;

    @Override
    public Object call() throws Exception {
        final EMTClientStatus status = cli.getClient().getStatus();
        if( status.isAuthenticated() ) {
            logger.info("Authenticated: Yes");
            logger.info("User: "+status.getUsername());
        } else {
            logger.info("Authenticated: No");
        }
        return null;
    }
}
