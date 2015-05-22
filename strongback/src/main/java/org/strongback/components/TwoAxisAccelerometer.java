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

import org.strongback.annotation.Immutable;


/**
 * An accelerometer is a device capable of sensing acceleration. By performing
 * two integrations, an accelerometer can also find velocity and displacement.
 *
 * @author Zach Anderson
 * @see ThreeAxisAccelerometer
 * @see Accelerometer
 */
public interface TwoAxisAccelerometer {

    /**
     * Get the X-axis accelerometer.
     * @return the accelerometer for the X-axis; never null
     */
    public Accelerometer getXDirection();

    /**
     * Get the Y-axis accelerometer.
     * @return the accelerometer for the Y-axis; never null
     */
    public Accelerometer getYDirection();

    /**
     * Get the accelerometer for the axis with the given index, where 0 is the X-axis and 1 is the Y-axis.
     * @return the accelerometer; never null
     * @throws IllegalArgumentException if {@code axis} is invalid
     */
    default public Accelerometer getDirection( int axis ) {
        if ( axis == 0 ) return getXDirection();
        if ( axis == 1 ) return getYDirection();
        throw new IllegalArgumentException("The axis was '" + axis + "', but only '0' or '1' is accepted");
    }

    /**
     * Get the instantaneous multidimensional acceleration values for all 2 axes.
     * @return the acceleration values for 2 axes; never null
     */
    default public TwoDimensionalTuple getAcceleration() {
        return new TwoDimensionalTuple(getXDirection().getAcceleration(),getYDirection().getAcceleration());
    }

    /**
     * A pair of immutable acceleration values, one for each axis.
     */
    @Immutable
    public class TwoDimensionalTuple {
        private final double x;
        private final double y;

        public TwoDimensionalTuple(double x, double y) {
            this.x = x;
            this.y = y;
        }
        public double getX() {
            return x;
        }
        public double getY() {
            return y;
        }
        @Override
        public String toString() {
            return "[" + x + ',' + y + "]";
        }
    }

    /**
     * Create a 2-axis accelerometer from the three individual accelerometers.
     * @param xAxis the accelerometer for the X-axis; may not be null
     * @param yAxis the accelerometer for the Y-axis; may not be null
     * @return
     */
    public static TwoAxisAccelerometer create(Accelerometer xAxis, Accelerometer yAxis) {
        return new TwoAxisAccelerometer() {

            @Override
            public Accelerometer getXDirection() {
                return xAxis;
            }

            @Override
            public Accelerometer getYDirection() {
                return yAxis;
            }
        };
    }
}