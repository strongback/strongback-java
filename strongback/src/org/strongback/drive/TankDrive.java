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

package org.strongback.drive;

import org.strongback.command.Command;
import org.strongback.command.Requirable;
import org.strongback.components.Motor;
import org.strongback.components.Relay;
import org.strongback.components.Relay.State;
import org.strongback.function.DoubleToDoubleFunction;
import org.strongback.util.Values;

/**
 * Control logic for a tank/skid-style drive system. This controller provides {@link #arcade(double, double) arcade-style},
 * {@link #tank(double, double) tank-style}, and {@link #cheesy(double, double, boolean) cheesy-style} inputs.
 * <p>
 * This drive implements {@link Requirable} so that {@link Command}s can use it directly when {@link Command#execute()
 * executing}.
 *
 * @author Randall Hauch
 */
public class TankDrive implements Requirable {

    public static final double DEFAULT_MINIMUM_SPEED = 0.02;
    public static final double DEFAULT_MAXIMUM_SPEED = 1.0;
    public static final DoubleToDoubleFunction DEFAULT_SPEED_LIMITER = Values.symmetricLimiter(DEFAULT_MINIMUM_SPEED,
                                                                                               DEFAULT_MAXIMUM_SPEED);

    private static final double SENSITIVITY_HIGH = 0.75;
    private static final double SENSITIVITY_LOW = 0.75;
    private static final double HALF_PI = Math.PI / 2.0;

    private final Motor left;
    private final Motor right;
    private final Relay highGear;
    private final DoubleToDoubleFunction speedLimiter;

    private double quickStopAccumulator = 0.0;
    private double oldWheel = 0.0;

    /**
     * Creates a new DriveSystem subsystem that uses the supplied drive train and no shifter. The voltage send to the drive
     * train is limited to [-1.0,1.0].
     *
     * @param left the left motor on the drive train for the robot; may not be null
     * @param right the right motor on the drive train for the robot; may not be null
     */
    public TankDrive(Motor left, Motor right) {
        this(left, right, null, null);
    }

    /**
     * Creates a new DriveSystem subsystem that uses the supplied drive train and optional shifter. The voltage send to the
     * drive train is limited to [-1.0,1.0].
     *
     * @param left the left motor on the drive train for the robot; may not be null
     * @param right the right motor on the drive train for the robot; may not be null
     * @param shifter the optional shifter used to put the transmission into high gear; may be null if there is no shifter
     */
    public TankDrive(Motor left, Motor right, Relay shifter) {
        this(left, right, shifter, null);
    }

    /**
     * Creates a new DriveSystem subsystem that uses the supplied drive train and optional shifter. The voltage send to the
     * drive train is limited by the given function.
     *
     * @param left the left motor on the drive train for the robot; may not be null
     * @param right the right motor on the drive train for the robot; may not be null
     * @param shifter the optional shifter used to put the transmission into high gear; may be null
     * @param speedLimiter the function that limits the speed sent to the drive train; if null, then a default clamping function
     *        is used to limit to the range [-1.0,1.0]
     */
    public TankDrive(Motor left, Motor right, Relay shifter, DoubleToDoubleFunction speedLimiter) {
        this.left = left;
        this.right = right;
        this.highGear = shifter != null ? shifter : Relay.fixed(State.OFF);
        this.speedLimiter = speedLimiter != null ? speedLimiter : DEFAULT_SPEED_LIMITER;
    }

    /**
     * Shift the transmission into high gear. This method does nothing if the drive train has no transmission shifter.
     */
    public void highGear() {
        this.highGear.on();
    }

    /**
     * Shift the transmission into low gear. This method does nothing if the drive train has no transmission shifter.
     */
    public void lowGear() {
        this.highGear.off();
    }

    /**
     * Stop the drive train. This sets the left and right motor speeds to 0, and shifts to low gear.
     */
    public void stop() {
        this.left.stop();
        this.right.stop();
        this.highGear.off();
    }

    /**
     * Arcade drive implements single stick driving. This function lets you directly provide joystick values from any source.
     *
     * @param driveSpeed the value to use for forwards/backwards; must be -1 to 1, inclusive
     * @param turnSpeed the value to use for the rotate right/left; must be -1 to 1, inclusive
     */
    public void arcade(double driveSpeed, double turnSpeed) {
        arcade(driveSpeed, turnSpeed, true);
    }

