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

import org.strongback.annotation.Experimental;
import org.strongback.components.Accelerometer;
import org.strongback.components.AngleSensor;
import org.strongback.components.DistanceSensor;
import org.strongback.components.Gyroscope;
import org.strongback.components.Motor;
import org.strongback.components.PneumaticsModule;
import org.strongback.components.PowerPanel;
import org.strongback.components.Relay;
import org.strongback.components.Solenoid;
import org.strongback.components.Switch;
import org.strongback.components.TalonSRX;
import org.strongback.components.ThreeAxisAccelerometer;
import org.strongback.components.TwoAxisAccelerometer;
import org.strongback.components.ui.FlightStick;
import org.strongback.components.ui.Gamepad;
import org.strongback.components.ui.InputDevice;
import org.strongback.control.TalonController;
import org.strongback.function.DoubleToDoubleFunction;
import org.strongback.util.Values;

import com.ctre.CANTalon;
import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.ADXL345_I2C;
import edu.wpi.first.wpilibj.ADXL345_SPI;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.AnalogAccelerometer;
import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.AnalogTrigger;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.interfaces.Accelerometer.Range;
import edu.wpi.first.wpilibj.interfaces.Gyro;

/**
 * The factory methods that will create component implementations corresponding to physical hardware on the robot. Nested
 * classes help organize the methods into those for similar kinds of hardware.
 * <p>
 * As a general rule, it is recommended that any subsystem or other classes that make use of components should not know how to
 * obtain their components. Instead, the components should be created by the robot code and passed into the subsystems via
 * constructors; the subsystem references to components can be immutable and marked as final.
 * <p>
 * Doing this makes it very easy for the subsystems to be tested off-robot without hardware, since test cases can create mock
 * components (rather than the hardware components created by the methods in this class) and pass them into the subsystem
 * constructors.
 *
 * @author Zach Anderson
 * @author Randall Hauch
 */
public class Hardware {

    /**
     * Gets the {@link PowerPanel} of the robot.
     *
     * @return the {@link PowerPanel} of the robot
     */
    public static PowerPanel powerPanel() {
        PowerDistributionPanel pdp = new PowerDistributionPanel();
        return PowerPanel.create(pdp::getCurrent, pdp::getTotalCurrent, pdp::getVoltage, pdp::getTemperature);
    }

    /**
     * Gets the {@link PneumaticsModule} of the robot with the default CAN ID of 0.
     *
     * @return the {@link PneumaticsModule} of the robot; never null
     */
    public static PneumaticsModule pneumaticsModule() {
        return new HardwarePneumaticsModule(new Compressor());
    }

    /**
     * Gets the {@link PneumaticsModule} of the robot with the supplied CAN ID. Multiple pneumatics modules can be used by
     *
     * @param canID the CAN ID of the module
     * @return the {@link PneumaticsModule} of the robot; never null
     */
    public static PneumaticsModule pneumaticsModule(int canID) {
        return new HardwarePneumaticsModule(new Compressor(canID));
    }

    /**
     * Factory method for angle sensors.
     */
    public static final class AngleSensors {

        /**
         * Create a {@link Gyroscope} that uses a WPILib {@link AnalogGyro} on the specified channel.
         *
         * @param channel the channel the gyroscope is plugged into
         * @return the gyroscope; never null
         */
        public static Gyroscope gyroscope(int channel) {
            return gyroscope(new AnalogGyro(channel));
        }

        /**
         * Create a {@link Gyroscope} that uses a WPILib {@link ADXRS450_Gyro} on the specified SPI bus port.
         *
         * @param port the port on SPI bus into which the digital ADXRS450 gyroscope is connected
         * @return the gyroscope; never null
         */
        public static Gyroscope gyroscope(SPI.Port port) {
            return gyroscope(new ADXRS450_Gyro(port));
        }

        /**
         * Create a {@link Gyroscope} that uses the provided WPILib {@link Gyro}. Use this method if you need to
         * {@link Gyro#calibrate() calibrate} before using it.
         *
         * @param gyro the low-level WPILib gyroscope
         * @return the gyroscope; never null
         */
        public static Gyroscope gyroscope(Gyro gyro) {
            return Gyroscope.create(gyro::getAngle, gyro::getRate);
        }

        /**
         * Creates a new {@link AngleSensor} from an {@link Encoder} using the specified channels with the specified distance
         * per pulse.
         *
         * @param aChannel the a channel of the encoder
         * @param bChannel the b channel of the encoder
         * @param distancePerPulse the distance the end shaft spins per pulse
         * @return the angle sensor; never null
         */
        public static AngleSensor encoder(int aChannel, int bChannel, double distancePerPulse) {
            Encoder encoder = new Encoder(aChannel, bChannel);
            encoder.setDistancePerPulse(distancePerPulse);
            return AngleSensor.create(encoder::getDistance);
        }

