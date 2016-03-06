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

import org.strongback.annotation.Experimental;

/**
 * A motor controlled by a Talon SRX with built-in current sensor, position (angle) sensor, and optional external limit switches
 * wired into the SRX so that it can automatically stop the forward and reverse directions when the limit switches are
 * triggered.
 * <p>
 * This class is currently experimental. It certainly works as a simple motor, but most of this interface exposes functionality
 * of the Talon SRX motor controller, including various input sensors. Little beyond setting and reading the speed has been
 * tested.
 */
@Experimental
public interface TalonSRX extends LimitedMotor {

    @Override
    public TalonSRX setSpeed(double speed);

    /**
     * Get the CAN device ID.
     *
     * @return the device ID.
     */
    public int getDeviceID();

    /**
     * <b>Deprecated.</b> Use {@link #getEncoderInput()} instead.
     * <p>
     * Get the angle sensor (encoder) hooked up to the Talon SRX motor controller.
     *
     * @return the angle sensor; never null, but if not hooked up the sensor will always return a meaningless value
     * @deprecated Use {@link #getEncoderInput()} instead.
     */
    @Deprecated
    public default AngleSensor getAngleSensor() {
        return getEncoderInput();
    }

    /**
     * Get the current encoder angle and rate, regardless of whether it is the current feedback device.
     *
     * @return the gyroscope that reads the encoder sensor; or null if the motor was created with no quadrature encoder input
     * @see org.strongback.hardware.Hardware.Motors#talonSRX(int)
     * @see org.strongback.hardware.Hardware.Motors#talonSRX(int, double)
     * @see org.strongback.hardware.Hardware.Motors#talonSRX(int, double, double)
     */
    public Gyroscope getEncoderInput();

    /**
     * Get the current analog angle and rate, regardless of whether it is the current feedback device.
     *
     * @return the gyroscope that reads the 3.3V analog sensor; or null if the motor was created with no analog input
     * @see org.strongback.hardware.Hardware.Motors#talonSRX(int)
     * @see org.strongback.hardware.Hardware.Motors#talonSRX(int, double)
     * @see org.strongback.hardware.Hardware.Motors#talonSRX(int, double, double)
     */
    public Gyroscope getAnalogInput();

    /**
     * Get the input angle and rate of the current {@link #setFeedbackDevice(FeedbackDevice) feedback device}.
     *
     * @return the selected input device sensor; never null, but it may return a meaningless value if a sensor is not physically
     *         wired as an input to the Talon device
     */
    public Gyroscope getSelectedSensor();

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
     * Set the feedback device for this controller.
     *
     * @param device the feedback device; may not be null
     * @return this object so that methods can be chained; never null
     * @see #reverseSensor(boolean)
     */
    public TalonSRX setFeedbackDevice(FeedbackDevice device);

    /**
     * Set the status frame rate for this controller.
     *
     * @param frameRate the status frame rate; may not be null
     * @param periodMillis frame rate period in milliseconds
     * @return this object so that methods can be chained; never null
     */
    public TalonSRX setStatusFrameRate(StatusFrameRate frameRate, int periodMillis);

    /**
     * Flips the sign (multiplies by negative one) the {@link #setFeedbackDevice(FeedbackDevice) feedback device} values read by
     * the Talon.
     * <p>
     * This only affects position and velocity closed loop control. Allows for situations where you may have a sensor flipped
     * and going in the wrong direction.
     *
     * @param flip <code>true</code> if sensor input should be flipped, or <code>false</code> if not.
     * @return this object so that methods can be chained; never null
     */
    public TalonSRX reverseSensor(boolean flip);

    /**
     * Set the soft limit for the forward throttle, in terms of the angle as measured by the {@link #getSelectedSensor()
     * selected input sensor}. Soft limits can be used to disable motor drive when the "Sensor Position" is outside of a
     * specified range: forward throttle will be disabled if the "Sensor Position" is greater than the forward soft limit.
     * This takes effect only when the forward soft limit is {@link #enableForwardSoftLimit(boolean)}.
     * <p>
     * Soft limits can be used to disable motor drive when the “Sensor Position” is outside of a specified range. Forward
     * throttle will be disabled if the “Sensor Position” is greater than the Forward Soft Limit. Reverse throttle will be
     * disabled if the “Sensor Position” is less than the Reverse Soft Limit. The respective Soft Limit Enable must be enabled
     * for this feature to take effect.
     *
     * @param forwardLimitInDegrees the angle at which the forward throttle should be disabled, where the angle in terms of the
     *        {@link #getSelectedSensor()}
     * @return this object so that methods can be chained; never null
     * @see #enableForwardSoftLimit(boolean)
     */
    public TalonSRX setForwardSoftLimit(int forwardLimitInDegrees);

    /**
     * Set the soft limit for the reverse throttle, in terms of the angle as measured by the {@link #getSelectedSensor()
     * selected input sensor}. Soft limits can be used to disable motor drive when the "Sensor Position" it outside of a
     * specified range: reverse throttle will be disabled if the "Sensor Position" is less than the reverse soft limit.
     * This takes effect only when the reverse soft limit is {@link #enableReverseSoftLimit(boolean)}.
     * <p>
     * Soft limits can be used to disable motor drive when the “Sensor Position” is outside of a specified range. Forward
     * throttle will be disabled if the “Sensor Position” is greater than the Forward Soft Limit. Reverse throttle will be
     * disabled if the “Sensor Position” is less than the Reverse Soft Limit. The respective Soft Limit Enable must be enabled
     * for this feature to take effect.
     *
     * @param reverseLimitInDegrees the angle at which the reverse throttle should be disabled, where the angle in terms of the
     *        {@link #getSelectedSensor()}
     * @return this object so that methods can be chained; never null
     * @see #enableForwardSoftLimit(boolean)
     */
    public TalonSRX setReverseSoftLimit(int reverseLimitInDegrees);

