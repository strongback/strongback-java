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

package org.strongback.components;

import java.util.concurrent.atomic.AtomicInteger;

import org.strongback.annotation.ThreadSafe;

/**
 * A simple counter that is safe to be concurrently used (both {@link #get()} and {@link #increment()}) in one or more threads.
 *
 * @author Randall Hauch
 */
@ThreadSafe
public interface Counter extends Zeroable {

    /**
     * Get the current count.
     *
     * @return the current count; will never be negative
     */
    public int get();

    /**
     * Increment the counter.
     *
     * @return this object to allow chaining of methods; never null
     */
    public Counter increment();

    @Override
    public Counter zero();

    /**
     * Create a new counter that will monotonically increment by 1 and upon reaching the {@link Integer#MAX_VALUE maximum
     * integer value} will upon the next {@link #increment()} call automatically reset to 0.
     *
     * @return the new counter; never null
     */
    public static Counter unlimited() {
        return circular(Integer.MAX_VALUE);
    }

    /**
     * Create a new counter that will monotonically increment by 1 and upon reaching the {@link Integer#MAX_VALUE maximum
     * integer value} will upon the next {@link #increment()} call automatically reset to 1.
     *
     * @param initial the initial value for the counter; must be 0 or positive
     * @return the new counter; never null
     */
    public static Counter unlimited(int initial) {
        return circular(initial, 1, Integer.MAX_VALUE);
    }

    /**
     * Create a new counter that will increment by 1 and that upon reaching the supplied maximum value will upon the next
     * {@link #increment()} call automatically reset to 0.
     *
     * @param maximum the maximum value for the counter before it resets to 0; this must be positive
     * @return the new counter; never null
     */
    public static Counter circular(int maximum) {
        return circular(0, 1, maximum);
    }

    /**
     * Create a new counter that will increment by the specified amount and that upon reaching the supplied maximum value will
     * upon the next {@link #increment()} call automatically reset to the given initial value.
     *
     * @param initial the initial value for the counter; must be 0 or positive
     * @param increment the value by which the counter should be incremented when {@link #increment()} is called; must be
     *        positive
     * @param maximum the maximum value the counter can achieve; must be positive and greater than {@code initial}
     * @return the new counter; never null
     * @throws IllegalArgumentException if {@code initial} or {@code resetValue} are negative, or if {@code maximum} is less
     *         than or equal to {@code initial}, or if {@code increment} is not positive
     */
    public static Counter circular(int initial, int increment, int maximum) {
        if (initial < 0) throw new IllegalArgumentException("The intial value must be non-negative");
        if (maximum <= initial) throw new IllegalArgumentException("The maximum value must be greater than the initial value");
        if (increment < 1) throw new IllegalArgumentException("The increment value must be positive");
        return new Counter() {
            private final AtomicInteger value = new AtomicInteger(initial);

            @Override
            public int get() {
                return value.get();
            }

            @Override
            public Counter increment() {
                // Atomically determine the next value for the counter using the supplied function ...
                value.getAndUpdate((currentValue) -> currentValue < maximum ? currentValue + increment : initial);
                return this;
            }

            @Override
            public Counter zero() {
                value.set(0);
                return this;
            }
        };
    }
}