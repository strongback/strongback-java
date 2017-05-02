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

import org.strongback.annotation.ThreadSafe;
import org.strongback.command.Requirable;
import org.strongback.drive.TankDrive;
import org.strongback.util.Values;

/**
 * A motor is a device that can be set to operate at a speed.
 *
 * @author Zach Anderson
 *
 */
@ThreadSafe
public interface Motor extends SpeedSensor, SpeedController, Stoppable, Requirable {

    public enum Direction {
        FORWARD, REVERSE, STOPPED
    }

    /**
     * Gets the current speed.
     *
     * @return the speed, will be between -1.0 and 1.0 inclusive
     */
    @Override
    public double getSpeed();

    /**
     * Sets the speed of this {@link Motor}.
     *
     * @param speed the new speed as a double, clamped to -1.0 to 1.0 inclusive
     * @return this object to allow chaining of methods; never null
     */
    @Override
    public Motor setSpeed(double speed);

    /**
     * Stops this {@link Motor}. Same as calling {@code setSpeed(0.0)}.
     */
    @Override
    default public void stop() {
        setSpeed(0.0);
    }

    /**
     * Create a new motor that inverts this motor.
     *
     * @return the new inverted motor; never null
     */
    default Motor invert() {
        return Motor.invert(this);
    }

    /**
     * Gets the current {@link Direction} of this {@link Motor}, can be {@code FORWARD}, {@code REVERSE}, or {@code STOPPED}.
     *
     * @return the {@link Direction} of this {@link Motor}
     */
    default public Direction getDirection() {
        int direction = Values.fuzzyCompare(getSpeed(), 0.0);
        if (direction < 0)
            return Direction.REVERSE;
        else if (direction > 0)
            return Direction.FORWARD;
        else
            return Direction.STOPPED;
    }

    /**
     * Create a new {@link Motor} instance that is actually composed of two other motors that will be controlled identically.
     * This is useful when multiple motors are controlled in the same way, such as on one side of a {@link TankDrive}.
     *
     * @param motor1 the first motor, and the motor from which the speed is read; may not be null
     * @param motor2 the second motor; may not be null
     * @return the composite motor; never null
     */
    static Motor compose(Motor motor1, Motor motor2) {
        return new Motor() {
            @Override
            public double getSpeed() {
                return motor1.getSpeed();
            }

            @Override
            public Motor setSpeed(double speed) {
                motor1.setSpeed(speed);
                motor2.setSpeed(speed);
                return this;
            }
        };
    }

    /**
     * Create a new {@link Motor} instance that is actually composed of three other motors that will be controlled identically.
     * This is useful when multiple motors are controlled in the same way, such as on one side of a {@link TankDrive}.
     *
     * @param motor1 the first motor, and the motor from which the speed is read; may not be null
     * @param motor2 the second motor; may not be null
     * @param motor3 the third motor; may not be null
     * @return the composite motor; never null
     */
    static Motor compose(Motor motor1, Motor motor2, Motor motor3) {
        return new Motor() {
            @Override
            public double getSpeed() {
                return motor1.getSpeed();
            }

            @Override
            public Motor setSpeed(double speed) {
                motor1.setSpeed(speed);
                motor2.setSpeed(speed);
                motor3.setSpeed(speed);
                return this;
            }
        };
    }

    /**
     * Create a new {@link Motor} instance that inverts the speed sent to and read from another motor. This is useful on
     * {@link TankDrive}, where all motors on one side are physically inverted compared to the motors on the other side.
     * <p>
     * For example:
     * <pre>
     *   Motor left = ...
     *   Motor right = ...
     *   DriveTrain drive = TankDrive.create(left, Motor.invert(right));
     * </pre> or <pre>
     *   Motor leftFront = ...
     *   Motor leftRear = ...
     *   Motor rightFront = ...
     *   Motor rightRear = ...
     *   Motor left = Motor.compose(leftFront,leftRear);
     *   Motor right = Motor.compose(rightFront,rightRear);
     *   DriveTrain drive = TankDrive.create(left, Motor.invert(right));
     * </pre>
     *
     * @param motor the motor to invert; may not be null
     * @return the inverted motor; never null
     */
    static Motor invert(Motor motor) {
        return new Motor() {
            @Override
            public Motor setSpeed(double speed) {
                motor.setSpeed(-1 * speed);
                return this;
            }

            @Override
            public double getSpeed() {
                return -1 * motor.getSpeed();
            }
        };
    }
}