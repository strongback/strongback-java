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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.strongback.annotation.ThreadSafe;

/**
 * A threadsafe {@link DataRecorder} that allows for switches, motors and other functions to be registered, and then to
 * {@link #start() start} recording the values.
 *
 * @author Randall Hauch
 */
@ThreadSafe
final class DataRecorderDriver implements Executable {

    private final DataRecorderChannels channels;
    private final Function<Iterable<DataRecorderChannel>, DataWriter> writerFactory;
    private final AtomicReference<DataWriter> writer = new AtomicReference<>(NULL_WRITER);

    DataRecorderDriver(DataRecorderChannels channels, Function<Iterable<DataRecorderChannel>, DataWriter> writerFactory) {
        this.channels = channels;
        this.writerFactory = writerFactory != null ? writerFactory : (channelIter) -> NULL_WRITER;
    }

    protected boolean isRunning() {
        return writer.get() != NULL_WRITER;
    }

    public synchronized void start() {
        writer.getAndUpdate((existing) -> existing == NULL_WRITER ? writerFactory.apply(channels) : existing);
    }

    public synchronized void flush() {
        writer.get().close(); // reopens if necessary
    }

    public synchronized void stop() {
        // We will always replace the existing data writer with NULL_WRITER, but only after we do this do we close the
        // writer. These steps are done in a very strict and ordered manner to ensure the non-NULL_WRITER is always closed.
        AtomicReference<DataWriter> unclosed = new AtomicReference<>();
        try {
            writer.getAndUpdate((existing) -> {
                if (existing != NULL_WRITER) unclosed.set(existing);
                return NULL_WRITER;
            });
        } finally {
            if (unclosed.get() != null) {
                unclosed.get().close();
            }
        }
    }

    @Override
    public void execute(long timeInMillis) {
        writer.get().write(timeInMillis);
    }

    private static final DataWriter NULL_WRITER = new DataWriter() {
        @Override
        public void write(long time) {
        }

        @Override
        public void close() {
        }
    };
}
