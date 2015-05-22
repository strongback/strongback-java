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
 * This class should perform pretty reliably for millisecond-precision periods on the RoboRIO, although strictly speaking it
 * depends on the particular JVM. Internally, the class uses {@link LockSupport#parkNanos(long) concurrent utilities} that
 * should perform well on Linux kernel 2.6+, which means it should perform well on the RoboRIO (which for 2015 used <a
 * ref="http://khengineering.github.io/RoboRio/faq/roborio/">kernel 3.2.35</a>). It also appears to work well on OS X 10.9.
 */
@FunctionalInterface
public interface Metronome {

    /**
     * Pause until the next tick of the metronome.
     *
     * @return true if the pause completed normally, or false if it was interrupted
     */
    public boolean pause();

    /**
     * Create a new metronome that starts ticking immediately and that uses {@link Thread#sleep(long)} to wait, which in general
     * is an inaccurate but simple approach.
     *
     * @param period the period of time that the metronome ticks and for which {@link #pause()} waits
     * @param unit the unit of time; may not be null
     * @param timeSystem the time system that will provide the current time; may not be null
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
        };
    }

    /**
     * Create a new metronome that starts ticking immediately and that uses {@link LockSupport#parkNanos(long)} to wait, which
     * generally works on many Linux platforms.
     *
     * @param period the period of time that the metronome ticks and for which {@link #pause()} waits
     * @param unit the unit of time; may not be null
     * @param timeSystem the time system that will provide the current time; may not be null
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
        };
    }

    /**
     * Create a new metronome that starts ticking immediately and that uses a busy loop to keep the thread active and works on
     * every platform. This is very accurate and reliable since it does not use thread context changes, but it is not terribly
     * efficient since the thread does not yield while waiting.
     *
     * @param period the period of time that the metronome ticks and for which {@link #pause()} waits
     * @param unit the unit of time; may not be null
     * @param timeSystem the time system that will provide the current time; may not be null
     */
    public static Metronome busy(long period, TimeUnit unit, Clock timeSystem) {
        long periodInNanos = unit.toNanos(period);
        return new Metronome() {
            private long next = timeSystem.currentTimeInNanos() + periodInNanos;

            @Override
            public boolean pause() {
                while (next > timeSystem.currentTimeInNanos()) {}
                next = next + periodInNanos;
                return true;
            }
        };
    }
}