/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FlowHelper {
    public static boolean isXmlUsingAPIKit(Document xmlDoc) {
        final Element rootEl = xmlDoc.getDocumentElement();
        return "http://www.mulesoft.org/schema/mule/core".equals(rootEl.getNamespaceURI()) &&
                "mule".equals(rootEl.getLocalName()) &&
                rootEl.getElementsByTagNameNS("http://www.mulesoft.org/schema/mule/mule-apikit","config").getLength() > 0;
    }
}
