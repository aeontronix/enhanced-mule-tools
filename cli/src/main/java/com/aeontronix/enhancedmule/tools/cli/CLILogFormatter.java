/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli;

import org.fusesource.jansi.Ansi;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class CLILogFormatter extends Formatter {
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    private final Date date = new Date();
    private boolean showLevel;
    private boolean showTimestamp;
    private String separator = " ";

    @Override
    public String format(final LogRecord record) {
        final StringBuilder txt = new StringBuilder();
        if (showLevel) {
            txt.append(record.getLevel().getLocalizedName()).append(separator);
        }
        if (showTimestamp) {
            synchronized (dateFormat) {
                date.setTime(record.getMillis());
                txt.append(dateFormat.format(date)).append(separator);
            }
        }
        String errorMessage = formatMessage(record);
        if (record.getLevel().intValue() > Level.INFO.intValue()) {
            errorMessage = Ansi.ansi().fgBrightRed().a("ERROR: " + errorMessage).reset().toString();
        }
        txt.append(errorMessage).append('\n');
        return txt.toString();
    }

    public void setDebug(boolean debug) {
        showLevel = debug;
        showTimestamp = debug;
    }
}
