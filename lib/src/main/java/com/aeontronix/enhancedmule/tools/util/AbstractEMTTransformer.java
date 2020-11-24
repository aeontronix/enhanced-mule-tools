/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.commons.io.InMemInputFilterStream;
import com.aeontronix.commons.xml.XmlUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloudtek.unpack.*;
import com.kloudtek.unpack.transformer.Transformer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class AbstractEMTTransformer extends Transformer {
    private ObjectMapper objectMapper = new ObjectMapper();

    public void addFileTransform(Source source, Destination destination, String name, String path, FileTransformer fileTransformer, boolean create) throws UnpackException {
        SourceFile file = (SourceFile) source.getFile(path);
        if (file == null) {
            if (create) {
                source.add(new InMemSourceFile(name, path, fileTransformer.transform(null)));
            } else {
                throw new UnpackException("File not found: " + path);
            }
        } else {
            file.setInputStream(new InMemInputFilterStream(file.getInputStream()) {
                @Override
                protected byte[] transform(byte[] data) throws IOException {
                    try {
                        return fileTransformer.transform(data);
                    } catch (UnpackException e) {
                        throw new IOException(e);
                    }
                }
            });
        }
    }

    public interface FileTransformer {
        byte[] transform(byte[] data) throws UnpackException;
    }

    public abstract class JsonFileTransformer<X extends JsonNode> implements FileTransformer {
        @SuppressWarnings("unchecked")
        @Override
        public final byte[] transform(byte[] data) throws UnpackException {
            try {
                return objectMapper.writeValueAsBytes(transformJson((X) objectMapper.readTree(data)));
            } catch (IOException e) {
                throw new UnpackException(e);
            }
        }

        protected abstract X transformJson(X root) throws UnpackException;
    }

    public abstract class XMLFileTranformer implements FileTransformer {
        @Override
        public final byte[] transform(byte[] data) throws UnpackException {
            try {
                final Document doc = XmlUtils.parse(new ByteArrayInputStream(data));
                final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                XmlUtils.serialize(transformXml(doc), outStream);
                outStream.close();
                return outStream.toByteArray();
            } catch (IOException | SAXException e) {
                throw new UnpackException(e);
            }
        }

        protected abstract Document transformXml(Document root);
    }
}
