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

package org.strongback.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.strongback.components.Clock;

/**
 * A class that can be used to perform an action at a regular interval. To use, set up a {@code Metronome} instance and perform
 * a repeated event (perhaps using a loop), calling {@link #pause()} before each event.
 * <p>
 * There are several implementations provided by this class, and each varies in the precision of the interval based upon the
 * supplied {@link Clock}, interval, and the technique used to pause. Among these implementations, the
 * {@link #busy(long, TimeUnit, Clock)} method produces the most accurate, precise, and consistent pause intervals down to 1
 * millisecond on most platforms (especially modern Linux and OS X).
 *
 * @author Randall Hauch
 */
public interface Metronome {

    /**
     * Pause until the next tick of the metronome.
     *
     * @return true if the pause completed normally, or false if it was interrupted
     */
    public boolean pause();

    /**
     * Create a new metronome that starts ticking immediately and that uses {@link Thread#sleep(long)} to wait.
     * <p>
     * Generally speaking, this is a simple but inaccurate approach for periods anywhere close to the precision of the supplied
     * Clock (which for the {@link Clock#system() system clock} is typically around 10-15 milliseconds for modern Linux and OS X
     * systems, and potentially worse on Windows and older Linux/Unix systems. And because this metronome uses
     * Thread#sleep(long), thread context switches are likely and will negatively affect the precision of the metronome's
     * period.
     * <p>
     * Although the method seemingly supports taking {@link TimeUnit#MICROSECONDS} and {@link TimeUnit#NANOSECONDS}, it is
     * likely that the JVM and operating system do not support such fine-grained precision. And as mentioned above, care should
     * be used when specifying a {@code period} of 20 milliseconds or smaller.
     *
     * @param period the period of time that the metronome ticks and for which {@link #pause()} waits
     * @param unit the unit of time; may not be null
     * @param timeSystem the time system that will provide the current time; may not be null
     * @return the new metronome; never null
     */
    public static Metronome sleeper(long period, TimeUnit unit, Clock timeSystem) {
        long periodInMillis = unit.toMillis(period);
        return new Metronome() {
            private long next = timeSystem.currentTimeInMillis() + periodInMillis;

            @Override
            public boolean pause() {
                while (next > timeSystem.currentTimeInMillis()) {
                    try {
                        Thread.sleep(next - timeSystem.currentTimeInMillis());
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        return false;
                    }
                }
                next = next + periodInMillis;
                return true;
            }

            @Override
            public String toString() {
                return "Metronome (sleep for " + periodInMillis + " ms)";
            }
        };
    }

    /**
     * Create a new metronome that starts ticking immediately and that uses {@link LockSupport#parkNanos(long)} to wait.
     * <p>
     * {@link LockSupport#parkNanos(long)} uses the underlying platform-specific timed wait mechanism, which may be more
     * accurate for smaller periods than {@link #sleeper(long, TimeUnit, Clock)}. However, like
     * {@link #sleeper(long, TimeUnit, Clock)}, the resulting Metronome may result in (expensive) thread context switches.
     * <p>
     * Although the method seemingly supports taking {@link TimeUnit#MICROSECONDS} and {@link TimeUnit#NANOSECONDS}, it is
     * likely that the JVM and operating system do not support such fine-grained precision. And as mentioned above, care should
     * be used when specifying a {@code period} of 10-15 milliseconds or smaller.
     *
     * @param period the period of time that the metronome ticks and for which {@link #pause()} waits
     * @param unit the unit of time; may not be null
     * @param timeSystem the time system that will provide the current time; may not be null
     * @return the new metronome; never null
     */
    public static Metronome parker(long period, TimeUnit unit, Clock timeSystem) {
        long periodInNanos = unit.toNanos(period);
        return new Metronome() {
            private long next = timeSystem.currentTimeInNanos() + periodInNanos;

            @Override
            public boolean pause() {
                while (next > timeSystem.currentTimeInNanos()) {
                    LockSupport.parkNanos(next - timeSystem.currentTimeInNanos());
                }
                next = next + periodInNanos;
                return true;
            }

            @Override
            public String toString() {
                return "Metronome (park for " + TimeUnit.NANOSECONDS.toMillis(periodInNanos) + " ms)";
            }
        };
    }

    /**
     * Create a new metronome that starts ticking immediately and that uses a busy loop to keep the thread active and works on
     * every platform.
     * <p>
     * Theoretically this is the most accurate Metronome since it prevents thread context switches in the JVM and because it
     * relies upon the supplied {@link Clock}'s {@link Clock#currentTimeInNanos() relative time} that is likely accurate to a
     * small number of microseconds.
     * <p>
     * Although the method seemingly supports taking {@link TimeUnit#MICROSECONDS} and {@link TimeUnit#NANOSECONDS}, it is
     * likely that the JVM and operating system do not support such fine-grained precision. And as mentioned above, care should
     * be used when specifying a {@code period} of 10-15 milliseconds or smaller.
     *
     * @param period the period of time that the metronome ticks and for which {@link #pause()} waits
     * @param unit the unit of time; may not be null
     * @param timeSystem the time system that will provide the current time; may not be null
     * @return the new metronome; never null
     */
    public static Metronome busy(long period, TimeUnit unit, Clock timeSystem) {
        long periodInNanos = unit.toNanos(period);
        return new Metronome() {
            private long next = timeSystem.currentTimeInNanos() + periodInNanos;

            @Override
            public boolean pause() {
                while (next - timeSystem.currentTimeInNanos() > 0) {
                }
                next = next + periodInNanos;
                return true;
            }

            @Override
            public String toString() {
                return "Metronome (busy wait for " + TimeUnit.NANOSECONDS.toMillis(periodInNanos) + " ms)";
            }
        };
    }
}