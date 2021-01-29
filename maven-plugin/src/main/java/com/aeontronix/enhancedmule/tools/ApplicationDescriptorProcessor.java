/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.ApplicationDescriptor;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public interface ApplicationDescriptorProcessor {
    void writeToFile(File file, boolean addToProject) throws IOException;

    ObjectNode getApplicationDescriptorJson();

    void setDefaultValues(boolean inheritNameAndDesc) throws IOException;
    void legacyConvert();
    ApplicationDescriptor getAnypointDescriptor() throws IOException;
}
