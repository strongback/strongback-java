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

import org.strongback.annotation.Experimental;
import org.strongback.components.TalonSRX;

/**
 * A hardware-based Proportional Integral Differential (PID) controller that runs on the Talon SRX motor controller. The
 * controller's input is wired as a sensor on the Talon SRX device, and specified via the
 * {@link #setFeedbackDevice(FeedbackDevice)} method. The controller's mode of control is set via the
 * {@link #setControlMode(ControlMode)}.
 * <h2>Important usage notes</h2>
 * <p>
 * This section outlines some useful and frequently used features of the TalonController.
 * <h3>Ramp rate</h3>
 * <p>
 * The Talon SRX can be set to honor a ramp rate to prevent instantaneous changes in throttle. This ramp rate is in effect
 * regardless of which mode is selected (throttle, slave, or closed-loop).
 *
 * <pre>
 * controller.setVoltageRampRate(6.0); // Allow throttle changes up to 6.0V per second
 * </pre>
 *
 * <h3>Feedback device</h3>
 * <p>
 * The analog and quadrature signals are available all the time in all modes (throttle, slave, or closed-loop), but the Talon
 * SRX requires the robot application to "pick" a particular feedback device for soft limit and closed-loop features. The Talon
 * SRX defaults to the {@link org.strongback.components.TalonSRX.FeedbackDevice#QUADRATURE_ENCODER quadrature encoder}.
 *
 * <pre>
 * controller.setFeedbackDevice(FeedbackDevice.ANALOG_ENCODER);
 * </pre>
 *
 * <p>
 * In order for limit switches and closed-loop features to function correctly, the sensor and motor have to be “in-phase”. This
 * means that the sensor position must move in a <i>positive</i> direction as the motor controller drives <i>positive</i>
 * throttle. To test this, first drive the motor manually (using a human input device), and watch the sensor position either in
 * the roboRIO Web-based Configuration Self-Test, or by calling {@link #getSelectedSensor()} and printing it to console. If the
 * "Sensor Position" moves in a negative direction while Talon SRX throttle is positive (blinking green), then use the
 * {@link #reverseSensor(boolean)} so that the Talon negates the sensor reading. Then re-test to confirm sensed position moves
 * in a positive direction with positive motor drive.
 * <p>
 * In the special case of using the {@link org.strongback.components.TalonSRX.FeedbackDevice#ENCODER_RISING} feedback device,
 * {@link #reverseSensor(boolean)} should be called with <code>false</code>, since a rising encoder is guaranteed to be positive
 * since it increments per rising edge, and never decrements.
 *
 * <h3>Soft limits</h3>
 * <p>
 * Soft limits can be used to disable motor drive when the position value read from the feedback device is outside of a
 * specified range. Forward throttle will be disabled when {@link #enableForwardSoftLimit(boolean)} is set to <code>true</code>
 * and the sensor position is greater than the {@link #setForwardSoftLimit(int)} value. Reverse throttle will be disabled when
 * {@link #enableReverseSoftLimit(boolean)} is set to <code>true</code> and the sensor position is less than the
 * {@link #setReverseSoftLimit(int)}.
 *
 * <h3>Control modes</h3>
 * <p>
 * The closed-loop logic is the same regardless of which feedback sensor or closed-loop mode is selected.
 *
 *
 * <h3>Follower mode</h3>
 * <p>
 * Any Talon SRX on CAN bus can be instructed to "follow" the drive output of another Talon SRX. This is done by putting a Talon
 * SRX into "follower" mode and specifying the device ID of the "lead Talon". The follower Talon will then mirror the output of
 * the leader, which can be in any mode: closed-loop, voltage percent (duty-cycle), or even following yet another Talon SRX.
 *
 * <pre>
 * leaderController = ...
 * followerController.setControlMode(ControlMode.FOLLOWER);
 * followerController.withTarget(leaderController.getDeviceID());
 * </pre>
 *
 * The {@link #reverseOutput(boolean)} method can be used to invert the output of a follower Talon. This may be useful if a
 * follower and leader Talon are wired out of phase with each other.
 * <p>
 * This class is currently experimental. It certainly works as a simple motor, but most of this interface exposes functionality
 * of the Talon SRX motor controller, including various input sensors. Little beyond setting and reading the speed has been
 * tested.
 */
@Experimental
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
        POSITION(1), /**
                      * Control the motor's speed in position change every 10 milliseconds.
                      */
        SPEED(2), /**
                   * Control the motor by directly setting the current to be sent to the motor.
                   */
        CURRENT(3), /**
                     * Control the motor by directly setting the voltage to be sent to the motor.
                     */
        VOLTAGE(4), /**
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

    @Override
    public TalonController setFeedbackDevice(FeedbackDevice device);

    /**
     * Get the current target angle for this controller as defined by the selected input sensor in degrees.
     * @return the target angle in degrees as defined by the selected input sensor
     * @see #withTarget(double)
     */
    @Override
    public double getTarget();

    /**
     * Sets the target angle for the controller's selected input sensor in degrees.
     *
     * @param angleInDegrees the desired angle in degrees that this controller will use as a target as defined by the selected
     *        input sensor
     * @return this object so that methods can be chained; never null
     * @see #getTarget()
     */
    @Override
    public TalonController withTarget(double angleInDegrees);

    @Override
    public TalonController setStatusFrameRate(StatusFrameRate frameRate, int periodMillis);

    @Override
    public TalonController reverseSensor(boolean flip);

    /**
     * Flips the sign (multiplies by negative one) the throttle values going into the motor on the talon in closed loop modes.
     *
     * @param flip <code>true</code> if motor output should be flipped; or <code>false</code> if not.
     * @return this object so that methods can be chained; never null
     */
    public TalonController reverseOutput(boolean flip);

    @Override
    public TalonController enableForwardSoftLimit(boolean enable);

    @Override
    public TalonController setForwardSoftLimit(int forwardLimitInDegrees);

    @Override
    public TalonController setReverseSoftLimit(int reverseLimitInDegrees);

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
    public TalonController withProfile(int profile, double p, double i, double d, double feedForward, int izone,
            double closeLoopRampRate);

    @Override
    public Gains getGainsForCurrentProfile();

    @Override
    public TalonController setVoltageRampRate(double rampRate);

    @Override
    public TalonController clearStickyFaults();

    @Override
    public TalonController setSafetyEnabled(boolean enabled);

    @Override
    public TalonController setExpiration(double timeout);
}
