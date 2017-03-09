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

import java.util.function.DoubleSupplier;

import org.strongback.annotation.ThreadSafe;

/**
 * A sensor that returns an angle. The sensor's can be {@link #zero() zeroed} to reset the angle at which the sensor considers 0
 * degrees.
 */
@ThreadSafe
@FunctionalInterface
public interface AngleSensor extends Zeroable {

    /**
     * Gets the angular displacement in continuous degrees.
     *
     * @return the positive or negative angular displacement
     */
    public double getAngle();

    /**
     * Change the output so that the current angle is considered to be 0.
     *
     * @return this object to allow chaining of methods; never null
     */
    @Override
    default public AngleSensor zero() {
        return this;
    }

    /**
     * Compute the change in angle between the {@link #getAngle() current angle} and the target angle, using the given tolerance
     * for the difference. The result is the angle that this sensor must rotate to reach the target angle, and may be positive
     * or negative
     *
     * @param targetAngle the target angle
     * @param tolerance the allowed tolerance in degrees between the two angles
     * @return the positive or negative angular displacement required for this sensor to reach the target angle, or 0.0d if the
     *         two angles are already within the specified {@code tolerance}
     */
    default public double computeAngleChangeTo(double targetAngle, double tolerance) {
        double diff = targetAngle - this.getAngle();
        return Math.abs(diff) <= Math.abs(tolerance) ? 0.0 : diff;
    }

    /**
     * Create an angle sensor around the given function that returns the angle.
     *
     * @param angleSupplier the function that returns the angle; may not be null
     * @return the angle sensor
     */
    public static AngleSensor create(DoubleSupplier angleSupplier) {
        return new AngleSensor() {
            private volatile double zero = 0;

            @Override
            public double getAngle() {
                return angleSupplier.getAsDouble() - zero;
            }

            @Override
            public AngleSensor zero() {
                zero = angleSupplier.getAsDouble();
                return this;
            }
        };
    }

    /**
     * Inverts the specified {@link AngleSensor} so that negative angles become positive angles.
     *
     * @param sensor the {@link AngleSensor} to invert
     * @return an {@link AngleSensor} that reads the opposite of the original sensor
     */
    public static AngleSensor invert(AngleSensor sensor) {
        return new AngleSensor() {
            @Override
            public double getAngle() {
                double angle = sensor.getAngle();
                return angle == 0.0 ? 0.0 : -angle;
            }

            @Override
            public AngleSensor zero() {
                return sensor.zero();
            }
        };
    }

}