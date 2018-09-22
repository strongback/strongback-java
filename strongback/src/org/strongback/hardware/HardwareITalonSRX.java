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
import org.strongback.components.*;
import org.strongback.components.ITalonSRX;

//import com.ctre.CANTalon;
//import com.ctre.CANTalon.TalonControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

/**
 * Talon speed controller with position and current sensor
 *
 * @author Nathan Brown
 * @see ITalonSRX
 * @see TalonSRX
 */
@Immutable
class HardwareITalonSRX implements ITalonSRX {

    protected static interface InputSensor extends Gyroscope {
        public double rawPositionForAngleInDegrees( double angle );
        public double angleInDegreesFromRawPosition( double position );
    }

    protected static final InputSensor NO_OP_SENSOR = new InputSensor() {

        @Override
        public double getAngle() {
            return 0;
        }

        @Override
        public double getRate() {
            return 0;
        }
        @Override
        public InputSensor zero() {
            return this; // do nothing
        }

        @Override
        public double rawPositionForAngleInDegrees(double angleInDegrees) {
            return 0.0;
        }

        @Override
        public double angleInDegreesFromRawPosition(double position) {
            return 0.0;
        }
    };

    protected static final class EncoderInputSensor implements InputSensor {
        private double zero = 0.0;
        private final DoubleSupplier positionInEdges;
        private final DoubleSupplier velocityInEdgesPerCycle;
        private final DoubleSupplier cyclePeriodInSeconds;
        private final double pulsesPerDegree;
        private final double edgesPerPulse;

        protected EncoderInputSensor( DoubleSupplier positionInEdges, DoubleSupplier velocityInEdgesPerCycle,
            double pulsesPerDegree, int edgesPerPulse, DoubleSupplier cyclesPeriodInSeconds ) {
            this.positionInEdges = positionInEdges;
            this.velocityInEdgesPerCycle = velocityInEdgesPerCycle;
            this.cyclePeriodInSeconds = cyclesPeriodInSeconds;
            this.pulsesPerDegree = pulsesPerDegree;
            this.edgesPerPulse = edgesPerPulse;
        }

        @Override
        public double rawPositionForAngleInDegrees(double angle) {
            // Units: (degrees) x (pulses/degrees) * (edges/pulses) = (edges)
            double relativeInput = angle * pulsesPerDegree * edgesPerPulse;
            return relativeInput + zero;
        }

        @Override
        public double angleInDegreesFromRawPosition(double position) {
            // Units: (edges) x (pulse/edges) x (degrees/pulse) = degrees
            return (position - zero) / edgesPerPulse / pulsesPerDegree;
        }

        @Override
        public double getAngle() {
            return angleInDegreesFromRawPosition(positionInEdges.getAsDouble());
        }

        @Override
        public double getRate() {
            // Units: (edges/cycle) * (pulses/edge) x (degrees/pulse) x (cycles/second) = (degrees/second)
            return velocityInEdgesPerCycle.getAsDouble() / edgesPerPulse / pulsesPerDegree / cyclePeriodInSeconds.getAsDouble();
        }

        @Override
        public Gyroscope zero() {
            zero = positionInEdges.getAsDouble();
            return this;
        }
    }

    protected static final class AnalogInputSensor implements InputSensor {
        private double zero = 0.0;
        private final DoubleSupplier analogPosition;
        private final DoubleSupplier changeInVoltsPerCycle;
        private final DoubleSupplier cyclePeriodInSeconds;
        private final double analogRange;
        private final double analogTurnsPerVolt;
        private final double voltageRange;

        protected AnalogInputSensor( DoubleSupplier analogPosition, DoubleSupplier changeInVoltsPerCycle,
                double analogRange, double analogTurnsPerVolt, double voltageRange, DoubleSupplier cyclePeriodInSeconds ) {
            this.analogPosition = analogPosition;
            this.changeInVoltsPerCycle = changeInVoltsPerCycle;
            this.cyclePeriodInSeconds = cyclePeriodInSeconds;
            this.analogRange = analogRange;
            this.analogTurnsPerVolt = analogTurnsPerVolt;
            this.voltageRange = voltageRange;
        }

