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
 * A motor controlled by a Talon SRX with built-in current sensor, position (angle) sensor, and optional external limit switches
 * wired into the SRX so that it can automatically stop the forward and reverse directions when the limit switches are
 * triggered.
 */
public interface TalonSRX extends LimitedMotor {

    @Override
    public TalonSRX setSpeed(double speed);

    /**
     * Get the angle sensor (encoder) hooked up to the Talon SRX motor controller.
     *
     * @return the angle sensor; never null, but if not hooked up the sensor will always return a meaningless value
     */
    public AngleSensor getAngleSensor();

    /**
     * Get the Talon SRX's output current sensor.
     *
     * @return the output current sensor; never null
     */
    public CurrentSensor getCurrentSensor();

    /**
     * Get the Talon SRX's output voltage sensor.
     *
     * @return the output voltage sensor; never null
     */
    public VoltageSensor getVoltageSensor();

    /**
     * Get the Talon SRX's bus voltage.
     *
     * @return the bus voltage sensor; never null
     */
    public VoltageSensor getBusVoltageSensor();

    /**
     * Get the Talon SRX's temperature sensor.
     *
     * @return the temperature sensor; never null
     */
    public TemperatureSensor getTemperatureSensor();

    /**
     * Set the soft limit for forward motion, which will disable the motor when the sensor is out of range.
     *
     * @param forwardLimit the sensor limit beyond which the motor's forward direction should be halted.
     * @return this object so that methods can be chained; never null
     */
    public TalonSRX setForwardSoftLimit(int forwardLimit);

    /**
     * Enable the forward soft limit.
     *
     * @param enable <code>true</code> if the limit is to be enabled, or <code>false</code> otherwise
     * @return this object so that methods can be chained; never null
     */
    public TalonSRX enableForwardSoftLimit(boolean enable);

    /**
     * Set the soft limit for reverse motion, which will disable the motor when the sensor is out of range.
     *
     * @param reverseLimit the sensor limit beyond which the motor's reverse direction should be halted.
     * @return this object so that methods can be chained; never null
     */
    public TalonSRX setReverseSoftLimit(int reverseLimit);

    /**
     * Enable the reverse soft limit.
     *
     * @param enable <code>true</code> if the limit is to be enabled, or <code>false</code> otherwise
     * @return this object so that methods can be chained; never null
     */
    public TalonSRX enableReverseSoftLimit(boolean enable);

    /**
     * Enable the forward and reverse limit switches.
     *
     * @param forward <code>true</code> if the forward limit is to be enabled, or <code>false</code> otherwise
     * @param reverse <code>true</code> if the reverse limit is to be enabled, or <code>false</code> otherwise
     * @return this object so that methods can be chained; never null
     */
    public TalonSRX enableLimitSwitch(boolean forward, boolean reverse);

    /**
     * Configure the forward limit switch to be normally open or normally closed. Talon will disable momentarily if the Talon's
     * current setting is dissimilar to the caller's requested setting.
     *
     * Since Talon saves setting to flash this should only affect a given Talon initially during robot install.
     *
     * @param normallyOpen <code>true</code> for normally open, or <code>false</code> for normally closed.
     * @return this object so that methods can be chained; never null
     */
    public TalonSRX setForwardLimitSwitchNormallyOpen(boolean normallyOpen);

    /**
     * Configure the reverse limit switch to be normally open or normally closed. Talon will disable momentarily if the Talon's
     * current setting is dissimilar to the caller's requested setting.
     *
     * Since Talon saves setting to flash this should only affect a given Talon initially during robot install.
     *
     * @param normallyOpen <code>true</code> for normally open, or <code>false</code> for normally closed.
     * @return this object so that methods can be chained; never null
     */
    public TalonSRX setReverseLimitSwitchNormallyOpen(boolean normallyOpen);

    /**
     * Enable the brake mode.
     *
     * @param brake <code>true</code> if the brake mode is to be enabled, or <code>false</code> otherwise
     * @return this object so that methods can be chained; never null
     */
    public TalonSRX enableBrakeMode(boolean brake);

