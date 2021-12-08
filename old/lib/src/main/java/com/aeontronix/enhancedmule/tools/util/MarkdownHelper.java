/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.commons.StringUtils;

import java.io.IOException;
import java.io.Writer;

public class MarkdownHelper {
    public static void writeHeader(Writer w, int lvl, String heading) throws IOException {
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < lvl; i++) {
            tmp.append('#');
        }
        tmp.append(' ').append(heading).append("\n\n");
        w.write(tmp.toString());
    }

    public static void writeParagraph(Writer w, String text) throws IOException {
        if(StringUtils.isNotBlank(text)) {
            w.write(text);
            w.write("\n\n");
        }
    }
}