        /**
         * Create a new {@link AngleSensor} from an {@link AnalogPotentiometer} using the specified channel and scaling. Since
         * no offset is provided, the resulting angle sensor may often be used with a limit switch to know precisely where a
         * mechanism might be located in space, and the angle sensor can be {@link AngleSensor#zero() zeroed} at that position.
         * (See {@link #potentiometer(int, double, double)} when another switch is not used to help determine the location, and
         * instead the zero point is pre-determined by the physical design of the mechanism.)
         * <p>
         * The scale factor multiplies the 0-1 ratiometric value to return the angle in degrees.
         * <p>
         * For example, let's say you have an ideal 10-turn linear potentiometer attached to a motor attached by chains and a
         * 25x gear reduction to an arm. If the potentiometer (attached to the motor shaft) turned its full 3600 degrees, the
         * arm would rotate 144 degrees. Therefore, the {@code fullVoltageRangeToInches} scale factor is
         * {@code 144 degrees / 5 V}, or {@code 28.8 degrees/volt}.
         *
         * @param channel The analog channel this potentiometer is plugged into.
         * @param fullVoltageRangeToDegrees The scaling factor multiplied by the analog voltage value to obtain the angle in
         *        degrees.
         * @return the angle sensor that uses the potentiometer on the given channel; never null
         */
        public static AngleSensor potentiometer(int channel, double fullVoltageRangeToDegrees) {
            return potentiometer(channel, fullVoltageRangeToDegrees, 0.0);
        }

        /**
         * Create a new {@link AngleSensor} from an {@link AnalogPotentiometer} using the specified channel, scaling, and
         * offset. This method is often used when the offset can be hard-coded by measuring the value of the potentiometer at
         * the mechanism's zero-point. On the other hand, if a limit switch is used to always determine the position of the
         * mechanism upon startup, then see {@link #potentiometer(int, double)}.
         * <p>
         * The scale factor multiplies the 0-1 ratiometric value to return the angle in degrees.
         * <p>
         * For example, let's say you have an ideal 10-turn linear potentiometer attached to a motor attached by chains and a
         * 25x gear reduction to an arm. If the potentiometer (attached to the motor shaft) turned its full 3600 degrees, the
         * arm would rotate 144 degrees. Therefore, the {@code fullVoltageRangeToInches} scale factor is
         * {@code 144 degrees / 5 V}, or {@code 28.8 degrees/volt}.
         * <p>
         * To prevent the potentiometer from breaking due to minor shifting in alignment of the mechanism, the potentiometer may
         * be installed with the "zero-point" of the mechanism (e.g., arm in this case) a little ways into the potentiometer's
         * range (for example 30 degrees). In this case, the {@code offset} value of {@code 30} is determined from the
         * mechanical design.
         *
         * @param channel The analog channel this potentiometer is plugged into.
         * @param fullVoltageRangeToDegrees The scaling factor multiplied by the analog voltage value to obtain the angle in
         *        degrees.
         * @param offsetInDegrees The offset in degrees that the angle sensor will subtract from the underlying value before
         *        returning the angle
         * @return the angle sensor that uses the potentiometer on the given channel; never null
         */
        public static AngleSensor potentiometer(int channel, double fullVoltageRangeToDegrees, double offsetInDegrees) {
            AnalogPotentiometer pot = new AnalogPotentiometer(channel, fullVoltageRangeToDegrees, offsetInDegrees);
            return AngleSensor.create(pot::get);
        }
    }

    /**
     * Factory method for accelerometers.
     */
    public static final class Accelerometers {
        /**
         * Create a new {@link ThreeAxisAccelerometer} for the ADXL345 with the desired range using the specified I2C port.
         *
         * @param port the I2C port used by the accelerometer
         * @param range the desired range of the accelerometer
         * @return the accelerometer; never null
         */
        public static ThreeAxisAccelerometer accelerometer(I2C.Port port, Range range) {
            if (port == null) throw new IllegalArgumentException("The I2C port must be specified");
            if (range == null) throw new IllegalArgumentException("The accelerometer range must be specified");
            ADXL345_I2C accel = new ADXL345_I2C(port, range);
            return ThreeAxisAccelerometer.create(accel::getX, accel::getY, accel::getZ);
        }

        /**
         * Create a new {@link ThreeAxisAccelerometer} for the ADXL345 with the desired range using the specified SPI port.
         *
         * @param port the SPI port used by the accelerometer
         * @param range the desired range of the accelerometer
         * @return the accelerometer; never null
         */
        public static ThreeAxisAccelerometer accelerometer(SPI.Port port, Range range) {
            if (port == null) throw new IllegalArgumentException("The I2C port must be specified");
            if (range == null) throw new IllegalArgumentException("The accelerometer range must be specified");
            ADXL345_SPI accel = new ADXL345_SPI(port, range);
            return ThreeAxisAccelerometer.create(accel::getX, accel::getY, accel::getZ);
        }

        /**
         * Create a new {@link ThreeAxisAccelerometer} using the RoboRIO's built-in accelerometer.
         *
         * @return the accelerometer; never null
         */
        public static ThreeAxisAccelerometer builtIn() {
            BuiltInAccelerometer accel = new BuiltInAccelerometer();
            return ThreeAxisAccelerometer.create(accel::getX, accel::getY, accel::getZ);
        }

