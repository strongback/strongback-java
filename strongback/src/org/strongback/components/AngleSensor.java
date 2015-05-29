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

    @Override
    default public AngleSensor zero() {
        return this;
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