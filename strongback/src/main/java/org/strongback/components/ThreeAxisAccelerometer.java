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

/**
 * An accelerometer is a device capable of sensing acceleration. By performing two integrations, an accelerometer can also find
 * velocity and displacement.
 *
 * @author Zach Anderson
 * @see TwoAxisAccelerometer
 * @see Accelerometer
 */
public interface ThreeAxisAccelerometer extends TwoAxisAccelerometer {

    /**
     * Get the Z-axis accelerometer.
     * @return the accelerometer for the Z-axis; never null
     */
    public Accelerometer getZDirection();

    /**
     * Get the accelerometer for the axis with the given index, where 0 is the X-axis, 1 is the Y-axis, and 2 is the Z-axis.
     * @return the accelerometer; never null
     * @throws IllegalArgumentException if {@code axis} is invalid
     */
    @Override
    default public Accelerometer getDirection(int axis) {
        if (axis == 0) return getXDirection();
        if (axis == 1) return getYDirection();
        if (axis == 2) return getZDirection();
        throw new IllegalArgumentException("The axis was '" + axis + "', but only '0', '1', or '2' is accepted");
    }

    /**
     * Get the instantaneous multidimensional acceleration values for all 3 axes.
     * @return the acceleration values for 3 axes; never null
     */
    @Override
    default public ThreeAxisAcceleration getAcceleration() {
        return new ThreeAxisAcceleration(getXDirection().getAcceleration(), getYDirection().getAcceleration(), getZDirection()
                .getAcceleration());
    }

    /**
     * Create a 3-axis accelerometer from the three individual accelerometers.
     * @param xAxis the accelerometer for the X-axis; may not be null
     * @param yAxis the accelerometer for the Y-axis; may not be null
     * @param zAxis the accelerometer for the Z-axis; may not be null
     * @return the 3-axis accelerometer; never null
     */
    public static ThreeAxisAccelerometer create(Accelerometer xAxis, Accelerometer yAxis, Accelerometer zAxis) {
        return new ThreeAxisAccelerometer() {

            @Override
            public Accelerometer getXDirection() {
                return xAxis;
            }

            @Override
            public Accelerometer getYDirection() {
                return yAxis;
            }

            @Override
            public Accelerometer getZDirection() {
                return zAxis;
            }
        };
    }

    /**
     * Create a 3-axis accelerometer from a 2-axis accelerometer and a separate accelerometer for the Z-axis.
     * @param xAndY the 2-axis accelerometer for the X- and Y-axes; may not be null
     * @param zAxis the accelerometer for the Z-axis; may not be null
     * @return the 3-axis accelerometer; never null
     */
    public static ThreeAxisAccelerometer create(TwoAxisAccelerometer xAndY, Accelerometer zAxis) {
        return new ThreeAxisAccelerometer() {

            @Override
            public Accelerometer getXDirection() {
                return xAndY.getXDirection();
            }

            @Override
            public Accelerometer getYDirection() {
                return xAndY.getYDirection();
            }

            @Override
            public Accelerometer getZDirection() {
                return zAxis;
            }
        };
    }
}