        /**
         * Create a new single-axis {@link Accelerometer} using the {@link AnalogAccelerometer} on the specified channel, with
         * has the given sensitivity and zero value.
         *
         * @param channel the channel for the analog accelerometer
         * @param sensitivity the desired sensitivity in Volts per G (depends on actual hardware, such as 18mV/g or
         *        {@code 0.018} for ADXL193)
         * @param zeroValueInVolts the voltage that represents no acceleration (should be determine experimentally)
         * @return the accelerometer; never null
         */
        public static Accelerometer analogAccelerometer(int channel, double sensitivity, double zeroValueInVolts) {
            AnalogAccelerometer accel = new AnalogAccelerometer(channel);
            accel.setSensitivity(sensitivity);
            accel.setZero(zeroValueInVolts);
            return accel::getAcceleration;
        }

        /**
         * Create a new single-axis {@link Accelerometer} using two {@link AnalogAccelerometer}s on the specified channels, with
         * each have the given sensitivity and zero value.
         *
         * @param xAxisChannel the channel for the X-axis analog accelerometer
         * @param yAxisChannel the channel for the Y-axis analog accelerometer
         * @param sensitivity the desired sensitivity in Volts per G (depends on actual hardware, such as 18mV/g or
         *        {@code 0.018} for ADXL193)
         * @param zeroValueInVolts the voltage that represents no acceleration (should be determine experimentally)
         * @return the accelerometer; never null
         */
        public static TwoAxisAccelerometer analogAccelerometer(int xAxisChannel, int yAxisChannel, double sensitivity,
                double zeroValueInVolts) {
            if (xAxisChannel == yAxisChannel) throw new IllegalArgumentException(
                    "The x- and y-axis channels may not be the same");
            Accelerometer xAxis = analogAccelerometer(xAxisChannel, sensitivity, zeroValueInVolts);
            Accelerometer yAxis = analogAccelerometer(yAxisChannel, sensitivity, zeroValueInVolts);
            return TwoAxisAccelerometer.create(xAxis::getAcceleration, yAxis::getAcceleration);
        }
    }

    /**
     * Factory method for different kinds of switches.
     */
    public static final class Switches {

        /**
         * Create a generic normally closed digital switch sensor on the specified digital channel.
         *
         * @param channel the channel the switch is connected to
         * @return a switch on the specified channel
         */
        public static Switch normallyClosed(int channel) {
            DigitalInput input = new DigitalInput(channel);
            return () -> !input.get();
        }

        /**
         * Create a generic normally open digital switch sensor on the specified digital channel.
         *
         * @param channel the channel the switch is connected to
         * @return a switch on the specified channel
         */
        public static Switch normallyOpen(int channel) {
            DigitalInput input = new DigitalInput(channel);
            return input::get;
        }

        /**
         * Option for analog switches.
         *
         * @see Switches#analog(int, double, double, AnalogOption, TriggerMode)
         */
        public static enum AnalogOption {
            /**
             * The filtering option of the analog trigger uses a 3-point average reject filter. This filter uses a circular
             * buffer of the last three data points and selects the outlier point nearest the median as the output. The primary
             * use of this filter is to reject data points which errantly (due to averaging or sampling) appear within the
             * window when detecting transitions using the Rising Edge and Falling Edge functionality of the analog trigger
             */
            FILTERED,
            /**
             * The analog output is averaged and over sampled.
             */
            AVERAGED,
            /**
             * No filtering or averaging is to be used.
             */
            NONE;
        }

        /**
         * Trigger mode for analog switches.
         *
         * @see Switches#analog(int, double, double, AnalogOption, TriggerMode)
         */
        public static enum TriggerMode {
            /**
             * The switch is triggered only when the analog value is inside the range, and not triggered if it is outside (above
             * or below)
             */
            IN_WINDOW,
            /**
             * The switch is triggered only when the value is above the upper limit, and not triggered if it is below the lower
             * limit and maintains the previous state if in between (hysteresis)
             */
            AVERAGED;
        }

        /**
         * Create an analog switch sensor that is triggered when the value exceeds the specified upper voltage and that is no
         * longer triggered when the value drops below the specified lower voltage.
         *
         * @param channel the port to use for the analog trigger 0-3 are on-board, 4-7 are on the MXP port
         * @param lowerVoltage the lower voltage limit that below which will result in the switch no longer being triggered
         * @param upperVoltage the upper voltage limit that above which will result in triggering the switch
         * @param option the trigger option; may not be null
         * @param mode the trigger mode; may not be null
         * @return the analog switch; never null
         */
        public static Switch analog(int channel, double lowerVoltage, double upperVoltage, AnalogOption option,
                TriggerMode mode) {
            if (option == null) throw new IllegalArgumentException("The analog option must be specified");
            if (mode == null) throw new IllegalArgumentException("The analog mode must be specified");
            AnalogTrigger trigger = new AnalogTrigger(channel);
            trigger.setLimitsVoltage(lowerVoltage, upperVoltage);
            switch (option) {
                case AVERAGED:
                    trigger.setAveraged(true);
                    break;
                case FILTERED:
                    trigger.setFiltered(true);
                    break;
                case NONE:
                    break;
            }
            return mode == TriggerMode.AVERAGED ? trigger::getTriggerState : trigger::getInWindow;
        }
    }

