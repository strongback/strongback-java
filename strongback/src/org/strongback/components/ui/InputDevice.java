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

import java.util.function.IntToDoubleFunction;

import org.strongback.components.Switch;
import org.strongback.function.IntToBooleanFunction;
import org.strongback.function.IntToIntFunction;

/**
 * A simple collection of axes and buttons.
 */
public interface InputDevice {
    /**
     * Get the analog axis for the given number.
     * @param axis the axis number
     * @return the analog axis, or null if there is no such axis
     */
    public ContinuousRange getAxis(int axis);
    /**
     * Get the button for the given number.
     * @param button the button number
     * @return the button, or null if there is no such button
     */
    public Switch getButton(int button);
    /**
     * Get the directional axis for the given D-pad number.
     * @param pad the pad number
     * @return the directional axis, or null if there is no such axis for the given D-pad number
     */
    public DirectionalAxis getDPad(int pad);

    /**
     * Create an input device from the supplied mapping functions.
     * @param axisToValue the function that maps an integer to a double value for the axis
     * @param buttonNumberToSwitch the function that maps an integer to whether the button is pressed
     * @param padToValue the function that maps an integer to the directional axis output
     * @return the resulting input device; never null
     */
    public static InputDevice create( IntToDoubleFunction axisToValue, IntToBooleanFunction buttonNumberToSwitch, IntToIntFunction padToValue ) {
        return new InputDevice() {
            @Override
            public ContinuousRange getAxis(int axis) {
                return ()->axisToValue.applyAsDouble(axis);
            }
            @Override
            public Switch getButton(int button) {
                return ()->buttonNumberToSwitch.applyAsBoolean(button);
            }
            @Override
            public DirectionalAxis getDPad(int pad) {
                return ()->padToValue.applyAsInt(pad);
            }
        };
    }
}