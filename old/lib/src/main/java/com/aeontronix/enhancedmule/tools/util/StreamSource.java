/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.util;

import java.io.IOException;
import java.io.InputStream;

public interface StreamSource {
    String getFileName();

    InputStream createInputStream() throws IOException;
}