        @Override
        public double rawPositionForAngleInDegrees(double angle) {
            // Units: (0-1023) / 1023 x (turns/volt) x (volts) x (degrees/turn) = degrees
            // Units: (degrees) x (turns/degrees) x (1/volts) x (volts/turn) * 1023  = (0-1023)
            double relativeInput = angle / 360.0 / voltageRange / analogTurnsPerVolt * analogRange;
            return relativeInput + zero;
        }

        @Override
        public double angleInDegreesFromRawPosition(double position) {
            // Units: (0-1023) / 1023 x (turns/volt) x (volts) x (degrees/turn) = degrees
            return (position - zero) / analogRange * analogTurnsPerVolt * voltageRange * 360.0;
        }
        @Override
        public double getAngle() {
            return angleInDegreesFromRawPosition(analogPosition.getAsDouble());
        }

        @Override
        public double getRate() {
            // Units: (0-1023)/cycle / 1023 x (turns/volt) x (volts) x (degrees/turn) x (cycles/second) = (degrees/second)
            return changeInVoltsPerCycle.getAsDouble() / analogRange * analogTurnsPerVolt * voltageRange * 360.0
                    / cyclePeriodInSeconds.getAsDouble();
        }

        @Override
        public Gyroscope zero() {
            zero = analogPosition.getAsDouble();
            return this;
        }
    }

    protected static EncoderInputSensor encoderInput(DoubleSupplier positionInPulses, DoubleSupplier velocityInPulsesPerCycle,
            double pulsesPerDegree, int risesPerPulse, DoubleSupplier cyclePeriodInSeconds) {
        if ( pulsesPerDegree <= 0.0000001d && pulsesPerDegree >= 0.0000001d ) return null;
        return new EncoderInputSensor(positionInPulses, velocityInPulsesPerCycle, pulsesPerDegree, risesPerPulse, cyclePeriodInSeconds);
    }

    protected static AnalogInputSensor analogInput(DoubleSupplier analogPosition, DoubleSupplier changeInVoltsPerCycle,
            double analogRange, double analogTurnsPerVolt, double voltageRange, DoubleSupplier cyclesPeriodInSeconds) {
        if ( analogTurnsPerVolt <= 0.0000001d && analogTurnsPerVolt >= 0.0000001d ) return null;
        return new AnalogInputSensor(analogPosition, changeInVoltsPerCycle, analogRange, analogTurnsPerVolt, voltageRange, cyclesPeriodInSeconds);
    }

    private static final double DEFAULT_ANALOG_RATE = 0.100;
    private static final double DEFAULT_QUADRATURE_RATE = 0.100;
    private static final double DEFAULT_FEEDBACK_RATE = 0.020;

    private static final int RISES_PER_PULSE = 4; // 4x mode
    private static final double MAX_ANALOG_VOLTAGE = 3.3; // 0-3.3V
    private static final double MAX_ANALOG_RANGE = 1023; // 10 bits non-continuous

    //protected final TalonSRX talon;
    protected  final PhoenixTalonAdapter talon;
    protected final InputSensor encoderInput;
    protected final InputSensor analogInput;
    protected final InputSensor selectedEncoderInput;
    protected final InputSensor selectedAnalogInput;
    protected volatile InputSensor selectedInput = NO_OP_SENSOR;
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

