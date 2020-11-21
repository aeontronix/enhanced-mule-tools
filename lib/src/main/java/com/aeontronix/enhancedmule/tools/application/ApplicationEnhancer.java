/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.commons.FileUtils;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloudtek.unpack.FileType;
import com.kloudtek.unpack.UnpackException;
import com.kloudtek.unpack.Unpacker;
import com.kloudtek.unpack.transformer.Transformer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipFile;

public class ApplicationEnhancer {
    public static void enhanceApplicationArchive(File file, File descriptorFile, ApplicationDescriptor applicationDescriptor) throws IOException, UnpackException {
        File oldArtifactFile = new File(file.getPath() + ".preweaving");
        if (oldArtifactFile.exists()) {
            FileUtils.delete(oldArtifactFile);
        }
        if (!file.renameTo(oldArtifactFile)) {
            throw new IOException("Unable to move " + file.getPath() + " to " + oldArtifactFile.getPath());
        }
        Unpacker unpacker = new Unpacker(oldArtifactFile, FileType.ZIP, file, FileType.ZIP);
        final ArrayList<Transformer> transformers = new ArrayList<>();
        transformers.add(new ApplicationEnhancementCoreTransformer(descriptorFile,applicationDescriptor));
        unpacker.addTransformers(transformers);
        unpacker.unpack();
    }
}
