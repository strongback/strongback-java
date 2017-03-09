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

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.strongback.annotation.ThreadSafe;
import org.strongback.components.Clock;

/**
 * A thread-safe and lock-free {@link EventRecorder} implementation that records all events to a thread-safe queue and then when
 * {@link #execute(long) executed} writes all enqueued events in the same order as received. The {@link AsyncEventRecorder} is
 * {@link Executable}, and is designed to be {@link Executor#register(Executable) registered} with an {@link Executor} to
 * automatically and periodically write the enqueued events to the given {@link EventWriter}.
 *
 * @author Randall Hauch
 */
@ThreadSafe
final class AsyncEventRecorder implements EventRecorder {

    private static final AtomicInteger TYPE_NUMBER_GENERATOR = new AtomicInteger(0);
    private static final EventType NEW_EVENT_TYPE = new EventType("NewEventType");

    protected static final class EventType {
        private final int typeNumber;
        private final String typeName;

        protected EventType(String typeName) {
            this.typeName = typeName;
            this.typeNumber = TYPE_NUMBER_GENERATOR.incrementAndGet();
        }

        public String typeName() {
            return typeName;
        }

        public int typeNumber() {
            return typeNumber;
        }
    }

    protected static interface EventWriter extends AutoCloseable {
        /**
         * Record a new type of event.
         *
         * @param timeInMillis the time (in milliseconds) of the event
         * @param newType the event information; never null
         */
        public void recordEventType(long timeInMillis, EventType newType);

        /**
         * Record an event with the specified {@link EventType#typeNumber() event type number} and String value. Before this
         * method is called, {@link #recordEventType(long, EventType)} will have been called for the given type of event
         *
         * @param timeInMillis the time (in milliseconds) of the event
         * @param eventType the type of event
         * @param value the event value
         */
        public void recordEvent(long timeInMillis, int eventType, String value);

        /**
         * Record an event with the specified {@link EventType#typeNumber() event type number} and integer value. Before this
         * method is called, {@link #recordEventType(long, EventType)} will have been called for the given type of event
         *
         * @param timeInMillis the time (in milliseconds) of the event
         * @param eventType the type of event
         * @param value the event value
         */
        public void recordEvent(long timeInMillis, int eventType, int value);

        @Override
        public void close();
    }

    private final ConcurrentMap<String, EventType> eventTypes = new ConcurrentHashMap<>();
    private final QueuedWriter queuedWriter;
    private final Clock clock;

    protected AsyncEventRecorder(EventWriter writer, Clock clock) {
        this.eventTypes.put(NEW_EVENT_TYPE.typeName(), NEW_EVENT_TYPE);
        this.clock = clock;
        this.queuedWriter = new QueuedWriter(writer);
    }

    protected int typeNumber(String eventType) {
        EventType info = eventTypes.get(eventType);
        if (info == null) {
            info = new EventType(eventType);
            EventType prev = eventTypes.putIfAbsent(eventType, info);
            if (prev == null) {
                // We added a new type, so record it ...
                queuedWriter.recordEventType(clock.currentTimeInMillis(), info);
            }
            // Otherwise, somebody already beat us to it so we don't have to
        }
        return info.typeNumber();
    }

    @Override
    public void record(String eventType, String value) {
        queuedWriter.recordEvent(clock.currentTimeInMillis(), typeNumber(eventType), value);
    }

    @Override
    public void record(String eventType, int value) {
        queuedWriter.recordEvent(clock.currentTimeInMillis(), typeNumber(eventType), value);
    }

    @Override
    public void execute(long timeInMillis) {
        queuedWriter.execute(timeInMillis);
    }

    @FunctionalInterface
    protected static interface Event {
        public void write(EventWriter writer);
    }

    protected static final class QueuedWriter implements EventWriter, Executable {
        private final EventWriter writer;
        private final Queue<Event> queue = new ConcurrentLinkedQueue<>();
        private Event event = null;

        public QueuedWriter(EventWriter writer) {
            this.writer = writer;
        }

        @Override
        public void recordEvent(long timeInMillis, int eventType, int value) {
            queue.offer(writer -> writer.recordEvent(timeInMillis, eventType, value));
        }

        @Override
        public void recordEvent(long timeInMillis, int eventType, String value) {
            queue.offer(writer -> writer.recordEvent(timeInMillis, eventType, value));
        }

        @Override
        public void recordEventType(long timeInMillis, EventType newType) {
            queue.offer(writer -> writer.recordEventType(timeInMillis, newType));
        }

        @Override
        public void close() {
            queue.offer(writer -> writer.close());
        }

        @Override
        public void execute(long timeInMillis) {
            while ((event = queue.poll()) != null) {
                event.write(writer);
            }
        }
    }
}
