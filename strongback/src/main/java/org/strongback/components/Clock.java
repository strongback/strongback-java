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
        return (long)(currentTimeInMicros() / 1000.0);
    }

    /**
     * Create a new time system that uses the FPGA clock.
     * @throws StrongbackRequirementException if the FPGA hardware is not available
     */
    public static Clock fpga() {
        try {
            Utility.getFPGATime();  // try it ...
            return Utility::getFPGATime;
        } catch ( UnsatisfiedLinkError e ) {
            throw new StrongbackRequirementException("Missing FPGA hardware",e);
        }
    }

    /**
     * Create a new time system that uses the FPGA clock if it is available, or the {@link #system() JRE's time system} if not.
     */
    public static Clock fpgaOrSystem() {
        try {
            return fpga();
        } catch ( StrongbackRequirementException e ) {
            return system();
        }
    }

    /**
     * Create a new time system that uses the JRE's System clock.
     */
    public static Clock system() {
        return new Clock() {
            @Override
            public long currentTimeInMicros() {
                return (long)(currentTimeInNanos() / 1000.0);
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
