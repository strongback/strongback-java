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

import org.strongback.annotation.Immutable;
import org.strongback.components.AngleSensor;
import org.strongback.components.CurrentSensor;
import org.strongback.components.Switch;
import org.strongback.components.TalonSRX;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.ControlMode;

/**
 * Talon speed controller with position and current sensor
 *
 * @author Nathan Brown
 * @see TalonSRX
 * @see CANTalon
 */
@Immutable
final class HardwareTalonSRX implements TalonSRX {

    private final CANTalon talon;
    private final AngleSensor encoder;
    private final Switch forwardLimitSwitch;
    private final Switch reverseLimitSwitch;
    private final CurrentSensor current;

    HardwareTalonSRX(CANTalon talon, double pulsesPerDegree) {
        this.talon = talon;

        this.forwardLimitSwitch = talon::isRevLimitSwitchClosed;
        this.reverseLimitSwitch = talon::isFwdLimitSwitchClosed;
        this.current = talon::getOutputCurrent;
        this.encoder = new AngleSensor() {
            private int zero = 0;

            @Override
            public double getAngle() {
                return (talon.getEncPosition() - zero) / pulsesPerDegree;
            }

            @Override
            public AngleSensor zero() {
                zero = talon.getEncPosition();
                return this;
            }
        };
    }

    @Override
    public double getSpeed() {
        talon.changeControlMode(ControlMode.PercentVbus);
        return talon.get();
    }

    @Override
    public TalonSRX setSpeed(double speed) {
        talon.changeControlMode(ControlMode.PercentVbus);
        talon.set(speed);
        return this;
    }

    @Override
    public void stop() {
        talon.enableBrakeMode(true);
        talon.set(0);
    }

    @Override
    public AngleSensor getAngleSensor() {
        return encoder;
    }

    @Override
    public Switch getForwardLimitSwitch() {
        return forwardLimitSwitch;
    }

    @Override
    public Switch getReverseLimitSwitch() {
        return reverseLimitSwitch;
    }

    @Override
    public CurrentSensor getCurrentSensor() {
        return current;
    }
}