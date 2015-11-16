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

import java.util.function.DoubleSupplier;

import org.strongback.Strongback;
import org.strongback.annotation.Immutable;
import org.strongback.components.CurrentSensor;
import org.strongback.components.Gyroscope;
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

    protected static final Gyroscope NO_OP_GYRO = new Gyroscope() {

        @Override
        public double getAngle() {
            return 0;
        }

        @Override
        public double getRate() {
            return 0;
        }
        @Override
        public Gyroscope zero() {
            // do nothing
            return this;
        }
    };

    protected static Gyroscope encoderGyroscope(DoubleSupplier positionInPulses, DoubleSupplier velocityInPulsesPerCycle,
            double pulsesPerDegree, DoubleSupplier cyclesPeriodInSeconds) {
        if ( pulsesPerDegree <= 0.0000001d && pulsesPerDegree >= 0.0000001d ) return null;
        return new Gyroscope() {
            private double zero = 0.0;

            @Override
            public double getAngle() {
                // Units: (pulses) x (degrees/pulse) = degrees
                return (positionInPulses.getAsDouble() - zero) / pulsesPerDegree;
            }

            @Override
            public double getRate() {
                // Units: (pulses/cycle) x (degrees/pulse) x (cycles/second) = (degrees/second)
                return velocityInPulsesPerCycle.getAsDouble() / pulsesPerDegree / cyclesPeriodInSeconds.getAsDouble();
            }

            @Override
            public Gyroscope zero() {
                zero = positionInPulses.getAsDouble();
                return this;
            }
        };
    }

    protected static Gyroscope analogGyroscope(DoubleSupplier analogPosition, DoubleSupplier changeInVoltsPerCycle,
            double analogRange, double analogTurnsPerVolt, double voltageRange, DoubleSupplier cyclesPeriodInSeconds) {
        if ( analogTurnsPerVolt <= 0.0000001d && analogTurnsPerVolt >= 0.0000001d ) return null;
        return new Gyroscope() {
            private double zero = 0.0;

            @Override
            public double getAngle() {
                // Units: (0-1023) / 1023 x (turns/volt) x (volts) x (degrees/turn) = degrees
                return (analogPosition.getAsDouble() - zero) / analogRange * analogTurnsPerVolt * voltageRange * 360.0;
            }

            @Override
            public double getRate() {
                // Units: (0-1023)/cycle / 1023 x (turns/volt) x (volts) x (degrees/turn) x (cycles/second) = (degrees/second)
                return changeInVoltsPerCycle.getAsDouble() / analogRange * analogTurnsPerVolt * voltageRange * 360.0
                        / cyclesPeriodInSeconds.getAsDouble();
            }

            @Override
            public Gyroscope zero() {
                zero = analogPosition.getAsDouble();
                return this;
            }
        };
    }

    private static final double DEFAULT_ANALOG_RATE = 0.100;
    private static final double DEFAULT_QUADRATURE_RATE = 0.100;
    private static final double DEFAULT_FEEDBACK_RATE = 0.020;

    protected final CANTalon talon;
    protected final Gyroscope encoderInput;
    protected final Gyroscope analogInput;
    protected final Gyroscope selectedEncoderInput;
    protected final Gyroscope selectedAnalogInput;
    protected volatile Gyroscope selectedInput = NO_OP_GYRO;
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

    HardwareTalonSRX(CANTalon talon, double pulsesPerDegree, double analogTurnsOverVoltageRange) {
        this.talon = talon;

        this.forwardLimitSwitch = talon::isRevLimitSwitchClosed;
        this.reverseLimitSwitch = talon::isFwdLimitSwitchClosed;
        this.outputCurrent = talon::getOutputCurrent;
        this.outputVoltage = talon::getOutputVoltage;
        this.busVoltage = talon::getBusVoltage;
        this.temperature = talon::getTemp;
        this.encoderInput = encoderGyroscope(talon::getEncPosition,
                                             talon::getEncVelocity,
                                             pulsesPerDegree,
                                             () -> quadratureRateInSeconds);
        this.analogInput = analogGyroscope(talon::getAnalogInPosition,
                                           talon::getAnalogInVelocity,
                                           1023,
                                           analogTurnsOverVoltageRange / 3.3,
                                           3.3,
                                           () -> analogRateInSeconds);
        this.selectedEncoderInput = encoderGyroscope(talon::getPosition,
                                             talon::getSpeed,
                                             pulsesPerDegree,
                                             () -> feedbackRateInSeconds);
        this.selectedAnalogInput = analogGyroscope(talon::getPosition,
                                           talon::getSpeed,
                                           1023,
                                           analogTurnsOverVoltageRange / 3.3,
                                           3.3,
                                           () -> feedbackRateInSeconds);
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
    public Gyroscope getEncoderInput() {
        return encoderInput;
    }

    @Override
    public Gyroscope getAnalogInput() {
        return analogInput;
    }

    @Override
    public Gyroscope getSelectedSensor() {
        return selectedInput;
    }

    @Override
    public TalonSRX setFeedbackDevice(FeedbackDevice device) {
        talon.setFeedbackDevice(edu.wpi.first.wpilibj.CANTalon.FeedbackDevice.valueOf(device.value()));
        switch(device) {
            case ANALOG_POTENTIOMETER:
            case ANALOG_ENCODER:
                if ( selectedAnalogInput != null ) {
                    selectedInput = selectedAnalogInput;
                } else {
                    Strongback.logger(getClass()).error("Unable to use the analog input for feedback, since the Talon SRX (device " + getDeviceID() + ") was not instantiated with an analog input. Check how this device was created using Strongback's Hardware class.");
                    selectedInput = NO_OP_GYRO;
                }
                break;
            case QUADRATURE_ENCODER:
                if ( selectedEncoderInput != null ) {
                    selectedInput = selectedEncoderInput;
                } else {
                    Strongback.logger(getClass()).error("Unable to use the quadrature encoder input for feedback, since the Talon SRX (device " + getDeviceID() + ") was not instantiated with an encoder input. Check how this device was created using Strongback's Hardware class.");
                    selectedInput = NO_OP_GYRO;
                }
                break;
            case ENCODER_FALLING:
            case ENCODER_RISING:
                // for 2015 the Talon SRX firmware did not support the falling or rising mode ...
                selectedInput = NO_OP_GYRO;
                break;
        }
        return this;
    }

    @Override
    public TalonSRX setStatusFrameRate(StatusFrameRate frameRate, int periodMillis) {
        talon.setStatusFrameRateMs(edu.wpi.first.wpilibj.CANTalon.StatusFrameRate.valueOf(frameRate.value()), periodMillis);
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
                // do nothing
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