    /**
     * Enable the soft limit for forward throttle, which is set via the {@link #setForwardSoftLimit(int)}.
     *
     * @param enable {@code true} if the forward throttle soft limit should be enabled
     * @return this object so that methods can be chained; never null
     * @see #setForwardLimitSwitchNormallyOpen(boolean)
     */
    public TalonSRX enableForwardSoftLimit(boolean enable);

    /**
     * Enable the soft limit for reverse throttle, which is set via the {@link #setReverseSoftLimit(int)}.
     *
     * @param enable {@code true} if the forward throttle soft limit should be enabled
     * @return this object so that methods can be chained; never null
     * @see #setReverseLimitSwitchNormallyOpen(boolean)
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
     * The Talon SRX can be set to honor a ramp rate to prevent instantaneous changes in throttle. This ramp rate is in effect
     * regardless of which mode is selected (throttle, slave, or closed-loop). For example, setting the ramp rate to
     * <code>6.0</code> will limit the maximum voltage change within any second to be less than or equal to 6.0 volts.
     *
     * @param rampRate maximum change in voltage per second, in volts / second
     * @return this object so that methods can be chained; never null
     */
    public TalonSRX setVoltageRampRate(double rampRate);

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
     * The type of feedback sensor used by this Talon controller.
     */
    public enum FeedbackDevice {
        /**
         * Use Quadrature Encoder.
         */
        QUADRATURE_ENCODER(0),

        /**
         * Analog potentiometer or any other analog device, 0-3.3V
         */
        ANALOG_POTENTIOMETER(2),

        /**
         * Analog encoder or any other analog device, 0-3.3V
         */
        ANALOG_ENCODER(3),

        /**
         * Encoder that increments position per rising edge (and never decrements) on Quadrature-A.
         */
        ENCODER_RISING(4),

        /**
         * Encoder that increments position per falling edge (and never decrements) on Quadrature-A. Note: Was not supported in
         * 2015 firmware, per the
         * <a href="https://www.ctr-electronics.com/Talon%20SRX%20Software%20Reference%20Manual.pdf">Talon SRX Software
         * Reference Manual</a>, section 21.3
         */
        ENCODER_FALLING(5);

        public int value;

        public static FeedbackDevice valueOf(int value) {
            for (FeedbackDevice mode : values()) {
                if (mode.value == value) {
                    return mode;
                }
            }
            return null;
        }

        private FeedbackDevice(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    /**
     * Types of status frame rates.
     */
    public enum StatusFrameRate {
        /**
         * The General Status frame has a default period of 10ms, and provides:
         * <ul>
         * <li>Closed Loop Error - the closed-loop target minus actual position/velocity.</li>
         * <li>Throttle - the current 10bit motor output duty cycle (-1023 full reverse to +1023 full forward).</li>
         * <li>Forward Limit Switch Pin State</li>
         * <li>Reverse Limit Switch Pin State</li>
         * <li>Fault bits</li>
         * <li>Applied Control Mode</li>
         * </ul>
         */
        GENERAL(0),

        /**
         * The Feedback Status frame has a default period of 20ms, and provides:
         * <ul>
         * <li>Sensor Position - position of the selected sensor</li>
         * <li>Sensor Velocity - velocity of the selected sensor</li>
         * <li>Motor Current</li>
         * <li>Sticky Faults</li>
         * <li>Brake Neutral State</li>
         * <li>Motor Control Profile Select</li>
         * </ul>
         */
        FEEDBACK(1),

        /**
         * The Quadrature Encoder Status frame has a default period of 100ms, and provides:
         * <ul>
         * <li>Encoder Position - position of the quadrature sensor</li>
         * <li>Encoder Velocity - velocity of the selected sensor</li>
         * <li>Number of rising edges counted on the Index Pin.</li>
         * <li>Quad A pin state.</li>
         * <li>Quad B pin state.</li>
         * <li>Quad Index pin state.</li>
         * </ul>
         * The quadrature decoder is always engaged, whether the feedback device is selected or not, and whether a quadrature
         * encoder is actually wired or not. This means that the Quadrature Encoder signals are always available in programming
         * API regardless of how the Talon is used. The 100ms update rate is sufficient for logging, instrumentation and
         * debugging. If a faster update rate is required the robot application can select the appropriate sensor and leverage
         * the Sensor Position and Sensor Velocity sent over the {@link #FEEDBACK} frame every 20ms (by default).
         */
        QUADRATURE_ENCODER(2),

        /**
         * The Analog, Temperature, and Battery Voltage status frame has a default period of 100ms, and provides:
         * <ul>
         * <li>Analog Position - position of the analog sensor</li>
         * <li>Analog Velocity - velocity of the analog sensor</li>
         * <li>Temperature</li>
         * <li>Battery Voltage</li>
         * </ul>
         * The Analog to Digital Converter is always engaged, whether the feedback device is selected or not, and whether an
         * analog sensor is actually wired or not. This means that the Analog In signals are always available in programming API
         * regardless of how the Talon is used. The 100ms update rate is sufficient for logging, instrumentation and debugging.
         * If a faster update rate is required the robot application can select the appropriate sensor and leverage the Sensor
         * Position and Sensor Velocity sent over the {@link #FEEDBACK} frame every 20ms (by default).
         */
        ANALOG_TEMPERATURE_BATTERY_VOLTAGE(3);
        public int value;

        public static StatusFrameRate valueOf(int value) {
            for (StatusFrameRate mode : values()) {
                if (mode.value == value) {
                    return mode;
                }
            }
            return null;
        }

        private StatusFrameRate(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

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