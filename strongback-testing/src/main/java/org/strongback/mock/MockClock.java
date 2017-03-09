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

package org.strongback.mock;

import java.util.concurrent.atomic.AtomicLong;

import org.strongback.components.Clock;

/**
 * A very simple mock {@link Clock} implementation that advances only when manually {@link #incrementBySeconds incremented}.
 *
 * @author Randall Hauch
 */
public class MockClock implements Clock {

    private final AtomicLong ticker = new AtomicLong(1000 * 1000 * 20); // 2 seconds

    public MockClock() {
    }

    /**
     * Increment the clock by the specified number of seconds.
     *
     * @param seconds the number of seconds; must be positive
     * @return this instance to enable chaining methods; never null
     */
    public MockClock incrementBySeconds(long seconds) {
        return incrementByMicroseconds(1000 * 1000 * seconds);
    }

    /**
     * Increment the clock by the specified number of milliseconds.
     *
     * @param miliseconds the number of milliseconds; must be positive
     * @return this instance to enable chaining methods; never null
     */
    public MockClock incrementByMilliseconds(long miliseconds) {
        return incrementByMicroseconds(1000 * miliseconds);
    }

    /**
     * Increment the clock by the specified number of microseconds.
     *
     * @param incrementInMicros the number of microseconds to add to the clock; must be positive
     * @return this instance to enable chaining methods; never null
     */
    public MockClock incrementByMicroseconds(long incrementInMicros) {
        if (incrementInMicros < 1) throw new IllegalArgumentException("The clock increment must be positive");
        ticker.accumulateAndGet(incrementInMicros, (a, b) -> a + b);
        return this;
    }

    @Override
    public long currentTimeInMicros() {
        return ticker.get();
    }

    @Override
    public String toString() {
        return Long.toString(currentTimeInMillis()) + " ms";
    }
}
