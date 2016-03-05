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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * @author Randall Hauch
 */
final class FileDataWriter implements DataWriter {

    private final Supplier<String> filenameGenerator;
    private final Iterable<DataRecorderChannel> channels;
    private final List<IntSupplier> suppliers = new ArrayList<>();
    private MappedFileDataWriter writer;
    private long recordLength;
    private final long fileSize;
    private final int channelCount;

    public FileDataWriter(Iterable<DataRecorderChannel> channels, Supplier<String> filenameGenerator, int writesPerSecond,
            int runningTimeInSeconds) {
        this.filenameGenerator = filenameGenerator;
        this.channels = channels;

        // Estimate minimum file size needed to write records at the specified rate and duration ...
        int numWrites = writesPerSecond * runningTimeInSeconds;

        // Infrastructure for variable element length
        recordLength = Integer.BYTES;
        recordLength += (Short.BYTES * suppliers.size());
        fileSize = numWrites * recordLength + 1024; // add extra room for header and miscellaneous

        AtomicInteger count = new AtomicInteger();
        channels.forEach(ch->count.incrementAndGet());
        channelCount = count.get() + 1; // adding the time sequence

        openIfNeeded();
    }

    protected void openIfNeeded() {
        if (writer == null) {
            try {
                writer = new MappedFileDataWriter(filenameGenerator.get(), fileSize);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            suppliers.clear();

            // Write the header
            writer.write("data-record");

            // Write the number of elements
            writer.write(channelCount);

            // Write the size of each channel as an integer
            writer.write(Integer.BYTES); // size of the time channel
            channels.forEach(channel -> {
                IntSupplier supplier = channel.getSupplier();
                assert supplier != null;
                writer.write(Short.BYTES);
                suppliers.add(supplier);
            });

            // Write the channel names (for each the length and then the name) ...
            writer.write("Time");
            channels.forEach(channel -> {
                String name = channel.getName();
                assert name != null;
                writer.write(name);
            });
        } else if ( writer.remaining() < recordLength) {
            System.err.println("Insuffient space to write next all of next record, closing file");
            close();
            openIfNeeded();
        }
    }

    @Override
    public void write(long time) {
        openIfNeeded();
        writer.write((int) time);
        suppliers.forEach((supplier) -> writer.write((short) supplier.getAsInt()));
    }

    @Override
    public void close() {
        try {
            writer.close();
        } finally {
            writer = null;
            suppliers.clear();
        }
    }

}
