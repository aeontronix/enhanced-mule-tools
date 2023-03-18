/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class EMTPropertiesTest {
    @Test
    public void testEMTProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("plane", "bird");
        props.put("hello.first", "world");
        props.put("hello.second", "world");
        props.put("hello.third", "world");
        props.put("emt.env.dev.hello.first", "blue");
        props.put("emt.env.qa.hello.first", "red");
        props.put("emt.env.dev.hello.third", "rover");
        props.put("emt.envtype.production.hello.second", "orange");
        props.put("emt.envtype.sandbox.hello.second", "green");
        props.put("emt.envtype.production.hello.third", "orange");
        props.put("emt.envtype.sandbox.hello.third", "green");
        props = new EMTProperties(props, "dev", "243243243", Environment.Type.SANDBOX).getProperties();
        System.out.println(props);
        HashMap<String, String> expected = new HashMap<>();
        expected.put("plane", "bird");
        expected.put("hello.first", "blue");
        expected.put("hello.second", "green");
        expected.put("hello.third", "rover");
        Assertions.assertEquals(expected, props);
    }

}
