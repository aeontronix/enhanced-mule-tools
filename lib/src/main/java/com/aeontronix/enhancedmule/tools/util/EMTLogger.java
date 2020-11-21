/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.util;

import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;

import java.awt.*;

import static org.fusesource.jansi.Ansi.Color.WHITE;

public class EMTLogger {
    private Logger logger;

    public EMTLogger(Logger logger) {
        this.logger = logger;
    }

    public void info(Product product, String message, Object... args ) {
        logger.info(Ansi.ansi().fgBrightBlue().a(product.toString()+" :: ").reset().a(message).toString(),args);
    }

    public static enum Product {
        EXCHANGE("Exchange"), API_MANAGER("API Manager"), RUNTIME_MANAGER("Runtime Manager");
        private String name;

        Product(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
