/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.util.restclient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloudtek.util.UnexpectedException;

import java.io.IOException;
import java.io.InputStream;

public class RESTClientJsonParserJacksonImpl implements RESTClientJsonParser {
    private ObjectMapper objectMapper;

    public RESTClientJsonParserJacksonImpl() {
        this(new ObjectMapper());
    }

    public RESTClientJsonParserJacksonImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String toJson(Object entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new UnexpectedException(e);
        }
    }

    public <X> X parse(InputStream inputStream, Class<X> clazz) throws IOException, ResponseParsingException {
        try {
            return objectMapper.readValue(inputStream, clazz);
        } catch (JsonParseException| JsonMappingException e) {
            throw new ResponseParsingException(e);
        }
    }
}
