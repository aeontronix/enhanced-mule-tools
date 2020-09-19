/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.util.restclient;

import java.io.IOException;
import java.io.InputStream;

public interface RESTClientJsonParser {
    String toJson(Object entity);

    <X> X parse(InputStream inputStream, Class<X> clazz) throws IOException, ResponseParsingException;
}
