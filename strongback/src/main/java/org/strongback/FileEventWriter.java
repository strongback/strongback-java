/*
 * Strongback
 * Copyright 2015, Strongback and individual contributors by the @authors tag.
 * See the COPYRIGHT.txt in the distribution for a full listing of individual
 * contributors.
 *
 * Licensed under the MIT License; you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://opensource.org/licenses/MIT
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.strongback;

import java.io.IOException;
import java.util.function.Supplier;

import org.strongback.AsyncEventRecorder.EventType;
import org.strongback.AsyncEventRecorder.EventWriter;
import org.strongback.annotation.ThreadSafe;

/**
 * @author Randall Hauch
 *
 */
@ThreadSafe
final class FileEventWriter implements EventWriter {

    private static final byte STRING_TYPE = 0x1;
    private static final byte INT_TYPE = 0x2;

    private final Supplier<String> filenameGenerator;
    private MappedFileDataWriter writer;
    private final long fileSize;

    public FileEventWriter(Supplier<String> filenameGenerator, long fileSize) {
        this.filenameGenerator = filenameGenerator;
        this.fileSize = fileSize;
    }

    protected void openIfNeeded() {
        if (writer == null) {
            try {
                writer = new MappedFileDataWriter(filenameGenerator.get(), fileSize);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (writer.remaining() < 100) {
            System.err.println("Insuffient space to write next all of next record, closing file");
            close();
            openIfNeeded();
        }
    }

    @Override
    public void recordEventType(long time, EventType newType) {
        openIfNeeded();
        writer.write(time);
        writer.write(newType.typeName());
        writer.write(newType.typeNumber());
    }

    @Override
    public void recordEvent(long time, int eventType, String value) {
        openIfNeeded();
        writer.write(time);
        writer.write(eventType);
        writer.write(STRING_TYPE);
        writer.write(value);
    }

    @Override
    public void recordEvent(long time, int eventType, int value) {
        openIfNeeded();
        writer.write(time);
        writer.write(eventType);
        writer.write(INT_TYPE);
        writer.write(value);
    }

    @Override
    public void close() {
        try {
            writer.close();
        } finally {
            writer = null;
        }
    }

}
