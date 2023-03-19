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
        props.put("emt.plane", "bird");
        props.put("emt.hello.first", "world");
        props.put("emt.hello.Second", "world");
        props.put("emt.hello.third", "world");
        props.put("emt.env.dev.hello.first", "blue");
        props.put("emt.env.qa.hello.first", "red");
        props.put("emt.env.dev.hello.third", "rover");
        props.put("emt.envtype.production.hello.Second", "orange");
        props.put("emt.envtype.sandbox.hello.Second", "green");
        props.put("emt.envtype.production.hello.third", "orange");
        props.put("emt.envtype.sandbox.hello.third", "green");
        props = new EMTProperties(props, "dev", "243243243", Environment.Type.SANDBOX).getProperties();
        System.out.println(props);
        HashMap<String, String> expected = new HashMap<>();
        expected.put("emt.plane", "bird");
        expected.put("emt.hello.first", "blue");
        expected.put("emt.hello.Second", "green");
        expected.put("emt.hello.third", "rover");
        Assertions.assertEquals(expected, props);
    }

}
