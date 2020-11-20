/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.commons.FileUtils;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.kloudtek.unpack.FileType;
import com.kloudtek.unpack.UnpackException;
import com.kloudtek.unpack.Unpacker;
import com.kloudtek.unpack.transformer.Transformer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ApplicationEnhancer {
    public static void enhanceApplicationDescriptor() {

    }

    public static void enhanceApplicationArchive(ApplicationDescriptor applicationDescriptor, File file) throws IOException, UnpackException {
        File oldArtifactFile = new File(file.getPath() + ".preweaving");
        if (oldArtifactFile.exists()) {
            FileUtils.delete(oldArtifactFile);
        }
        if (!file.renameTo(oldArtifactFile)) {
            throw new IOException("Unable to move " + file.getPath() + " to " + oldArtifactFile.getPath());
        }
        Unpacker unpacker = new Unpacker(oldArtifactFile, FileType.ZIP, file, FileType.ZIP);
        final ArrayList<Transformer> transformers = new ArrayList<>();
        transformers.add(new ApplicationEnhancementCoreTransformer(applicationDescriptor));
        unpacker.addTransformers(transformers);
        unpacker.unpack();
    }

//    @NotNull
//    private static Map<String, Object> loadDescriptor() throws IOException {
//        Map<String, Object> anypointDescriptor = null;
//        if (StringUtils.isNotBlank(descriptor)) {
//            File descriptorFile = new File(descriptor);
//            anypointDescriptor = readFile(descriptorFile);
//        } else {
//            File descriptorFile = findAnypointFile(project.getBasedir());
//            if (descriptorFile != null) {
//                anypointDescriptor = readFile(descriptorFile);
//            }
//        }
//        if (anypointDescriptor == null) {
//            anypointDescriptor = new HashMap<>();
//        }
//        return anypointDescriptor;
//    }

}