    /**
     * Factory method for distance sensors.
     */
    public static final class DistanceSensors {
        /**
         * Create a digital ultrasonic {@link DistanceSensor} for an {@link Ultrasonic} sensor that uses the specified channels.
         *
         * @param pingChannel the digital output channel to use for sending pings
         * @param echoChannel the digital input channel to use for receiving echo responses
         * @return a {@link DistanceSensor} linked to the specified channels
         */
        public static DistanceSensor digitalUltrasonic(int pingChannel, int echoChannel) {
            Ultrasonic ultrasonic = new Ultrasonic(pingChannel, echoChannel);
            ultrasonic.setAutomaticMode(true);
            return DistanceSensor.create(ultrasonic::getRangeInches);
        }

        /**
         * Create an analog {@link DistanceSensor} for an {@link AnalogInput} sensor using the specified channel.
         *
         * @param channel the channel the sensor is connected to
         * @param voltsToInches the conversion from analog volts to inches
         * @return a {@link DistanceSensor} linked to the specified channel
         */
        public static DistanceSensor analogUltrasonic(int channel, double voltsToInches) {
            AnalogInput sensor = new AnalogInput(channel);
            return DistanceSensor.create(() -> sensor.getVoltage() * voltsToInches);
        }

        /**
         * Create a new {@link DistanceSensor} from an {@link AnalogPotentiometer} using the specified channel and scaling.
         * Since no offset is provided, the resulting distance sensor may often be used with a limit switch to know precisely
         * where a mechanism might be located in space, and the distance sensor can be {@link DistanceSensor#zero() zeroed} at
         * that position. (See {@link #potentiometer(int, double, double)} when another switch is not used to help determine the
         * location, and instead the zero point is pre-determined by the physical design of the mechanism.)
         * <p>
         * The scale factor multiplies the 0-1 ratiometric value to return useful units. Generally, the most useful scale factor
         * will be the angular or linear full scale of the potentiometer.
         * <p>
         * For example, let's say you have an ideal single-turn linear potentiometer attached to a robot arm. This pot will turn
         * 270 degrees over the 0V-5V range while the end of the arm travels 20 inches. Therefore, the
         * {@code fullVoltageRangeToInches} scale factor is {@code 20 inches / 5 V}, or {@code 4 inches/volt}.
         *
         * @param channel The analog channel this potentiometer is plugged into.
         * @param fullVoltageRangeToInches The scaling factor multiplied by the analog voltage value to obtain inches.
         * @return the distance sensor that uses the potentiometer on the given channel; never null
         */
        public static DistanceSensor potentiometer(int channel, double fullVoltageRangeToInches) {
            return potentiometer(channel, fullVoltageRangeToInches, 0.0);
        }

        /**
         * Create a new {@link DistanceSensor} from an {@link AnalogPotentiometer} using the specified channel, scaling, and
         * offset. This method is often used when the offset can be hard-coded by first measuring the value of the potentiometer
         * at the mechanism's zero-point. On the other hand, if a limit switch is used to always determine the position of the
         * mechanism upon startup, then see {@link #potentiometer(int, double)}.
         * <p>
         * The scale factor multiplies the 0-1 ratiometric value to return useful units, and an offset to add after the scaling.
         * Generally, the most useful scale factor will be the angular or linear full scale of the potentiometer.
         * <p>
         * For example, let's say you have an ideal single-turn linear potentiometer attached to a robot arm. This pot will turn
         * 270 degrees over the 0V-5V range while the end of the arm travels 20 inches. Therefore, the
         * {@code fullVoltageRangeToInches} scale factor is {@code 20 inches / 5 V}, or {@code 4 inches/volt}.
         * <p>
         * To prevent the potentiometer from breaking due to minor shifting in alignment of the mechanism, the potentiometer may
         * be installed with the "zero-point" of the mechanism (e.g., arm in this case) a little ways into the potentiometer's
         * range (for example 10 degrees). In this case, the {@code offset} value is measured from the physical mechanical
         * design and can be specified to automatically remove the 10 degrees from the potentiometer output.
         *
         * @param channel The analog channel this potentiometer is plugged into.
         * @param fullVoltageRangeToInches The scaling factor multiplied by the analog voltage value to obtain inches.
         * @param offsetInInches The offset in inches that the distance sensor will subtract from the underlying value before
         *        returning the distance
         * @return the distance sensor that uses the potentiometer on the given channel; never null
         */
        public static DistanceSensor potentiometer(int channel, double fullVoltageRangeToInches, double offsetInInches) {
            AnalogPotentiometer pot = new AnalogPotentiometer(channel, fullVoltageRangeToInches, offsetInInches);
            return DistanceSensor.create(pot::get);
        }
    }

    /**
     * Factory method for different kinds of motors.
     */
    public static final class Motors {

        private static final DoubleToDoubleFunction SPEED_LIMITER = Values.limiter(-1.0, 1.0);

