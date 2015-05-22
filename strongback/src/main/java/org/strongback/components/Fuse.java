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

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import org.strongback.annotation.ThreadSafe;

/**
 * A switch that is initially not triggered but that can be (manually or automatically) reset.
 */
@ThreadSafe
public interface Fuse extends Switch {
    /**
     * Trigger the fuse. Once this method is called, the {@link #isTriggered()} switches but then will never change.
     * @return this object to allow for chaining methods together; never null
     */
    public Fuse trigger();

    /**
     * Reset the fuse so it is no longer triggered.
     * @return this object to allow for chaining methods together; never null
     */
    public Fuse reset();

    /**
     * Create a simple fuse.
     * @return
     */
    public static Fuse create() {
        return new Fuse() {
            private boolean triggered = false;
            @Override
            public boolean isTriggered() {
                return triggered;
            }
            @Override
            public Fuse trigger() {
                triggered = true;
                return this;
            }
            @Override
            public Fuse reset() {
                triggered = false;
                return this;
            }
            @Override
            public String toString() {
                return triggered ? "triggered" : "notTriggered";
            }
        };
    }

    /**
     * Create a fuse that will automatically reset after the given delay
     * @param delay the time after the fuse is triggered that it should automatically reset; must be positive
     * @param unit the time units for the delay; may not be null
     * @param timeProvider the provider of the current time in milliseconds; may be null if the {@link System#currentTimeMillis() system time} is to be used
     * @param timeProviderUnit the time unit of the provider
     * @return
     */
    public static Fuse autoResetting( long delay, TimeUnit unit, LongSupplier timeProvider, TimeUnit timeProviderUnit ) {
        if ( timeProvider == null ) {
            timeProvider = System::currentTimeMillis;
            timeProviderUnit = TimeUnit.MILLISECONDS;
        }
        if ( timeProviderUnit == null ) throw new IllegalArgumentException("The time provider unit may not be null");
        LongSupplier currentTime = timeProvider;
        long delayTime = timeProviderUnit.convert(delay, unit);
        return new Fuse() {
            private boolean triggered = false;
            private volatile long resetTime = 0L;
            @Override
            public synchronized boolean isTriggered() {
                if ( triggered && currentTime.getAsLong() > resetTime ) triggered = false;
                return triggered;
            }
            @Override
            public synchronized Fuse trigger() {
                triggered = true;
                resetTime = currentTime.getAsLong() + delayTime;
                return this;
            }
            @Override
            public synchronized Fuse reset() {
                triggered = false;
                return this;
            }
            @Override
            public String toString() {
                return triggered ? "triggered" : "notTriggered";
            }
        };

    }
}