    /**
     * Arcade drive implements single stick driving. This function lets you directly provide joystick values from any source.
     *
     * @param driveSpeed the value to use for forwards/backwards; must be -1 to 1, inclusive
     * @param turnSpeed the value to use for the rotate right/left; must be -1 to 1, inclusive Negative values turn right;
     *        positive values turn left.
     * @param squaredInputs if set, decreases the sensitivity at low speeds
     */
    public void arcade(double driveSpeed, double turnSpeed, boolean squaredInputs) {
        double leftMotorSpeed;
        double rightMotorSpeed;

        driveSpeed = speedLimiter.applyAsDouble(driveSpeed);
        turnSpeed = speedLimiter.applyAsDouble(turnSpeed);

        if (squaredInputs) {
            // square the inputs (while preserving the sign) to increase fine control while permitting full power
            if (driveSpeed >= 0.0) {
                driveSpeed = (driveSpeed * driveSpeed);
            } else {
                driveSpeed = -(driveSpeed * driveSpeed);
            }
            if (turnSpeed >= 0.0) {
                turnSpeed = (turnSpeed * turnSpeed);
            } else {
                turnSpeed = -(turnSpeed * turnSpeed);
            }
        }

        if (driveSpeed > 0.0) {
            if (turnSpeed > 0.0) {
                leftMotorSpeed = driveSpeed - turnSpeed;
                rightMotorSpeed = Math.max(driveSpeed, turnSpeed);
            } else {
                leftMotorSpeed = Math.max(driveSpeed, -turnSpeed);
                rightMotorSpeed = driveSpeed + turnSpeed;
            }
        } else {
            if (turnSpeed > 0.0) {
                leftMotorSpeed = -Math.max(-driveSpeed, turnSpeed);
                rightMotorSpeed = driveSpeed + turnSpeed;
            } else {
                leftMotorSpeed = driveSpeed - turnSpeed;
                rightMotorSpeed = -Math.max(-driveSpeed, -turnSpeed);
            }
        }

        left.setSpeed(leftMotorSpeed);
        right.setSpeed(rightMotorSpeed);
    }

    /**
     * Provide tank steering using the stored robot configuration. This function lets you directly provide joystick values from
     * any source.
     *
     * @param leftSpeed The value of the left stick; must be -1 to 1, inclusive
     * @param rightSpeed The value of the right stick; must be -1 to 1, inclusive
     * @param squaredInputs Setting this parameter to true decreases the sensitivity at lower speeds
     */
    public void tank(double leftSpeed, double rightSpeed, boolean squaredInputs) {
        // square the inputs (while preserving the sign) to increase fine control while permitting full power
        leftSpeed = speedLimiter.applyAsDouble(leftSpeed);
        rightSpeed = speedLimiter.applyAsDouble(rightSpeed);
        if (squaredInputs) {
            if (leftSpeed >= 0.0) {
                leftSpeed = (leftSpeed * leftSpeed);
            } else {
                leftSpeed = -(leftSpeed * leftSpeed);
            }
            if (rightSpeed >= 0.0) {
                rightSpeed = (rightSpeed * rightSpeed);
            } else {
                rightSpeed = -(rightSpeed * rightSpeed);
            }
        }
        left.setSpeed(leftSpeed);
        right.setSpeed(rightSpeed);
    }

    /**
     * Provide tank steering using the stored robot configuration. This function lets you directly provide joystick values from
     * any source.
     *
     * @param leftSpeed The value of the left stick; must be -1 to 1, inclusive
     * @param rightSpeed The value of the right stick; must be -1 to 1, inclusive
     */
    public void tank(double leftSpeed, double rightSpeed) {
        leftSpeed = speedLimiter.applyAsDouble(leftSpeed);
        rightSpeed = speedLimiter.applyAsDouble(rightSpeed);
        left.setSpeed(leftSpeed);
        right.setSpeed(rightSpeed);
    }

    /**
     * Used in the {@link #cheesy(double, double, boolean) cheesy drive} logic, this function dampens the supplied input at
     * low-to-mid input values.
     *
     * @param wheel the input value of the steering wheel; must be -1 to 1, inclusive
     * @param wheelNonLinearity the non-linearity factor
     * @return the dampened output
     */
    private static double dampen(double wheel, double wheelNonLinearity) {
        double factor = HALF_PI * wheelNonLinearity;
        return Math.sin(factor * wheel) / Math.sin(factor);
    }

