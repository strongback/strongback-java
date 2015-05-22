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
     * @return the analog axis
     */
    public AnalogAxis getAxis(int axis);
    public Switch getButton(int button);
    public DirectionalAxis getDPad(int pad);

    public static InputDevice create( IntToDoubleFunction axisToValue, IntToBooleanFunction buttonNumberToSwitch, IntToIntFunction padToValue ) {
        return new InputDevice() {
            @Override
            public AnalogAxis getAxis(int axis) {
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