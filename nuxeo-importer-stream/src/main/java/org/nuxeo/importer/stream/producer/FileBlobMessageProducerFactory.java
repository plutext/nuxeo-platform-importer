/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     bdelbosc
 */
package org.nuxeo.importer.stream.producer;

import java.io.File;

import org.nuxeo.importer.stream.message.BlobMessage;
import org.nuxeo.lib.stream.pattern.producer.ProducerFactory;
import org.nuxeo.lib.stream.pattern.producer.ProducerIterator;

/**
 * @since 9.1
 */
public class FileBlobMessageProducerFactory implements ProducerFactory<BlobMessage> {
    protected final File listFile;

    /**
     * Produce messages to import files listed in the listFile.
     */
    public FileBlobMessageProducerFactory(File listFile) {
        this.listFile = listFile;
    }

    @Override
    public ProducerIterator<BlobMessage> createProducer(int producerId) {
        return new FileBlobMessageProducer(producerId, listFile);
    }
}