        /**
         * Create a motor driven by a Talon speed controller on the specified channel. The speed output is limited to [-1.0,1.0]
         * inclusive.
         *
         * @param channel the channel the controller is connected to
         * @return a motor on the specified channel
         */
        public static Motor talon(int channel) {
            return talon(channel, SPEED_LIMITER);
        }

        /**
         * Create a motor driven by a Talon speed controller on the specified channel, with a custom speed limiting function.
         *
         * @param channel the channel the controller is connected to
         * @param speedLimiter function that will be used to limit the speed; may not be null
         * @return a motor on the specified channel
         */
        public static Motor talon(int channel, DoubleToDoubleFunction speedLimiter) {
            return new HardwareMotor(new Talon(channel), speedLimiter);
        }

        /**
         * Create a motor driven by a Jaguar speed controller on the specified channel. The speed output is limited to
         * [-1.0,1.0] inclusive.
         *
         * @param channel the channel the controller is connected to
         * @return a motor on the specified channel
         */
        public static Motor jaguar(int channel) {
            return jaguar(channel, SPEED_LIMITER);
        }

        /**
         * Create a motor driven by a Jaguar speed controller on the specified channel, with a custom speed limiting function
         *
         * @param channel the channel the controller is connected to
         * @param speedLimiter function that will be used to limit the speed; may not be null
         * @return a motor on the specified channel
         */
        public static Motor jaguar(int channel, DoubleToDoubleFunction speedLimiter) {
            return new HardwareMotor(new Jaguar(channel), SPEED_LIMITER);
        }

        /**
         * Create a motor driven by a Victor speed controller on the specified channel. The speed output is limited to
         * [-1.0,1.0] inclusive.
         *
         * @param channel the channel the controller is connected to
         * @return a motor on the specified channel
         */
        public static Motor victor(int channel) {
            return victor(channel, SPEED_LIMITER);
        }

        /**
         * Create a motor driven by a Victor speed controller on the specified channel, with a custom speed limiting function
         *
         * @param channel the channel the controller is connected to
         * @param speedLimiter function that will be used to limit the speed (input voltage); may not be null
         * @return a motor on the specified channel
         */
        public static Motor victor(int channel, DoubleToDoubleFunction speedLimiter) {
            return new HardwareMotor(new Victor(channel), SPEED_LIMITER);
        }

        /**
         * Create a motor driven by a VEX Robotics Victor SP speed controller on the specified channel. The speed output is
         * limited to [-1.0,1.0] inclusive.
         *
         * @param channel the channel the controller is connected to
         * @return a motor on the specified channel
         */
        public static Motor victorSP(int channel) {
            return victorSP(channel, SPEED_LIMITER);
        }

        /**
         * Create a motor driven by a VEX Robotics Victor SP speed controller on the specified channel, with a custom speed
         * limiting function.
         *
         * @param channel the channel the controller is connected to
         * @param speedLimiter function that will be used to limit the speed (input voltage); may not be null
         * @return a motor on the specified channel
         */
        public static Motor victorSP(int channel, DoubleToDoubleFunction speedLimiter) {
            return new HardwareMotor(new VictorSP(channel), speedLimiter);
        }

        /**
         * Creates a {@link TalonSRX} motor controlled by a Talon SRX with no sensors wired as inputs.
         * <p>
         * The resulting {@link TalonSRX} will have a null {@link TalonSRX#getAnalogInput()} and a null
         * {@link TalonSRX#getEncoderInput()}.
         *
         * @param deviceNumber the CAN device number for the Talon SRX; may not be null
         * @return a {@link TalonSRX} motor; never null
         */
        public static TalonSRX talonSRX(int deviceNumber) {
            return talonSRX(deviceNumber, 0.0d, 0.0d);
        }

        /**
         * Creates a {@link TalonSRX} motor controlled by a Talon SRX with an optional quadrature encoder and no analog input
         * wired into the Talon.
         * <p>
         * The resulting {@link TalonSRX} will have a non-null {@link TalonSRX#getEncoderInput()} when the
         * <code>pulsesPerDegree</code> is non-zero. But the resulting {@link TalonSRX} will always have a null
         * {@link TalonSRX#getAnalogInput()}.
         *
         * @param deviceNumber the CAN device number for the Talon SRX; may not be null
         * @param pulsesPerDegree the number of encoder pulses per degree of revolution of the final shaft; may be 0 if unused
         * @return a {@link TalonSRX} motor; never null
         */
        @Experimental
        public static TalonSRX talonSRX(int deviceNumber, double pulsesPerDegree) {
            return talonSRX(deviceNumber, pulsesPerDegree, 0.0d);
        }