    HardwareITalonSRX(TalonSRX talonSRX, double pulsesPerDegree, double analogTurnsOverVoltageRange) {
        this.talon = new PhoenixTalonAdapter( talonSRX );


        this.forwardLimitSwitch = talon::isFwdLimitSwitchClosed;
        this.reverseLimitSwitch = talon::isRevLimitSwitchClosed;
        this.outputCurrent = talon::getOutputCurrent;
        this.outputVoltage = talon::getOutputVoltage;
        this.busVoltage = talon::getBusVoltage;
        this.temperature = talon::getTemperature;
        this.encoderInput = encoderInput(talon::getEncPosition,
                                             talon::getEncVelocity,
                                             pulsesPerDegree,
                                             RISES_PER_PULSE,
                                             () -> quadratureRateInSeconds);
        this.analogInput = analogInput(talon::getAnalogInPosition,
                                           talon::getAnalogInVelocity,
                                           MAX_ANALOG_RANGE,
                                           analogTurnsOverVoltageRange / MAX_ANALOG_VOLTAGE,
                                           MAX_ANALOG_VOLTAGE,
                                           () -> analogRateInSeconds);
        this.selectedEncoderInput = encoderInput(talon::getPosition,
                                             talon::getSpeed,
                                             pulsesPerDegree,
                                             RISES_PER_PULSE,
                                             () -> feedbackRateInSeconds);
        this.selectedAnalogInput = analogInput(talon::getPosition,
                                           talon::getSpeed,
                                           MAX_ANALOG_RANGE,
                                           analogTurnsOverVoltageRange / MAX_ANALOG_VOLTAGE,
                                           MAX_ANALOG_VOLTAGE,
                                           () -> feedbackRateInSeconds);
        this.instantaneousFaults = new Faults() {
            @Override
            public Switch forwardLimitSwitch() {
                return () -> talon.getFaultForLim() ;
            }

            @Override
            public Switch reverseLimitSwitch() {
                return () -> talon.getFaultRevLim() ;
            }

            @Override
            public Switch forwardSoftLimit() {
                return () -> talon.getFaultForSoftLim() ;
            }

            @Override
            public Switch reverseSoftLimit() {
                return () -> talon.getFaultRevSoftLim() ;
            }

            @Override
            public Switch hardwareFailure() {
                return () -> talon.getFaultHardwareFailure() ;
            }

            @Override
            public Switch overTemperature() {
                return () -> talon.getFaultOverTemp() ;
            }

            @Override
            public Switch underVoltage() {
                return () -> talon.getFaultUnderVoltage() ;
            }
        };
        this.stickyFaults = new Faults() {
            @Override
            public Switch forwardLimitSwitch() {
                return () -> talon.getStickyFaultForLim() ;
            }

            @Override
            public Switch reverseLimitSwitch() {
                return () -> talon.getStickyFaultRevLim() ;
            }

            @Override
            public Switch forwardSoftLimit() {
                return () -> talon.getStickyFaultForSoftLim() ;
            }

            @Override
            public Switch reverseSoftLimit() {
                return () -> talon.getStickyFaultRevSoftLim() ;
            }

            @Override
            public Switch hardwareFailure() {
                return () -> talon.getFaultHardwareFailure() ; // no sticky version!
            }

            @Override
            public Switch overTemperature() {
                return () -> talon.getStickyFaultOverTemp() ;
            }

            @Override
            public Switch underVoltage() {
                return () -> talon.getStickyFaultUnderVoltage() ;
            }
        };
    }

    @Override
    public int getDeviceID() {
        return talon.getDeviceID();
    }

    @Override
    public double getSpeed() {
       // talon.changeControlMode(TalonControlMode.PercentVbus);
        return talon.getSpeed();
    }

    @Override
    public ITalonSRX setSpeed(double speed) {
        //talon.changeControlMode(TalonControlMode.PercentVbus);
        talon.setSpeed(speed);
        return this;
    }

