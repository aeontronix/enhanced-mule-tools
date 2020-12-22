/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.exchange;

import com.aeontronix.commons.TempFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public interface APISpecSource {
    Set<String> listAPISpecFiles() throws IOException;

    void writeAPISpecFile(String name, OutputStream os) throws IOException;
}
