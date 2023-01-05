/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.propertiesprovider.utils;

import com.aeontronix.commons.StringUtils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class JacksonFlattener {
    /**
     * Flatten a hierarchical {@link Map} into a flat {@link Map} with key names using
     * property dot notation.
     *
     * @param inputMap must not be {@literal null}.
     * @return the resulting {@link Map}.
     * @since 2.0
     */
    public static Map<String, String> flattenToStringMap(Map<String, ? extends Object> inputMap) {
        Map<String, String> resultMap = new LinkedHashMap<>();
        doFlatten("", inputMap.entrySet().iterator(), resultMap, it -> it == null ? null : it.toString());
        return resultMap;
    }

    private static void doFlatten(String propertyPrefix, Iterator<? extends Map.Entry<String, ?>> inputMap,
                                  Map<String, ? extends Object> resultMap, Function<Object, Object> valueTransformer) {

        if (StringUtils.isNotBlank(propertyPrefix)) {
            propertyPrefix = propertyPrefix + ".";
        }

        while (inputMap.hasNext()) {

            Map.Entry<String, ? extends Object> entry = inputMap.next();
            flattenElement(propertyPrefix.concat(entry.getKey()), entry.getValue(), resultMap, valueTransformer);
        }
    }

    private static void flattenElement(String propertyPrefix, Object source, Map<String, ?> resultMap,
                                       Function<Object, Object> valueTransformer) {

        if (source instanceof Iterable) {
            flattenCollection(propertyPrefix, (Iterable<Object>) source, resultMap, valueTransformer);
            return;
        }

        if (source instanceof Map) {
            doFlatten(propertyPrefix, ((Map<String, ?>) source).entrySet().iterator(), resultMap, valueTransformer);
            return;
        }
        ((Map) resultMap).put(propertyPrefix, valueTransformer.apply(source));
    }

    private static void flattenCollection(String propertyPrefix, Iterable<Object> iterable, Map<String, ?> resultMap,
                                          Function<Object, Object> valueTransformer) {
        int counter = 0;
        for (Object element : iterable) {
            flattenElement(propertyPrefix + "[" + counter + "]", element, resultMap, valueTransformer);
            counter++;
        }
    }
}
