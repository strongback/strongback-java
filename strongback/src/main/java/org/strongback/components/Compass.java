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
 * A sensor that determines the positional heading, or angular displacement, in degrees. A compass is an {@link AngleSensor}
 * with an additional method to determine {@link #getHeading() heading}. It also can be {@link #zero() zeroed} to return angles
 * and heading relative to another. Negative values are assumed to be counter-clockwise and positive values are clockwise.
 *
 * @author Randall Hauch
 */
@ThreadSafe
public interface Compass extends AngleSensor {

    /**
     * Gets the angular displacement of in degrees in the range [0, 360).
     *
     * @return the heading of this {@link Compass}
     */
    public default double getHeading() {
        double positiveOrNegative = getAngle() % 360;
        return positiveOrNegative >= 0 ? positiveOrNegative : 360 + positiveOrNegative;
    }

    /**
     * Compute the change in heading between the {@link #getHeading() current heading} and the target heading, using the given
     * tolerance for the difference. The result is the angle that this sensor must rotate to reach the target heading, which may
     * be positive or negative.
     *
     * @param targetHeading the target heading
     * @param tolerance the allowed tolerance in degrees between the two headings
     * @return the angular displacement required for this sensor to reach the target heading, or 0.0d if the two headings are
     *         already within the specified {@code tolerance}
     */
    default public double computeHeadingChangeTo(double targetHeading, double tolerance) {
        double diff = targetHeading - this.getHeading();
        return Math.abs(diff) <= Math.abs(tolerance) ? 0.0 : diff;
    }

    /**
     * Create a angle sensor for the given function that returns the angle.
     *
     * @param angleSupplier the function that returns the angle; may not be null
     * @return the angle sensor
     */
    public static Compass create(DoubleSupplier angleSupplier) {
        return new Compass() {
            private volatile double zero = 0;

            @Override
            public double getAngle() {
                return angleSupplier.getAsDouble() - zero;
            }

            @Override
            public Compass zero() {
                zero = angleSupplier.getAsDouble();
                return this;
            }
        };
    }

}