        /**
         * Creates a {@link TalonSRX} motor controlled by a Talon SRX with an optional quadrature encoder and no an analog 3.3V
         * input wired into the Talon.
         * <p>
         * The resulting {@link TalonSRX} will have a non-null {@link TalonSRX#getEncoderInput()} when the
         * <code>pulsesPerDegree</code> is non-zero. Likewise, the resulting {@link TalonSRX} will have a non-null
         * {@link TalonSRX#getAnalogInput()} when the <code>analogTurnsOverVoltageRange</code> is non-zero.
         *
         * @param deviceNumber the CAN device number for the Talon SRX; may not be null
         * @param pulsesPerDegree the number of encoder pulses per degree of revolution of the final shaft; may be 0 if unused
         * @param analogTurnsOverVoltageRange the number of turns of an analog pot or analog encoder over the 0-3.3V range; may
         *        be 0 if unused
         * @return a {@link TalonSRX} motor; never null
         */
        @Experimental
        public static TalonSRX talonSRX(int deviceNumber, double pulsesPerDegree, double analogTurnsOverVoltageRange) {
            CANTalon talon = new CANTalon(deviceNumber);
            return talonSRX(talon, pulsesPerDegree, analogTurnsOverVoltageRange);
        }

        /**
         * Creates a {@link TalonSRX} motor controlled by a Talon SRX. The {@link CANTalon} object passed into this method
         * should be already configured by the calling code.
         * <p>
         * The resulting {@link TalonSRX} will have a null {@link TalonSRX#getEncoderInput()} and a null
         * {@link TalonSRX#getAnalogInput()}, and the {@link TalonSRX#getSelectedSensor()} will always return 0.
         *
         * @param talon the already configured {@link CANTalon} instance; may not be null
         * @return a {@link TalonSRX} motor; never null
         */
        public static TalonSRX talonSRX(CANTalon talon) {
            return talonSRX(talon, 0.0, 0.0d);
        }

        /**
         * Creates a {@link TalonSRX} motor controlled by a Talon SRX with an optional (angle) sensor. The {@link CANTalon}
         * object passed into this method should be already configured by the calling code.
         * <p>
         * The resulting {@link TalonSRX} will have a non-null {@link TalonSRX#getEncoderInput()} when the
         * <code>pulsesPerDegree</code> is non-zero. But the resulting {@link TalonSRX} will always have a null
         * {@link TalonSRX#getAnalogInput()}.
         *
         * @param talon the already configured {@link CANTalon} instance; may not be null
         * @param pulsesPerDegree the number of encoder pulses per degree of revolution of the final shaft
         * @return a {@link TalonSRX} motor; never null
         */
        @Experimental
        public static TalonSRX talonSRX(CANTalon talon, double pulsesPerDegree) {
            return talonSRX(talon, pulsesPerDegree, 0.0d);
        }

        /**
         * Creates a {@link TalonSRX} motor controlled by a Talon SRX with and optional quadrature encoder and/or an analog 3.3V
         * input wired into the Talon. The {@link CANTalon} object passed into this method should be already configured by the
         * calling code.
         * <p>
         * The resulting {@link TalonSRX} will have a non-null {@link TalonSRX#getEncoderInput()} when the
         * <code>pulsesPerDegree</code> is non-zero. Likewise, the resulting {@link TalonSRX} will have a non-null
         * {@link TalonSRX#getAnalogInput()} when the <code>analogTurnsOverVoltageRange</code> is non-zero.
         *
         * @param talon the already configured {@link CANTalon} instance; may not be null
         * @param pulsesPerDegree the number of encoder pulses per degree of revolution of the final shaft
         * @param analogTurnsOverVoltageRange the number of turns of an analog pot or analog encoder over the 0-3.3V range; may
         *        be 0 if unused
         * @return a {@link TalonSRX} motor; never null
         */
        @Experimental
        public static TalonSRX talonSRX(CANTalon talon, double pulsesPerDegree, double analogTurnsOverVoltageRange) {
            if (talon == null) throw new IllegalArgumentException("The CANTalon reference may not be null");
            return new HardwareTalonSRX(talon, pulsesPerDegree, analogTurnsOverVoltageRange);
        }

        /**
         * Creates a {@link TalonSRX} motor controller that follows another Talon SRX. The resulting TalonSRX will have neither
         * a {@link TalonSRX#getAnalogInput()} or a {@link TalonSRX#getEncoderInput()}.
         *
         * @param deviceNumber the CAN device number for the Talon SRX; may not be null
         * @param leader the Talon SRX that is to be followed; may not be null
         * @param reverse <code>true</code> if the resulting Talon should have inverted output compared to the leader, or
         *        <code>false</code> if the output should exactly match the leader
         * @return a {@link TalonSRX} motor controller that follows the leader; never null
         */
        public static TalonSRX talonSRX(int deviceNumber, TalonSRX leader, boolean reverse) {
            CANTalon talon = new CANTalon(deviceNumber);
            talon.changeControlMode(TalonControlMode.Follower);
            talon.set(leader.getDeviceID());
            talon.reverseOutput(reverse);
            return talonSRX(talon, 0.0d, 0.0d);
        }

        /**
         * Create a motor driven by a <a href="http://www.revrobotics.com/SPARK">RevRobotics Spark Motor Controller</a> on the
         * specified channel. The speed output is limited to [-1.0,1.0] inclusive.
         *
         * @param channel the channel the controller is connected to
         * @return a motor on the specified channel
         */
        public static Motor spark(int channel) {
            return spark(channel, SPEED_LIMITER);
        }

