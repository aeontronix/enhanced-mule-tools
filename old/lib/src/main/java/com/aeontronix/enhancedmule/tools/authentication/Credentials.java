/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.authentication;

import java.util.Map;

public interface Credentials {
    Map<String,String> toAuthRequestPayload();
}
