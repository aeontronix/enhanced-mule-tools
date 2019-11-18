/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;

public interface AuthenticationProvider {
    String getBearerToken(HttpHelper httpHelper) throws HttpException;
}
