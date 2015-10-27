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

import org.strongback.Strongback;
import org.strongback.annotation.ThreadSafe;

/**
 * A switch that is initially not triggered but that can be (manually or automatically) reset.
 */
@ThreadSafe
public interface Fuse extends Switch {
    /**
     * Trigger the fuse. Once this method is called, the {@link #isTriggered()} switches but then will never change until it is
     * {@link #reset()}.
     *
     * @return this object to allow for chaining methods together; never null
     */
    public Fuse trigger();

    /**
     * Reset the fuse so it is no longer triggered.
     *
     * @return this object to allow for chaining methods together; never null
     */
    public Fuse reset();

    /**
     * Create a simple fuse that is manually {@link #trigger() triggered} and manually {@link #reset()}.
     *
     * @return the fuse; never null
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
     * Create a simple fuse that is manually {@link #trigger() triggered} and manually {@link #reset()}.
     *
     * @param whenTriggered the function that is to be called when the fuse is triggered; may be null
     * @return the fuse; never null
     */
    public static Fuse instantaneous( Runnable whenTriggered ) {
        return new Fuse() {

            @Override
            public boolean isTriggered() {
                return false;
            }

            @Override
            public Fuse trigger() {
                if (whenTriggered != null) {
                    try {
                        whenTriggered.run();
                    } catch (Throwable t) {
                        Strongback.logger(Fuse.class).error(t, "Error when calling fuse trigger function");
                    }
                }
                return this;
            }

            @Override
            public Fuse reset() {
                return this;
            }

            @Override
            public String toString() {
                return "notTriggered";
            }
        };
    }

    /**
     * Create a fuse that can be manually {@link #reset()} but that will automatically reset after the given delay.
     *
     * @param delay the time after the fuse is triggered that it should automatically reset; must be positive
     * @param unit the time units for the delay; may not be null
     * @param clock the clock that the fuse should use; if null, the system clock will be used
     * @return the auto-resetting fuse; never null
     */
    public static Fuse autoResetting(long delay, TimeUnit unit, Clock clock) {
        return autoResetting(delay, unit, clock, null);
    }

    /**
     * Create a fuse that can be manually {@link #reset()} but that will automatically reset after the given delay.
     *
     * @param delay the time after the fuse is triggered that it should automatically reset; must be positive
     * @param unit the time units for the delay; may not be null
     * @param clock the clock that the fuse should use; if null, the system clock will be used
     * @param whenTriggered the function that is to be called when the fuse is triggered; may be null
     * @return the auto-resetting fuse; never null
     */
    public static Fuse autoResetting(long delay, TimeUnit unit, Clock clock, Runnable whenTriggered) {
        Clock theClock = clock != null ? clock : Clock.system();
        if (unit == null) throw new IllegalArgumentException("The time unit may not be null");
        long delayTimeInMillis = unit.toMillis(delay);
        return new Fuse() {
            private boolean triggered = false;
            private volatile long resetTime = 0L;

            @Override
            public synchronized boolean isTriggered() {
                if (triggered && theClock.currentTimeInMillis() > resetTime) triggered = false;
                return triggered;
            }

            @Override
            public synchronized Fuse trigger() {
                triggered = true;
                resetTime = theClock.currentTimeInMillis() + delayTimeInMillis;
                if (whenTriggered != null) {
                    try {
                        whenTriggered.run();
                    } catch (Throwable t) {
                        Strongback.logger(Fuse.class).error(t, "Error when calling fuse trigger function");
                    }
                }
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