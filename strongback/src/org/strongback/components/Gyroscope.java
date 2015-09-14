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
 * A gyroscope is a device that measures angular velocity (in degrees per second) about a single axis. A gyroscope can
 * indirectly determine angular displacement by integrating velocity with respect to time, which is why it extends
 * {@link Compass}. Negative values are assumed to be counter-clockwise and positive values are clockwise.
 *
 * @author Zach Anderson
 *
 */
@ThreadSafe
public interface Gyroscope extends Compass {
    /**
     * Gets the rate of change in {@link #getAngle()} of this {@link Gyroscope} in degrees per second.
     *
     * @return the angular velocity of this {@link Gyroscope}
     */
    public double getRate();

    @Override
    default public Gyroscope zero() {
        return this;
    }

    /**
     * Create a gyroscope for the given functions that returns the angular displacement and velocity.
     *
     * @param angleSupplier the function that returns the angle; may not be null
     * @param rateSupplier the function that returns the angular acceleration; may not be null
     * @return the angle sensor
     */
    public static Gyroscope create(DoubleSupplier angleSupplier, DoubleSupplier rateSupplier) {
        return new Gyroscope() {
            private volatile double zero = 0;

            @Override
            public double getAngle() {
                return angleSupplier.getAsDouble() - zero;
            }

            @Override
            public Gyroscope zero() {
                zero = angleSupplier.getAsDouble();
                return this;
            }

            @Override
            public double getRate() {
                return rateSupplier.getAsDouble();
            }
        };
    }

}