    @Override
    public void stop() {
        talon.enableBrakeMode(true);
        //talon.set(0);
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
    public ITalonSRX setFeedbackDevice(FeedbackDevice device) {
        talon.setFeedbackDevice(device);
        switch(device) {
            case ANALOG_POTENTIOMETER:
            case ANALOG_ENCODER:
                if ( selectedAnalogInput != null ) {
                    selectedInput = selectedAnalogInput;
                } else {
                    Strongback.logger().error("Unable to use the analog input for feedback, since the Talon SRX (device " + getDeviceID() + ") was not instantiated with an analog input. Check how this device was created using Strongback's Hardware class.");
                    selectedInput = NO_OP_SENSOR;
                }
                break;
            case QUADRATURE_ENCODER:
            case ENCODER_RISING:
                if ( selectedEncoderInput != null ) {
                    selectedInput = selectedEncoderInput;
                } else {
                    Strongback.logger().error("Unable to use the quadrature encoder input for feedback, since the Talon SRX (device " + getDeviceID() + ") was not instantiated with an encoder input. Check how this device was created using Strongback's Hardware class.");
                    selectedInput = NO_OP_SENSOR;
                }
                break;
            case ENCODER_FALLING:
                // for 2015 the Talon SRX firmware did not support the falling or rising mode ...
                selectedInput = NO_OP_SENSOR;
                break;
            case MAGNETIC_ENCODER_ABSOLUTE:
                selectedInput = NO_OP_SENSOR;
                break;
            case MAGNETIC_ENCODER_RELATIVE:
                selectedInput = NO_OP_SENSOR;
                break;
            case PULSE_WIDTH:
                selectedInput = NO_OP_SENSOR;
                break;
        }
        return this;
    }

    @Override
    public ITalonSRX setStatusFrameRate(StatusFrameRate frameRate, int periodMillis) {
        talon.setStatusFrameRateMs(frameRate, periodMillis);
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
    public ITalonSRX reverseSensor(boolean flip) {
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
    public ITalonSRX setForwardSoftLimit(int forwardLimitDegrees) {
        // Compute the desired forward limit in terms of the current selected input sensor ...
        if ( this.selectedInput != null ) {
            double rawPosition = this.selectedInput.rawPositionForAngleInDegrees(forwardLimitDegrees);
            talon.setForwardSoftLimit(rawPosition);
        }
        return this;
    }

    @Override
    public HardwareITalonSRX enableForwardSoftLimit(boolean enable) {
        talon.enableForwardSoftLimit(enable);
        return this;
    }

    @Override
    public HardwareITalonSRX setReverseSoftLimit(int reverseLimitDegrees) {
        // Compute the desired reverse limit in terms of the current selected input sensor ...
        if ( this.selectedInput != null ) {
            double rawPosition = this.selectedInput.rawPositionForAngleInDegrees(reverseLimitDegrees);
            talon.setReverseSoftLimit(rawPosition);
        }
        return this;
    }

    @Override
    public HardwareITalonSRX enableReverseSoftLimit(boolean enable) {
        talon.enableReverseSoftLimit(enable);
        return this;
    }

    @Override
    public ITalonSRX enableLimitSwitch(boolean forward, boolean reverse) {
        talon.enableLimitSwitch(forward, reverse);
        return this;
    }

    @Override
    public ITalonSRX enableBrakeMode(boolean brake) {
        talon.enableBrakeMode(brake);
        return this;
    }

    @Override
    public ITalonSRX setForwardLimitSwitchNormallyOpen(boolean normallyOpen) {
        talon.ConfigFwdLimitSwitchNormallyOpen(normallyOpen);
        return this;
    }

    @Override
    public ITalonSRX setReverseLimitSwitchNormallyOpen(boolean normallyOpen) {
        talon.ConfigRevLimitSwitchNormallyOpen(normallyOpen);
        return this;
    }

    @Override
    public ITalonSRX setVoltageRampRate(double rampRate) {
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
    public ITalonSRX clearStickyFaults() {
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
    public ITalonSRX setSafetyEnabled(boolean enabled) {
        talon.setSafetyEnabled(enabled);
        return this;
    }

    @Override
    public double getExpiration() {
        return talon.getExpiration();
    }

    @Override
    public ITalonSRX setExpiration(double timeout) {
        talon.setExpiration(timeout);
        return this;
    }

    @Override
    public boolean isAlive() {
        return talon.isAlive();
    }
}