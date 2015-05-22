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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.IntSupplier;

import org.strongback.annotation.ThreadSafe;
import org.strongback.components.Motor;
import org.strongback.components.Switch;

/**
 * A threadsafe {@link DataRecorder} that allows for switches, motors and other functions to be registered, and then to
 * {@link #start() start} recording the values.
 *
 * @author Randall Hauch
 */
@ThreadSafe
final class DataRecorderWriter implements DataRecorder, Executable {

    private final List<DataRecorderChannel> channels = new CopyOnWriteArrayList<>();
    private final Function<List<DataRecorderChannel>, DataWriter> writerFactory;
    private final AtomicReference<DataWriter> writer = new AtomicReference<>(NULL_WRITER);

    DataRecorderWriter(Function<List<DataRecorderChannel>, DataWriter> writerFactory) {
        this.writerFactory = writerFactory != null ? writerFactory : (channels) -> NULL_WRITER;
    }

    protected boolean isRunning() {
        return writer.get() != NULL_WRITER;
    }

    @Override
    public void register(String name, IntSupplier supplier) {
        if (isRunning()) throw new IllegalStateException("The logger is already running; unable to register channel");
        if (name == null) throw new IllegalArgumentException("The name may not be null");
        if (supplier == null) throw new IllegalArgumentException("The supplier may not be null");
        channels.add(new DataRecorderChannel(name, supplier));
    }

    @Override
    public void registerSwitch(String name, Switch swtch) {
        if (isRunning()) throw new IllegalStateException("The logger is already running; unable to register channel");
        if (name == null) throw new IllegalArgumentException("The name may not be null");
        if (swtch == null) throw new IllegalArgumentException("The switch may not be null");
        channels.add(new DataRecorderChannel(name, () -> swtch.isTriggered() ? 1 : 0));
    }

    @Override
    public void registerMotor(String name, Motor motor) {
        if (isRunning()) throw new IllegalStateException("The logger is already running; unable to register channel");
        if (name == null) throw new IllegalArgumentException("The name may not be null");
        if (motor == null) throw new IllegalArgumentException("The motor may not be null");
        channels.add(new DataRecorderChannel(name + " speed", () -> (short) (motor.getSpeed() * 1000)));
    }

    @Override
    public synchronized void start() {
        writer.getAndUpdate((existing) -> existing == NULL_WRITER ? writerFactory.apply(channels) : existing);
    }

    @Override
    public synchronized void flush() {
        writer.get().close(); // reopens if necessary
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