        /**
         * Create a motor driven by a <a href="http://www.revrobotics.com/SPARK">RevRobotics Spark Motor Controller</a> on the
         * specified channel, with a custom speed limiting function.
         *
         * @param channel the channel the controller is connected to
         * @param speedLimiter function that will be used to limit the speed (input voltage); may not be null
         * @return a motor on the specified channel
         */
        public static Motor spark(int channel, DoubleToDoubleFunction speedLimiter) {
            return new HardwareSpark(new Spark(channel), SPEED_LIMITER);
        }
    }

    /**
     * Factory method for hardware-based controllers.
     */
    public static final class Controllers {

        /**
         * Create a component that manages and uses the hardware-based PID controller on the Talon SRX with a quadrature encoder
         * and/or an analog 3.3V input sensor wired into the Talon.
         * <p>
         * The resulting {@link TalonSRX} will have a non-null {@link TalonSRX#getEncoderInput()} when the
         * <code>pulsesPerDegree</code> is non-zero. Likewise, the resulting {@link TalonSRX} will have a non-null
         * {@link TalonSRX#getAnalogInput()} when the <code>analogTurnsOverVoltageRange</code> is non-zero.
         *
         * @param deviceNumber the CAN device number; may not be null
         * @param pulsesPerDegree the number of encoder pulses per degree of revolution of the final shaft
         * @param analogTurnsOverVoltageRange the number of turns of an analog pot or analog encoder over the 0-3.3V range; may
         *        be 0 if unused
         * @return the interface for managing and using the Talon SRX hardware-based PID controller; never null
         */
        @Experimental
        public static TalonController talonController(int deviceNumber, double pulsesPerDegree,
                double analogTurnsOverVoltageRange) {
            CANTalon talon = new CANTalon(deviceNumber);
            HardwareTalonController c = new HardwareTalonController(talon, pulsesPerDegree, analogTurnsOverVoltageRange);
            return c;
        }
    }

    /**
     * Factory methods for solenoids.
     */
    public static final class Solenoids {
        /**
         * Create a double-acting solenoid that uses the specified channels on the default module.
         *
         * @param extendChannel the channel that extends the solenoid
         * @param retractChannel the channel that retracts the solenoid
         * @param initialDirection the initial direction for the solenoid; may not be null
         * @return a solenoid on the specified channels; never null
         */
        public static Solenoid doubleSolenoid(int extendChannel, int retractChannel, Solenoid.Direction initialDirection) {
            DoubleSolenoid solenoid = new DoubleSolenoid(extendChannel, retractChannel);
            return new HardwareDoubleSolenoid(solenoid, initialDirection);
        }

        /**
         * Create a double-acting solenoid that uses the specified channels on the given module.
         *
         * @param module the module for the channels
         * @param extendChannel the channel that extends the solenoid
         * @param retractChannel the channel that retracts the solenoid
         * @param initialDirection the initial direction for the solenoid; may not be null
         * @return a solenoid on the specified channels; never null
         */
        public static Solenoid doubleSolenoid(int module, int extendChannel, int retractChannel,
                Solenoid.Direction initialDirection) {
            DoubleSolenoid solenoid = new DoubleSolenoid(module, extendChannel, retractChannel);
            return new HardwareDoubleSolenoid(solenoid, initialDirection);
        }

        /**
         * Create a relay on the specified channel.
         *
         * @param channel the channel the relay is connected to
         * @return a relay on the specified channel
         */
        public static Relay relay(int channel) {
            return new HardwareRelay(channel);
        }

    }

    public static final class HumanInterfaceDevices {

        private static void verifyJoystickConnected(Joystick joystick) {
            joystick.getButtonCount();
        }

        /**
         * Create an generic input device controlled by the Driver Station.
         *
         * @param port the port on the driver station that the joystick is plugged into
         * @return the input device; never null
         */
        public static InputDevice driverStationJoystick(int port) {
            Joystick joystick = new Joystick(port);
            verifyJoystickConnected(joystick);
            return InputDevice.create(joystick::getRawAxis, joystick::getRawButton, joystick::getPOV);
        }

        /**
         * Create a Logitech Attack 3 flight stick controlled by the Driver Station.
         *
         * @param port the port on the driver station that the flight stick is plugged into
         * @return the input device; never null
         */
        public static FlightStick logitechAttack3D(int port) {
            Joystick joystick = new Joystick(port);
            verifyJoystickConnected(joystick);
            return FlightStick.create(joystick::getRawAxis,
                                      joystick::getRawButton,
                                      joystick::getPOV,
                                      joystick::getY, // pitch
                                      () -> joystick.getTwist() * -1, // yaw is reversed
                                      joystick::getX, // roll
                                      joystick::getThrottle, // throttle
                                      () -> joystick.getRawButton(1), // trigger
                                      () -> joystick.getRawButton(2)); // thumb
        }

