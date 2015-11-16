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

package org.strongback.control;

import org.strongback.components.TalonSRX;

/**
 * The Talon SRX Proportional Integral Differential (PID) controller with optional support for feed forward.
 */
public interface TalonController extends PIDController, TalonSRX {

    /**
     * A set of gains.
     */
    public static interface Gains extends PIDController.Gains {

        /**
         * Get the IZone value for the current profile of this controller. When IZone is used, the controller will automatically
         * clear the integral accumulated error if the closed loop error is outside the IZone.
         *
         * @return the IZone value
         */
        public double getIzone();

        /**
         * Get the closed loop ramp rate for the current profile of this controller.
         *
         * @return the feed forward gain
         */
        public double getCloseLoopRampRate();
    }

    /**
     * The type of control input used by this Talon controller.
     */
    public static enum ControlMode {
        /**
         * Directly control the motor by setting the speed as percent voltage, from -1.0 to 1.0 with 0.0 as stopped.
         */
        PERCENT_VBUS(0),
        /**
         * Control the motor by directly setting the desired position (in ticks or an analog value) for the motor's encoder.
         */
        POSITION(1),
        /**
         * Control the motor's speed in position change every 10 milliseconds.
         */
        SPEED(2),
        /**
         * Control the motor by directly setting the current to be sent to the motor.
         */
        CURRENT(3),
        /**
         * Control the motor by directly setting the voltage to be sent to the motor.
         */
        VOLTAGE(4),
        /**
         * Control the motor by following another Talon SRX device.
         */
        FOLLOWER(5);

        private final int value;

        public static ControlMode valueOf(int value) {
            for (ControlMode mode : values()) {
                if (mode.value == value) {
                    return mode;
                }
            }
            return null;
        }

        private ControlMode(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }

    /**
     * The type of control sensor used by this Talon controller.
     */
    public enum FeedbackDevice {
        /**
         * Use Quadrature Encoder.
         */
        QUADRATURE_ENCODER(0),
        /**
         * Analog potentiometer, 0-3.3V
         */
        ANALOG_POTENTIOMETER(2),
        /**
         * Analog encoder, 0-3.3V
         */
        ANALOG_ENCODER(3),
        /**
         * Encoder that increment position per rising edge on Quadrature-A.
         */
        ENCODER_RISING(4),
        /**
         * Encoder that increment position per falling edge on Quadrature-A.
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
        GENERAL(0), FEEDBACK(1), QUADRATURE_ENCODER(2), ANALOG_TEMP_VBAT(3);
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
     * Get the CAN device ID.
     *
     * @return the device ID.
     */
    public int getDeviceID();

    /**
     * Get this controller's current control mode.
     *
     * @return the control mode; never null
     */
    public ControlMode getControlMode();

    /**
     * Set the control mode for this controller.
     *
     * @param mode the control mode; may not be null
     * @return this object so that methods can be chained; never null
     */
    public TalonController setControlMode(ControlMode mode);

    /**
     * Set the feedback device for this controller.
     *
     * @param device the feedback device; may not be null
     * @return this object so that methods can be chained; never null
     */
    public TalonController setFeedbackDevice(FeedbackDevice device);

    /**
     * Set the status frame rate for this controller.
     *
     * @param frameRate the status frame rate; may not be null
     * @param periodMillis frame rate period in milliseconds
     * @return this object so that methods can be chained; never null
     */
    public TalonController setStatusFrameRate(StatusFrameRate frameRate, int periodMillis);

    /**
     * Flips the sign (multiplies by negative one) the sensor values going into the talon.
     *
     * This only affects position and velocity closed loop control. Allows for situations where you may have a sensor flipped
     * and going in the wrong direction.
     *
     * @param flip <code>true</code> if sensor input should be flipped, or <code>false</code> if not.
     * @return this object so that methods can be chained; never null
     */
    public TalonController reverseSensor(boolean flip);

    /**
     * Flips the sign (multiplies by negative one) the throttle values going into the motor on the talon in closed loop modes.
     *
     * @param flip <code>true</code> if motor output should be flipped; or <code>false</code> if not.
     * @return this object so that methods can be chained; never null
     */
    public TalonController reverseOutput(boolean flip);

    @Override
    public TalonController setForwardSoftLimit(int forwardLimit);

    @Override
    public TalonController enableForwardSoftLimit(boolean enable);

    @Override
    public TalonController setReverseSoftLimit(int reverseLimit);

    @Override
    public TalonController enableReverseSoftLimit(boolean enable);

    @Override
    public TalonController enableLimitSwitch(boolean forward, boolean reverse);

    @Override
    public TalonController setForwardLimitSwitchNormallyOpen(boolean normallyOpen);

    @Override
    public TalonController setReverseLimitSwitchNormallyOpen(boolean normallyOpen);

    @Override
    public TalonController enableBrakeMode(boolean brake);

    @Override
    public TalonController withGains(double p, double i, double d);

    @Override
    public TalonController withGains(double p, double i, double d, double feedForward);

    /**
     * Set the gains for the current profile of this controller.
     *
     * @param p the proportional gain
     * @param i the integral gain
     * @param d the differential gain
     * @param feedForward the feed-forward gain
     * @param izone Integration zone -- prevents accumulation of integration error with large errors. Setting this to zero will
     *        ignore any izone stuff.
     * @param closeLoopRampRate Closed loop ramp rate. Maximum change in voltage, in volts / sec.
     * @return this object so that methods can be chained; never null
     * @see #useProfile(int)
     */
    public TalonController withGains(double p, double i, double d, double feedForward, int izone, double closeLoopRampRate);

    @Override
    public TalonController withProfile(int profile, double p, double i, double d);

    @Override
    public TalonController withProfile(int profile, double p, double i, double d, double feedForward);

    /**
     * Set the gains for the specified profile of this controller.
     *
     * @param profile which profile to set the PID constants for. You can have two profiles, with values of 0 or 1, allowing you
     *        to keep a second set of values on hand in the Talon. In order to switch profiles without recalling setPID, you
     *        must call setProfile().
     * @param p the proportional gain
     * @param i the integral gain
     * @param d the differential gain
     * @param feedForward the feed-forward gain
     * @param izone Integration zone -- prevents accumulation of integration error with large errors. Setting this to zero will
     *        ignore any izone stuff.
     * @param closeLoopRampRate Closed loop ramp rate. Maximum change in voltage, in volts / sec.
     * @return this object so that methods can be chained; never null
     * @see #useProfile(int)
     */
    public TalonController withProfile(int profile, double p, double i, double d, double feedForward, int izone, double closeLoopRampRate);

    @Override
    public Gains getGainsForCurrentProfile();

    /**
     * Set the voltage ramp rate for the current profile of this controller. It limits the rate at which the throttle will
     * change. Affects all modes.
     *
     * @param rampRate Maximum change in voltage, in volts / second
     * @return this object so that methods can be chained; never null
     */
    public TalonController setVoltageRampRate(double rampRate);

    @Override
    public TalonController clearStickyFaults();

    @Override
    public TalonController setSafetyEnabled(boolean enabled);

    @Override
    public TalonController setExpiration(double timeout);
}
