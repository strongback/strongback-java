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

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntSupplier;

import org.strongback.annotation.ThreadSafe;
import org.strongback.components.SpeedSensor;
import org.strongback.components.Switch;
import org.strongback.util.Iterators;

/**
 * A threadsafe {@link DataRecorder} that allows for switches, motors and other functions to be registered, and then to
 * {@link #start() start} recording the values.
 *
 * @author Randall Hauch
 */
@ThreadSafe
final class DataRecorderChannels implements DataRecorder, Iterable<DataRecorderChannel> {

    private final CopyOnWriteArrayList<DataRecorderChannel> channels = new CopyOnWriteArrayList<>();

    DataRecorderChannels() {
    }

    @Override
    public DataRecorder register(String name, IntSupplier supplier) {
        if (name == null) throw new IllegalArgumentException("The name may not be null");
        if (supplier == null) throw new IllegalArgumentException("The supplier may not be null");
        channels.addIfAbsent(new DataRecorderChannel(name, supplier));
        return this;
    }

    @Override
    public DataRecorder register(String name, Switch swtch) {
        if (name == null) throw new IllegalArgumentException("The name may not be null");
        if (swtch == null) throw new IllegalArgumentException("The switch may not be null");
        channels.addIfAbsent(new DataRecorderChannel(name, () -> swtch.isTriggered() ? 1 : 0));
        return this;
    }

    @Override
    public DataRecorder register(String name, SpeedSensor sensor) {
        if (name == null) throw new IllegalArgumentException("The name may not be null");
        if (sensor == null) throw new IllegalArgumentException("The motor may not be null");
        channels.addIfAbsent(new DataRecorderChannel(name + " speed", () -> (int) (sensor.getSpeed() * 1000)));
        return this;
    }

    @Override
    public DataRecorder register(String name, DataRecordable recordable) {
        if (name == null) throw new IllegalArgumentException("The name may not be null");
        if (recordable == null) throw new IllegalArgumentException("The recordable may not be null");
        recordable.registerWith(this, name);
        return this;
    }

    @Override
    public Iterator<DataRecorderChannel> iterator() {
        return Iterators.immutable(channels);
    }
}
