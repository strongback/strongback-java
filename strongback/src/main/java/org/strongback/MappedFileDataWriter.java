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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.strongback.annotation.ThreadSafe;

/**
 * @author Randall Hauch
 *
 */
@ThreadSafe
final class MappedFileDataWriter implements DataWriter {

    private final Charset UTF8 = StandardCharsets.UTF_8;

    private final File outFile;
    private final FileChannel channel;
    private final MappedByteBuffer buffer;

    @SuppressWarnings("resource")
    protected MappedFileDataWriter( String filename, long size ) throws IOException {
        outFile = new File(filename);
        channel = new RandomAccessFile(outFile, "rw").getChannel();
        buffer = channel.map(MapMode.READ_WRITE, 0, size);
    }

    public void write( String str ) {
        buffer.putInt(str.length());
        buffer.put(str.getBytes(UTF8));
    }

    public void write( int number ) {
        buffer.putInt(number);
    }

    @Override
    public void write( long number ) {
        buffer.putLong(number);
    }

    public void write( short number ) {
        buffer.putShort(number);
    }

    public void write( float number ) {
        buffer.putFloat(number);
    }

    public void write( double number ) {
        buffer.putDouble(number);
    }

    public int remaining() {
        return buffer.remaining();
    }

    @Override
    public void close() {
        try {
            // Write terminator
            buffer.putInt(0xFFFFFFFF);
        } finally {
            try {
                // And always force the buffer ...
                buffer.force();
            } finally {
                try{
                    // And always close the channel ...
                    channel.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to close channel",e);
                }
            }
        }
    }

}
