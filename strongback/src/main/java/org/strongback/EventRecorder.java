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

import org.strongback.annotation.ThreadSafe;
import org.strongback.command.Command;

/**
 * Records arbitrary non-continuous events to a log. This is part of Strongback's recorder feature.
 * <p>
 * There are two kinds of data that Strongback supports in its recording: events and data. Data are essentially continuous, and
 * Stronback's {@link DataRecorder} is used to capture at regular time steps values for all registered continuous channels.
 * Events, on the other hand, are far less frequent occurrences of some type of action; recording them as a channel is
 * inefficient (since there is no value at most time steps) and often makes little sense, so instead the {@link EventRecorder}
 * allows components to record these spurious and infrequent events. Both the data and event records can then be combined to
 * view a complete timeline with all available information.
 * <p>
 * Strongback's command scheduler can be {@link Strongback#configure() configured} to automatically record the state transitions
 * of {@link Command}s as they are executed. Of course, custom robot code can also record any other kinds of events.
 * <p>
 * Implementations of this class are expected to be thread-safe so that any of the {@link #record(String, String) record(...)}
 * methods can be called from any threads without having to lock or synchronize access.
 *
 * @author Randall Hauch
 */
@ThreadSafe
public interface EventRecorder extends Executable {

    /**
     * Record an event with the given identifier and event information.
     *
     * @param eventType the type of event; may not be null
     * @param value the event details as a string value; may be null
     */
    public void record(String eventType, String value);

    /**
     * Record an event with the given identifier and event information.
     *
     * @param eventType the type of event; may not be null
     * @param value the event detail as an integer value; may be null
     */
    public void record(String eventType, int value);

    /**
     * Record an event with the given identifier and event information.
     *
     * @param eventType the type of event; may not be null
     * @param value the event detail as an integer value; may be null
     */
    default public void record(String eventType, boolean value) {
        record(eventType, value ? 1 : 0);
    }

    /**
     * Return an {@link EventRecorder} implementation that does nothing.
     *
     * @return the no-operation event recorder; never null
     */
    public static EventRecorder noOp() {
        return new EventRecorder() {

            @Override
            public void record(String eventType, String value) {
            }

            @Override
            public void record(String eventType, int value) {
            }

            @Override
            public void execute(long timeInMillis) {
            }
        };
    }
}
