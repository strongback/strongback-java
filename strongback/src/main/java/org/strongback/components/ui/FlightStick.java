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
 * A type of input device consisting of a joystick with twist and throttle and multiple buttons.
 */
public interface FlightStick extends InputDevice {
    public ContinuousRange getPitch();

    public ContinuousRange getYaw();

    public ContinuousRange getRoll();

    public ContinuousRange getThrottle();

    public Switch getTrigger();

    public Switch getThumb();

    public static FlightStick create(IntToDoubleFunction axisToValue, IntToBooleanFunction buttonNumberToSwitch,
            IntToIntFunction padToValue, ContinuousRange pitch, ContinuousRange yaw, ContinuousRange roll,
            ContinuousRange throttle, Switch trigger, Switch thumb) {
        return new FlightStick() {
            @Override
            public ContinuousRange getAxis(int axis) {
                return () -> axisToValue.applyAsDouble(axis);
            }

            @Override
            public Switch getButton(int button) {
                return () -> buttonNumberToSwitch.applyAsBoolean(button);
            }

            @Override
            public DirectionalAxis getDPad(int pad) {
                return () -> padToValue.applyAsInt(pad);
            }

            @Override
            public ContinuousRange getPitch() {
                return pitch;
            }

            @Override
            public ContinuousRange getYaw() {
                return yaw;
            }

            @Override
            public ContinuousRange getRoll() {
                return roll;
            }

            @Override
            public ContinuousRange getThrottle() {
                return throttle;
            }

            @Override
            public Switch getTrigger() {
                return trigger;
            }

            @Override
            public Switch getThumb() {
                return thumb;
            }
        };
    }

}