    /**
     * Provide "cheesy drive" steering using a steering wheel and throttle. This function lets you directly provide joystick
     * values from any source.
     *
     * @param throttle the value of the throttle; must be -1 to 1, inclusive
     * @param wheel the value of the steering wheel; must be -1 to 1, inclusive Negative values turn right; positive values turn
     *        left.
     * @param isQuickTurn true if the quick-turn button is pressed
     * @see <a href="https://github.com/Team254/FRC-2014/blob/master/src/com/team254/frc2014/CheesyDriveHelper.java">Team 254
     *      Cheesy Drive logic</a>
     */
    public void cheesy(double throttle, double wheel, boolean isQuickTurn) {

        wheel = speedLimiter.applyAsDouble(wheel);
        throttle = speedLimiter.applyAsDouble(throttle);

        double negInertia = wheel - oldWheel;
        oldWheel = wheel;

        double wheelNonLinearity = 0.6;
        final boolean isHighGear = highGear.isOn();
        if (isHighGear) {
            wheelNonLinearity = 0.6;
            // Apply a sin function that's scaled to make it feel better.
            wheel = dampen(wheel, wheelNonLinearity);
            wheel = dampen(wheel, wheelNonLinearity);
        } else {
            wheelNonLinearity = 0.5;
            // Apply a sin function that's scaled to make it feel better.
            wheel = dampen(wheel, wheelNonLinearity);
            wheel = dampen(wheel, wheelNonLinearity);
            wheel = dampen(wheel, wheelNonLinearity);
        }

        double leftPwm, rightPwm, overPower;
        double sensitivity;

        double angularPower;
        double linearPower;

        // Negative inertia!
        double negInertiaAccumulator = 0.0;
        double negInertiaScalar;
        if (isHighGear) {
            sensitivity = SENSITIVITY_HIGH;
            negInertiaScalar = 5.0;
        } else {
            sensitivity = SENSITIVITY_LOW;
            if (wheel * negInertia > 0) {
                negInertiaScalar = 2.5;
            } else {
                if (Math.abs(wheel) > 0.65) {
                    negInertiaScalar = 5.0;
                } else {
                    negInertiaScalar = 3.0;
                }
            }
        }
        double negInertiaPower = negInertia * negInertiaScalar;
        negInertiaAccumulator += negInertiaPower;

        wheel = wheel + negInertiaAccumulator;
        if (negInertiaAccumulator > 1) {
            negInertiaAccumulator -= 1;
        } else if (negInertiaAccumulator < -1) {
            negInertiaAccumulator += 1;
        } else {
            negInertiaAccumulator = 0;
        }
        linearPower = throttle;

        // Quick turn!
        if (isQuickTurn) {
            if (Math.abs(linearPower) < 0.2) {
                double alpha = 0.1;
                quickStopAccumulator = (1 - alpha) * quickStopAccumulator + alpha * Values.symmetricLimit(0.0, wheel, 1.0) * 5;
            }
            overPower = 1.0;
            if (isHighGear) {
                sensitivity = 1.0;
            } else {
                sensitivity = 1.0;
            }
            angularPower = wheel;
        } else {
            overPower = 0.0;
            angularPower = Math.abs(throttle) * wheel * sensitivity - quickStopAccumulator;
            if (quickStopAccumulator > 1) {
                quickStopAccumulator -= 1;
            } else if (quickStopAccumulator < -1) {
                quickStopAccumulator += 1;
            } else {
                quickStopAccumulator = 0.0;
            }
        }

        rightPwm = leftPwm = linearPower;
        leftPwm += angularPower;
        rightPwm -= angularPower;

        if (leftPwm > 1.0) {
            rightPwm -= overPower * (leftPwm - 1.0);
            leftPwm = 1.0;
        } else if (rightPwm > 1.0) {
            leftPwm -= overPower * (rightPwm - 1.0);
            rightPwm = 1.0;
        } else if (leftPwm < -1.0) {
            rightPwm += overPower * (-1.0 - leftPwm);
            leftPwm = -1.0;
        } else if (rightPwm < -1.0) {
            leftPwm += overPower * (-1.0 - rightPwm);
            rightPwm = -1.0;
        }
        left.setSpeed(leftPwm);
        right.setSpeed(rightPwm);
    }

}
