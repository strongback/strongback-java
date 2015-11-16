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
import org.strongback.components.TemperatureSensor;
import org.strongback.components.VoltageSensor;

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
class HardwareTalonSRX implements TalonSRX {

    protected final CANTalon talon;
    protected final AngleSensor encoder;
    protected final Switch forwardLimitSwitch;
    protected final Switch reverseLimitSwitch;
    protected final CurrentSensor outputCurrent;
    protected final VoltageSensor outputVoltage;
    protected final VoltageSensor busVoltage;
    protected final TemperatureSensor temperature;
    protected final Faults instantaneousFaults;
    protected final Faults stickyFaults;

    HardwareTalonSRX(CANTalon talon, double pulsesPerDegree) {
        this.talon = talon;

        this.forwardLimitSwitch = talon::isRevLimitSwitchClosed;
        this.reverseLimitSwitch = talon::isFwdLimitSwitchClosed;
        this.outputCurrent = talon::getOutputCurrent;
        this.outputVoltage = talon::getOutputVoltage;
        this.busVoltage = talon::getBusVoltage;
        this.temperature = talon::getTemp;
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
        instantaneousFaults = new Faults() {
            @Override
            public Switch forwardLimitSwitch() {
                return ()->talon.getFaultForLim() != 0;
            }
            @Override
            public Switch reverseLimitSwitch() {
                return ()->talon.getFaultRevLim() != 0;
            }
            @Override
            public Switch forwardSoftLimit() {
                return ()->talon.getFaultForSoftLim() != 0;
            }
            @Override
            public Switch reverseSoftLimit() {
                return ()->talon.getFaultRevSoftLim() != 0;
            }
            @Override
            public Switch hardwareFailure() {
                return ()->talon.getFaultHardwareFailure() != 0;
            }
            @Override
            public Switch overTemperature() {
                return ()->talon.getFaultOverTemp() != 0;
            }
            @Override
            public Switch underVoltage() {
                return ()->talon.getFaultUnderVoltage() != 0;
            }
        };
        stickyFaults = new Faults() {
            @Override
            public Switch forwardLimitSwitch() {
                return ()->talon.getStickyFaultForLim() != 0;
            }
            @Override
            public Switch reverseLimitSwitch() {
                return ()->talon.getStickyFaultRevLim() != 0;
            }
            @Override
            public Switch forwardSoftLimit() {
                return ()->talon.getStickyFaultForSoftLim() != 0;
            }
            @Override
            public Switch reverseSoftLimit() {
                return ()->talon.getStickyFaultRevSoftLim() != 0;
            }
            @Override
            public Switch hardwareFailure() {
                return ()->talon.getFaultHardwareFailure() != 0;    // no sticky version!
            }
            @Override
            public Switch overTemperature() {
                return ()->talon.getStickyFaultOverTemp() != 0;
            }
            @Override
            public Switch underVoltage() {
                return ()->talon.getStickyFaultUnderVoltage() != 0;
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
        return outputCurrent;
    }

    @Override
    public VoltageSensor getVoltageSensor() {
        return outputVoltage;
    }

    @Override
    public VoltageSensor getBusVoltageSensor() {
        return busVoltage;
    }

    @Override
    public TemperatureSensor getTemperatureSensor() {
        return temperature;
    }

    @Override
    public TalonSRX setForwardSoftLimit(int forwardLimit) {
        talon.setForwardSoftLimit(forwardLimit);
        return this;
    }

    @Override
    public TalonSRX enableForwardSoftLimit(boolean enable) {
        talon.enableForwardSoftLimit(enable);
        return this;
    }

    @Override
    public TalonSRX setReverseSoftLimit(int reverseLimit) {
        talon.setReverseSoftLimit(reverseLimit);
        return this;
    }

    @Override
    public TalonSRX enableReverseSoftLimit(boolean enable) {
        talon.enableReverseSoftLimit(enable);
        return this;
    }

    @Override
    public TalonSRX enableLimitSwitch(boolean forward, boolean reverse) {
        talon.enableLimitSwitch(forward, reverse);
        return this;
    }

    @Override
    public TalonSRX enableBrakeMode(boolean brake) {
        talon.enableBrakeMode(brake);
        return this;
    }

    @Override
    public TalonSRX setForwardLimitSwitchNormallyOpen(boolean normallyOpen) {
        talon.ConfigFwdLimitSwitchNormallyOpen(normallyOpen);
        return this;
    }

    @Override
    public TalonSRX setReverseLimitSwitchNormallyOpen(boolean normallyOpen) {
        talon.ConfigRevLimitSwitchNormallyOpen(normallyOpen);
        return this;
    }

    @Override
    public Faults faults() {
        return instantaneousFaults;
    }

    @Override
    public Faults stickyFaults() {
        return stickyFaults;
    }

    @Override
    public TalonSRX clearStickyFaults() {
        talon.clearStickyFaults();
        return this;
    }

    @Override
    public long getFirmwareVersion() {
        return talon.GetFirmwareVersion();
    }

    @Override
    public boolean isSafetyEnabled() {
        return talon.isSafetyEnabled();
    }

    @Override
    public TalonSRX setSafetyEnabled(boolean enabled) {
        talon.setSafetyEnabled(enabled);
        return this;
    }

    @Override
    public double getExpiration() {
        return talon.getExpiration();
    }

    @Override
    public TalonSRX setExpiration(double timeout) {
        talon.setExpiration(timeout);
        return this;
    }

    @Override
    public boolean isAlive() {
        return talon.isAlive();
    }
}