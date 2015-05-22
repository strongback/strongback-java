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

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.strongback.AsyncEventRecorder.EventType;
import org.strongback.AsyncEventRecorder.EventWriter;

/**
 * An {@link EventWriter} that records what it sees and allows tests to verify the order and values of events.
 *
 * @author Randall Hauch
 */
public class AccumulatingEventWriter implements EventWriter {

    public static final class Event {
        private final long time;
        private final int eventType;
        private final Object value;

        public Event(long time, int eventType, Object value) {
            this.time = time;
            this.eventType = eventType;
            this.value = value;
        }

        public boolean matches(long time, int eventType, int value) {
            return this.time == time && this.eventType == eventType && this.value instanceof Integer
                    && ((Integer) this.value).intValue() == value;
        }

        public boolean matches(long time, int eventType, String value) {
            return this.time == time && this.eventType == eventType && value.equals(this.value);
        }

        @Override
        public String toString() {
            return "time=" + time + "; type=" + eventType + "; value=" + value;
        }
    }

    private final Set<Integer> eventTypes = new HashSet<>();
    private final Map<String, EventType> eventTypesByName = new HashMap<>();
    private LinkedList<Event> events = new LinkedList<>();

    @Override
    public void recordEventType(long timeInMillis, EventType newType) {
        assertThat(timeInMillis > 0).isTrue();
        assertThat(newType).isNotNull();
        eventTypesByName.put(newType.typeName(), newType);
        eventTypes.add(newType.typeNumber());
    }

    @Override
    public void recordEvent(long timeInMillis, int eventType, String value) {
        assertThat(timeInMillis > 0).isTrue();
        assertThat(eventTypes.contains(eventType)).isTrue();
        events.add(new Event(timeInMillis, eventType, value));
    }

    @Override
    public void recordEvent(long timeInMillis, int eventType, int value) {
        assertThat(timeInMillis > 0).isTrue();
        assertThat(eventTypes.contains(eventType)).isTrue();
        events.add(new Event(timeInMillis, eventType, value));
    }

    public void assertMatch(long time, String eventType, String value) {
        assertThat(events.pop().matches(time, eventTypesByName.get(eventType).typeNumber(), value)).isTrue();
    }

    public void assertMatch(long time, String eventType, int value) {
        assertThat(events.pop().matches(time, eventTypesByName.get(eventType).typeNumber(), value)).isTrue();
    }

    public void assertMatch(long time, String eventType, boolean value) {
        assertMatch(time,eventType,value?1:0);
    }

    public void assertEmpty() {
        assertThat(events.isEmpty()).isTrue();
    }

    @Override
    public void close() {
    }

}