    /**
     * Get the faults currently associated with the Talon controller. These state of these faults may change at any time based
     * upon the state of the module, and so they cannot be manually cleared.
     *
     * @return the current faults; never null
     * @see #stickyFaults()
     */
    public Faults faults();

    /**
     * Get the sticky faults associated with the Talon controller. Once these faults are triggered, they are only reset with
     * #clearStickyFaults
     *
     * @return the sticky faults; never null
     * @see #faults()
     * @see #clearStickyFaults()
     */
    public Faults stickyFaults();

    /**
     * Clear all {@link #stickyFaults() sticky faults} that may have been triggered.
     *
     * @return this instance so that methods can be chained; never null
     */
    public TalonSRX clearStickyFaults();

    /**
     * Get the firmware version.
     *
     * @return the version of the firmware running on the Talon
     */
    public long getFirmwareVersion();

    /**
     * Determine whether this motor controller's safety mode is enabled.
     *
     * @return <code>true</code> if the motor will be automatically disabled if it is not used within the expiration time, or
     *         <code>false</code> otherwise
     */
    public boolean isSafetyEnabled();

    /**
     * Set whether this motor controller's safety mode is enabled.
     *
     * @param enabled <code>true</code> if the motor will be automatically disabled if it is not used within the expiration
     *        time, or <code>false</code> otherwise
     * @return this instance so that methods can be chained; never null
     */
    public TalonSRX setSafetyEnabled(boolean enabled);

    /**
     * Get the motor safety expiration time in milliseconds. When {@link #isSafetyEnabled() safety is enabled}, then the motor
     * will be automatically disabled if it is not used within the expiration time, which is typically 100ms. This can be
     * adjusted to a larger value when debugging the robot.
     *
     * @return the safety expiration time in milliseconds
     */
    public double getExpiration();

    /**
     * Set the motor safety expiration time in milliseconds. When {@link #isSafetyEnabled() safety is enabled}, then the motor
     * will be automatically disabled if it is not used within the expiration time, which is typically 100ms. This can be
     * adjusted to a larger value when debugging the robot.
     *
     * @param timeout the safety expiration time in milliseconds
     * @return this instance so that methods can be chained; never null
     */
    public TalonSRX setExpiration(double timeout);

    /**
     * Determine if this motor controller is alive or has been disabled because it has not been used within the
     * {@link #getExpiration() expiration}.
     *
     * @return <code>true</code> if the controller is alive, or <code>false</code> if it has been disabled after expiring.
     */
    public boolean isAlive();

    /**
     * The set of possible faults that this module can trigger.
     */
    public static interface Faults {

        /**
         * The switch that is {@link Switch#isTriggered() triggered} when the Talon's over-temperature fault is tripped.
         *
         * @return the switch; never null
         */
        Switch overTemperature();

        /**
         * The switch that is {@link Switch#isTriggered() triggered} when the voltage is too low.
         *
         * @return the switch; never null
         */
        Switch underVoltage();

        /**
         * The switch that is {@link Switch#isTriggered() triggered} when the forward limit switch fault is tripped.
         *
         * @return the switch; never null
         */
        Switch forwardLimitSwitch();

        /**
         * The switch that is {@link Switch#isTriggered() triggered} when the reverse limit switch fault is tripped.
         *
         * @return the switch; never null
         */
        Switch reverseLimitSwitch();

        /**
         * The switch that is {@link Switch#isTriggered() triggered} when the forward soft limit is tripped.
         *
         * @return the switch; never null
         */
        Switch forwardSoftLimit();

        /**
         * The switch that is {@link Switch#isTriggered() triggered} when the reverse soft limit is tripped.
         *
         * @return the switch; never null
         */
        Switch reverseSoftLimit();

        /**
         * The switch that is {@link Switch#isTriggered() triggered} when a hardware fault occurs.
         *
         * @return the switch; never null
         */
        Switch hardwareFailure();
    }
}