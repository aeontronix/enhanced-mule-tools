/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CredentialsUsernamePasswordImpl.class, name = "upw"),
        @JsonSubTypes.Type(value = CredentialsBearerTokenImpl.class, name = "bearer"),
        @JsonSubTypes.Type(value = CredentialsClientCredentialsImpl.class, name = "client")
})
public interface ConfigCredentials {
}
