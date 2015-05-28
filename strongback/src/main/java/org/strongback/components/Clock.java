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

import org.strongback.StrongbackRequirementException;

import edu.wpi.first.wpilibj.Utility;

/**
 * The system that provides information about match time.
 *
 * @author Randall Hauch
 */
@FunctionalInterface
public interface Clock {

    /**
     * Return the current time, in microseconds.
     *
     * @return the elapsed time, in microseconds
     */
    public long currentTimeInMicros();

    /**
     * Return the current time, in nanoseconds.
     *
     * @return the elapsed time, in nanoseconds
     */
    default public long currentTimeInNanos() {
        return currentTimeInMicros() * 1000;
    }

    /**
     * Return the current time, in milliseconds.
     *
     * @return the elapsed time, in milliseconds
     */
    default public long currentTimeInMillis() {
        return (long) (currentTimeInMicros() / 1000.0);
    }

    /**
     * Create a new time system that uses the FPGA clock. At this time, the precision of the resulting clock has not been
     * verified or tested.
     *
     * @return the FPGA-based clock; never null
     * @throws StrongbackRequirementException if the FPGA hardware is not available
     */
    public static Clock fpga() {
        try {
            Utility.getFPGATime();
            // If we're here, then the method did not throw an exception and there is FPGA hardware on this platform ...
            return Utility::getFPGATime;
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            throw new StrongbackRequirementException("Missing FPGA hardware or software", e);
        }
    }

    /**
     * Create a new time system that uses the FPGA clock if it is available, or the {@link #system() JRE's time system} if not.
     *
     * @return the FPGA-based clock or (if that is not available) the system clock; never null
     */
    public static Clock fpgaOrSystem() {
        try {
            return fpga();
        } catch (StrongbackRequirementException e) {
            return system();
        }
    }

    /**
     * Create a new time system that uses the JRE's System clock. The performance and precision of the JVM clock depends upon
     * the accuracy and precision of the underlying platform's mechanism for returning absolute and relative time. Absolute time
     * can be obtained via {@link #currentTimeInMillis()}, although precision on some platforms may be as low as 10 or 20
     * milliseconds. Relative time that can be compared within a single process can be obtained via
     * {@link #currentTimeInNanos()} and {@link #currentTimeInMicros()}, although again the precision may be at best around 10
     * or 20 microseconds.
     * <p>
     * Additionally, care must be taken when using relative time on multi-core systems. Because relative time often uses the
     * CPU's clock, relative time obtained from different CPUs might not be comparable. This is possible even if using a single
     * thread, since the same thread might be scheduled on a different CPU after switching thread contexts.
     *
     * @return the system clock; never null
     */
    public static Clock system() {
        return new Clock() {
            @Override
            public long currentTimeInMicros() {
                return (long) (currentTimeInNanos() / 1000.0);
            }

            @Override
            public long currentTimeInNanos() {
                return System.nanoTime();
            }

            @Override
            public long currentTimeInMillis() {
                return System.currentTimeMillis();
            }
        };
    }
}
