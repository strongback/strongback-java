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
 * A distance sensor is a sensor capable of sensing distance. The unit is assumed to be inches.
 *
 * @author Zach Anderson
 */
@ThreadSafe
@FunctionalInterface
public interface DistanceSensor extends Zeroable {
    /**
     * Gets the current value of this {@link DistanceSensor} in inches.
     *
     * @return the value of this {@link DistanceSensor}
     * @see #getDistanceInFeet()
     */
    public double getDistance();

    /**
     * Gets the current value of this {@link DistanceSensor} in feet.
     *
     * @return the value of this {@link DistanceSensor}
     * @see #getDistance()
     */
    default public double getDistanceInFeet() {
        return getDistance() / 12.0;
    }

    @Override
    default public DistanceSensor zero() {
        return this;
    }

    /**
     * Create a distance sensor for the given function that returns the distance.
     *
     * @param distanceSupplier the function that returns the distance; may not be null
     * @return the angle sensor
     */
    public static DistanceSensor create(DoubleSupplier distanceSupplier) {
        return new DistanceSensor() {
            private double zero = 0;

            @Override
            public double getDistance() {
                return distanceSupplier.getAsDouble() - zero;
            }

            @Override
            public DistanceSensor zero() {
                zero = distanceSupplier.getAsDouble();
                return this;
            }
        };
    }
}