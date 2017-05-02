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

package org.strongback.components.ui;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

import org.strongback.function.DoubleToDoubleFunction;

/**
 * Defines a range of values between [-1.0, 1.0] inclusive.
 *
 * @author Zach Anderson
 */
@FunctionalInterface
public interface ContinuousRange {
    /**
     * Read the current value.
     *
     * @return the value in the range [-1.0, 1.0] inclusive.
     */
    public double read();

    /**
     * Create a new range that inverts the values of this instance.
     *
     * @return the new inverted range; never null
     */
    default public ContinuousRange invert() {
        return () -> this.read() * -1.0;
    }

    /**
     * Create a new range that scales the values of this instance.
     *
     * @param scale the scaling factor
     * @return the new scaled range; never null
     */
    default public ContinuousRange scale(double scale) {
        return () -> this.read() * scale;
    }

    /**
     * Create a new range that scales the values of this instance.
     *
     * @param scale the function that determines the scaling factor
     * @return the new scaled range; never null
     */
    default public ContinuousRange scale(DoubleSupplier scale) {
        return () -> this.read() * scale.getAsDouble();
    }

    /**
     * Create a new range that maps the values of this instance using the supplied function.
     *
     * @param mapFunction the function that maps the current value to another
     * @return the new mapped range; never null
     */
    default public ContinuousRange map(DoubleToDoubleFunction mapFunction) {
        return () -> mapFunction.applyAsDouble(this.read());
    }

    /**
     * Create a new {@link IntSupplier} that scales the values of this instance and rounds to an integer.
     *
     * @param scale the scaling factor
     * @return the new scaled IntSupplier; never null
     */
    default public IntSupplier scaleAsInt(double scale) {
        return () -> (int)(this.read() * scale);
    }
}