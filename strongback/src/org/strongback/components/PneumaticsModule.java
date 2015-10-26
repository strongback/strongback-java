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

/**
 * A simple abstraction for the Pneumatics Control Module (PCM).
 */
public interface PneumaticsModule {

    /**
     * Gets the sensor that reports the current draw of the compressor.
     *
     * @return the current sensor for the compressor; never null
     */
    public CurrentSensor compressorCurrent();

    /**
     * Get the switch that reports whether the compressor is running.
     *
     * @return the compressor switch; never null
     */
    public Switch compressorRunningSwitch();

    /**
     * Get the switch that reports whether the pressure is below the lower threshold.
     *
     * @return the low pressure switch; never null
     */
    public Switch lowPressureSwitch();

    /**
     * Get the relay that controls whether the module automatically runs the compressor when the {@link #lowPressureSwitch()} is
     * triggered and shuts if off when the switch is untriggered (i.e., the maximum pressure threshold is reached).
     *
     * @return the automatic mode relay; never null
     */
    public Relay automaticMode();

    /**
     * Get the faults currently associated with the compressor. These state of these faults may change at any time based upon
     * the state of the module, and so they cannot be manually cleared.
     *
     * @return the current faults; never null
     * @see #compressorStickyFaults()
     */
    public Faults compressorFaults();

    /**
     * Get the sticky faults associated with the compressor. Once these faults are triggered, they are only reset with
     * #clearStickyFaults
     *
     * @return the sticky faults; never null
     * @see #compressorFaults()
     * @see #clearStickyFaults()
     */
    public Faults compressorStickyFaults();

    /**
     * Clear all {@link #compressorStickyFaults() sticky compressor faults} that may have been triggered.
     * @return this instance so that methods can be chained; never null
     */
    public PneumaticsModule clearStickyFaults();

    /**
     * The set of possible faults that this module can trigger.
     */
    public static interface Faults {

        /**
         * The switch that is {@link Switch#isTriggered() triggered} when the compressor is not connected.
         * @return the switch; never null
         */
        Switch notConnected();

        /**
         * The switch that is {@link Switch#isTriggered() triggered} when the compressor current is too high.
         * @return the switch; never null
         */
        Switch currentTooHigh();

        /**
         * The switch that is {@link Switch#isTriggered() triggered} when the compressor is not running because it is shorted.
         * @return the switch; never null
         */
        Switch shorted();
    }
}