        /**
         * Create a Logitech Extreme 3D flight stick controlled by the Driver Station.
         *
         * @param port the port on the driver station that the flight stick is plugged into
         * @return the input device; never null
         */
        public static FlightStick logitechExtreme3D(int port) {
            Joystick joystick = new Joystick(port);
            verifyJoystickConnected(joystick);
            return FlightStick.create(joystick::getRawAxis,
                                      joystick::getRawButton,
                                      joystick::getPOV,
                                      joystick::getY, // pitch
                                      joystick::getTwist, // yaw
                                      joystick::getX, // roll
                                      joystick::getThrottle, // flapper thing on bottom
                                      () -> joystick.getRawButton(1), // trigger
                                      () -> joystick.getRawButton(2)); // thumb
        }

        /**
         * Create a Microsoft SideWinder flight stick controlled by the Driver Station.
         *
         * @param port the port on the driver station that the flight stick is plugged into
         * @return the input device; never null
         */
        public static FlightStick microsoftSideWinder(int port) {
            Joystick joystick = new Joystick(port);
            verifyJoystickConnected(joystick);
            return FlightStick.create(joystick::getRawAxis,
                                      joystick::getRawButton,
                                      joystick::getPOV,
                                      () -> joystick.getY() * -1, // pitch is reversed
                                      joystick::getTwist, // yaw
                                      joystick::getX, // roll
                                      joystick::getThrottle, // throttle
                                      () -> joystick.getRawButton(1), // trigger
                                      () -> joystick.getRawButton(2)); // thumb
        }

        /**
         * Create a Logitech DualAction gamepad controlled by the Driver Station.
         *
         * @param port the port on the driver station that the gamepad is plugged into
         * @return the input device; never null
         */
        public static Gamepad logitechDualAction(int port) {
            Joystick joystick = new Joystick(port);
            verifyJoystickConnected(joystick);
            return Gamepad.create(joystick::getRawAxis,
                                  joystick::getRawButton,
                                  joystick::getPOV,
                                  () -> joystick.getRawAxis(0),
                                  () -> joystick.getRawAxis(1) * -1,
                                  () -> joystick.getRawAxis(2),
                                  () -> joystick.getRawAxis(3) * -1,
                                  () -> joystick.getRawButton(6) ? 1.0 : 0.0,
                                  () -> joystick.getRawButton(7) ? 1.0 : 0.0,
                                  () -> joystick.getRawButton(4),
                                  () -> joystick.getRawButton(5),
                                  () -> joystick.getRawButton(1),
                                  () -> joystick.getRawButton(2),
                                  () -> joystick.getRawButton(0),
                                  () -> joystick.getRawButton(3),
                                  () -> joystick.getRawButton(9),
                                  () -> joystick.getRawButton(8),
                                  () -> joystick.getRawButton(10),
                                  () -> joystick.getRawButton(11));
        }

        /**
         * Create a Logitech F310 gamepad controlled by the Driver Station.
         *
         * @param port the port on the driver station that the gamepad is plugged into
         * @return the input device; never null
         */
        public static Gamepad logitechF310(int port) {
            Joystick joystick = new Joystick(port);
            verifyJoystickConnected(joystick);
            return Gamepad.create(joystick::getRawAxis,
                                  joystick::getRawButton,
                                  joystick::getPOV,
                                  () -> joystick.getRawAxis(0),
                                  () -> joystick.getRawAxis(1) * -1,
                                  () -> joystick.getRawAxis(4),
                                  () -> joystick.getRawAxis(5) * -1,
                                  () -> joystick.getRawAxis(2),
                                  () -> joystick.getRawAxis(3),
                                  () -> joystick.getRawButton(4),
                                  () -> joystick.getRawButton(5),
                                  () -> joystick.getRawButton(0),
                                  () -> joystick.getRawButton(1),
                                  () -> joystick.getRawButton(2),
                                  () -> joystick.getRawButton(3),
                                  () -> joystick.getRawButton(7),
                                  () -> joystick.getRawButton(6),
                                  () -> joystick.getRawButton(8),
                                  () -> joystick.getRawButton(9));
        }

        /**
         * Create a Microsoft Xbox360 gamepad controlled by the Driver Station.
         *
         * @param port the port on the driver station that the gamepad is plugged into
         * @return the input device; never null
         */
        public static Gamepad xbox360(int port) {
            Joystick joystick = new Joystick(port);
            verifyJoystickConnected(joystick);
            return Gamepad.create(joystick::getRawAxis,
                                  joystick::getRawButton,
                                  joystick::getPOV,
                                  () -> joystick.getRawAxis(0),
                                  () -> joystick.getRawAxis(1) * -1,
                                  () -> joystick.getRawAxis(4),
                                  () -> joystick.getRawAxis(5) * -1,
                                  () -> joystick.getRawAxis(2),
                                  () -> joystick.getRawAxis(3),
                                  () -> joystick.getRawButton(5),
                                  () -> joystick.getRawButton(6),
                                  () -> joystick.getRawButton(1),
                                  () -> joystick.getRawButton(2),
                                  () -> joystick.getRawButton(3),
                                  () -> joystick.getRawButton(4),
                                  () -> joystick.getRawButton(8),
                                  () -> joystick.getRawButton(7),
                                  () -> joystick.getRawButton(9),
                                  () -> joystick.getRawButton(10));
        }
    }
}
