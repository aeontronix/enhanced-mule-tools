/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli;

import picocli.CommandLine.ParentCommand;

public abstract class AbstractCommand {
    @ParentCommand
    private AbstractCommand parent;

    public EMTCli getCli() {
        while (parent != null) {
            if (parent instanceof EMTCli) {
                return (EMTCli) parent;
            } else {
                parent = parent.parent;
            }
        }
        return null;
    }
}
