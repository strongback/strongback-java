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

package org.strongback.hardware;

import org.strongback.components.Motor;
import org.strongback.function.DoubleToDoubleFunction;

import edu.wpi.first.wpilibj.PWM;
import edu.wpi.first.wpilibj.SpeedController;

/**
 * Wrapper for WPILib {@link SpeedController}.
 *
 * @author Zach Anderson
 * @see Motor
 * @see Hardware
 * @see edu.wpi.first.wpilibj.SpeedController
 */
final class HardwareMotor implements Motor {

    private final SpeedController controller;
    private final DoubleToDoubleFunction speedValidator;

    HardwareMotor(SpeedController controller, DoubleToDoubleFunction speedValidator ) {
        this.controller = controller;
        this.speedValidator = speedValidator;
    }

    @Override
    public HardwareMotor setSpeed(double speed) {
        controller.set(speedValidator.applyAsDouble(speed));
        return this;
    }

    @Override
    public double getSpeed() {
        return controller.get();
    }

    public short getSpeedAsShort() {
        return (short) ((PWM) (controller)).getRaw();
    }
}