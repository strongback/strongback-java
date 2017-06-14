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

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;
import org.strongback.annotation.Immutable;
import org.strongback.components.CurrentSensor;
import org.strongback.components.RevSensor;
import org.strongback.components.Switch;
import org.strongback.components.TalonSRX;
import org.strongback.components.TemperatureSensor;
import org.strongback.components.VoltageSensor;

/**
 * Talon speed controller with position and current sensor
 *
 * @author Nathan Brown
 * @see TalonSRX
 * @see CANTalon
 */
@Immutable
class HardwareTalonSRX implements TalonSRX {
    private static final double DEFAULT_ANALOG_RATE = 0.100;
    private static final double DEFAULT_QUADRATURE_RATE = 0.100;
    private static final double DEFAULT_FEEDBACK_RATE = 0.020;

    private static final int RISES_PER_PULSE = 4; // 4x mode
    private static final double MAX_ANALOG_VOLTAGE = 3.3; // 0-3.3V
    private static final RevSensor NO_OP_SENSOR = RevSensor.create(() -> 0.0, () -> 0.0);

    protected final CANTalon talon;
    protected final RevSensor encoderInput;
    protected final RevSensor analogInput;
    protected final RevSensor pwmInput;
    protected final RevSensor selectedInput;
    protected volatile double quadratureRateInSeconds = DEFAULT_QUADRATURE_RATE;
    protected volatile double analogRateInSeconds = DEFAULT_ANALOG_RATE;
    protected volatile double feedbackRateInSeconds = DEFAULT_FEEDBACK_RATE;
    protected final Switch forwardLimitSwitch;
    protected final Switch reverseLimitSwitch;
    protected final CurrentSensor outputCurrent;
    protected final VoltageSensor outputVoltage;
    protected final VoltageSensor busVoltage;
    protected final TemperatureSensor temperature;
    protected final Faults instantaneousFaults;
    protected final Faults stickyFaults;

    HardwareTalonSRX(CANTalon talon) {
        this.talon = talon;

        this.forwardLimitSwitch = talon::isFwdLimitSwitchClosed;
        this.reverseLimitSwitch = talon::isRevLimitSwitchClosed;
        this.outputCurrent = talon::getOutputCurrent;
        this.outputVoltage = talon::getOutputVoltage;
        this.busVoltage = talon::getBusVoltage;
        this.temperature = talon::getTemperature;
        this.encoderInput = RevSensor.create(talon::getEncPosition, talon::getEncVelocity);
        this.analogInput = RevSensor.create(talon::getAnalogInPosition, talon::getAnalogInVelocity);
        this.pwmInput = RevSensor.create(talon::getPulseWidthPosition, talon::getPulseWidthVelocity);
        this.selectedInput = RevSensor.create(talon::getPosition, talon::getSpeed);
        this.instantaneousFaults = new Faults() {
            @Override
            public Switch forwardLimitSwitch() {
                return () -> talon.getFaultForLim() != 0;
            }

            @Override
            public Switch reverseLimitSwitch() {
                return () -> talon.getFaultRevLim() != 0;
            }

            @Override
            public Switch forwardSoftLimit() {
                return () -> talon.getFaultForSoftLim() != 0;
            }

            @Override
            public Switch reverseSoftLimit() {
                return () -> talon.getFaultRevSoftLim() != 0;
            }

            @Override
            public Switch hardwareFailure() {
                return () -> talon.getFaultHardwareFailure() != 0;
            }

            @Override
            public Switch overTemperature() {
                return () -> talon.getFaultOverTemp() != 0;
            }

            @Override
            public Switch underVoltage() {
                return () -> talon.getFaultUnderVoltage() != 0;
            }
        };
        this.stickyFaults = new Faults() {
            @Override
            public Switch forwardLimitSwitch() {
                return () -> talon.getStickyFaultForLim() != 0;
            }

            @Override
            public Switch reverseLimitSwitch() {
                return () -> talon.getStickyFaultRevLim() != 0;
            }

            @Override
            public Switch forwardSoftLimit() {
                return () -> talon.getStickyFaultForSoftLim() != 0;
            }

            @Override
            public Switch reverseSoftLimit() {
                return () -> talon.getStickyFaultRevSoftLim() != 0;
            }

            @Override
            public Switch hardwareFailure() {
                return () -> talon.getFaultHardwareFailure() != 0; // no sticky version!
            }

            @Override
            public Switch overTemperature() {
                return () -> talon.getStickyFaultOverTemp() != 0;
            }

            @Override
            public Switch underVoltage() {
                return () -> talon.getStickyFaultUnderVoltage() != 0;
            }
        };
    }

    @Override
    public int getDeviceID() {
        return talon.getDeviceID();
    }

    @Override
    public double getSpeed() {
        talon.changeControlMode(TalonControlMode.PercentVbus);
        return talon.get();
    }

    @Override
    public TalonSRX setSpeed(double speed) {
        talon.changeControlMode(TalonControlMode.PercentVbus);
        talon.set(speed);
        return this;
    }

    @Override
    public void stop() {
        talon.enableBrakeMode(true);
        talon.set(0);
    }

    @Override
    public RevSensor getEncoderInput() {
        return encoderInput;
    }

    @Override
    public RevSensor getAnalogInput() {
        return analogInput;
    }

    @Override
    public RevSensor getPwmInput() {
        return pwmInput;
    }

    @Override
    public RevSensor getSelectedSensor() {
        return selectedInput;
    }

    @Override
    public void setEncoderCodesPerRevolution(int codesPerRev) {
        talon.configEncoderCodesPerRev(codesPerRev);
    }

    @Override
    public void setPotentiometerTurns(int turns) {
        talon.configPotentiometerTurns(turns);
    }

    @Override
    public TalonSRX setFeedbackDevice(FeedbackDevice device) {
        talon.setFeedbackDevice(CANTalon.FeedbackDevice.valueOf(device.value()));
        return this;
    }

    @Override
    public TalonSRX setStatusFrameRate(StatusFrameRate frameRate, int periodMillis) {
        talon.setStatusFrameRateMs(CANTalon.StatusFrameRate.valueOf(frameRate.value()), periodMillis);
        double periodInSeconds = periodMillis / 1000.0;
        switch(frameRate) {
            case FEEDBACK:
                feedbackRateInSeconds = periodInSeconds;
                break;
            case QUADRATURE_ENCODER:
                quadratureRateInSeconds = periodInSeconds;
                break;
            case ANALOG_TEMPERATURE_BATTERY_VOLTAGE:
                analogRateInSeconds = periodInSeconds;
                break;
            case GENERAL:
                // nothing to set, since our code doesn't use the "general" frames
                break;
        }
        return this;
    }

    @Override
    public TalonSRX reverseSensor(boolean flip) {
        talon.reverseSensor(flip);
        return this;
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
    public HardwareTalonSRX enableForwardSoftLimit(boolean enable) {
        talon.enableForwardSoftLimit(enable);
        return this;
    }

    @Override
    public HardwareTalonSRX setReverseSoftLimit(int reverseLimit) {
        talon.setReverseSoftLimit(reverseLimit);
        return this;
    }

    @Override
    public HardwareTalonSRX enableReverseSoftLimit(boolean enable) {
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
    public TalonSRX setVoltageRampRate(double rampRate) {
        talon.setVoltageRampRate(rampRate);
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