/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.ApplicationSourceMetadata;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public interface ApplicationDescriptorProcessor {
    void writeToFile(File file, boolean addToProject) throws IOException;

    ObjectNode getApplicationDescriptorJson();

    void legacyConvert();

    ApplicationDescriptor getAnypointDescriptor() throws IOException;
}
