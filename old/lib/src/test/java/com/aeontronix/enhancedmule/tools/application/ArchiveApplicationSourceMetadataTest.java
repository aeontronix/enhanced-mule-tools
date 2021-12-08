/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveApplicationSourceMetadataTest {
    @Test
    public void testMetadataFromArchive() throws Exception {
        final ArchiveApplicationSourceMetadata src = new ArchiveApplicationSourceMetadata(new File(Objects.requireNonNull(getClass().getResource("/api-application.jar")).toURI()));
        assertEquals("testapp", src.getArtifactId());
    }
}
