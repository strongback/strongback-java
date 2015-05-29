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

/**
 * This package defines a set of components that mirror real actuator, sensors, and other devices. The defined interfaces
 * are minimal abstractions of these physical components, and are intended to have only those methods required by
 * traditional subsystems and commands to make use of the components.
 * Robot code can create {@link org.strongback.hardware.Hardware hardware implementations} and pass them to the subsystems,
 * while test code can create other mock implementations.
 * <h2>Low-level components</h2>
 * <p>The following are the low-level abstractions of physical components:
 * <ul>
 * <li>{@link org.strongback.components.AngleSensor} - A sensor that returns instantaneous angles measured in degrees. The sensor can return absolute
 * angles (including those beyond positive or negative 360 degrees), or it can also be zeroed so that all subsequent values
 * are relative to the angle measured when the sensor is zeroed. An AngleSensor can represent potentiometers, encoders, and
 * other similar devices.</li>
 * <li>{@link org.strongback.components.CurrentSensor} - A sensor that returns the instantaneous and absolute current measured in Amps.</li>
 * <li>{@link org.strongback.components.VoltageSensor} - A sensor that returns the instantaneous and absolute voltage measured in Volts.</li>
 * <li>{@link org.strongback.components.TemperatureSensor} - A sensor that returns the instantaneous and absolute temperature measured in degrees Celcius.</li>
 * <li>{@link org.strongback.components.DistanceSensor} - A sensor that returns the instantaneous distance measured in inches or feet from a nearby
 * object. It can represent ultrasonic sensors, infrared proximity sensors, or even a vision system that estimates distance.
 * For simplicity, it can be zeroed to return relative positions.</li>
 * <li>{@link org.strongback.components.Switch} - A switch represents a device that has an active state when it is triggered and an inactive state when
 * it isn't. It can represent buttons, limit switches, Reed switches, Hall effect sensors, etc.</li>
 * <li>{@link org.strongback.components.Fuse} - A device that acts like a {@link org.strongback.components.Switch} but that can be explicitly triggered and optionally reset.</li>
 * <li>{@link org.strongback.components.SpeedSensor} - A sensor that returns the instantaneous speed. SpeedSensor may be useful on its own,
 * but it is intended primarily as a base interface for {@link org.strongback.components.Motor}</li>
 * <li>{@link org.strongback.components.SpeedController} - A device whose speed is able to be controlled. SpeedController may be useful on its own,
 * but it is intended primarily as a base interface for {@link org.strongback.components.Motor}.</li>
 * <li>{@link org.strongback.components.Accelerometer} - A sensor that provides the acceleration along a single axis.</li>
 * <li>{@link org.strongback.components.Relay} - A device that can be turned on and off.</li>
 * <li>{@link org.strongback.components.Solenoid} - A device that can be is a device that can be extended and retracted. Although similar to a
 * {@link org.strongback.components.Relay}, pneumatic solenoids are common enough in FRC to distinguish as a separate low-level component.</li>
 * </ul>
 * <h2>Higher-level components</h2>
 * <p>The following are the higher-level abstractions of slightly more complex physical components, and which are usually
 * created using combinations of multiple lower-level components:
 * <ul>
 * <li>{@link org.strongback.components.Motor} - A device that is both a {@link org.strongback.components.SpeedSensor} and {@link org.strongback.components.SpeedController}. Quite obviously, it can
 * represent a combination of an electric motor (e.g., CIM) and electronic speed controller (e.g., Talon).</li>
 * <li>{@link org.strongback.components.LimitedMotor} - A {@link org.strongback.components.Motor} that is constrained to move between a maximum position and minimum position.</li>
 * <li>{@link org.strongback.components.TalonSRX} - A motor controlled by a Talon SRX with a current sensor and position (angle) sensor, and that is
 * optionally constrained to move between a maximum position and minimum position.</li>
 * <li>{@link org.strongback.components.Compass} - An {@link org.strongback.components.AngleSensor angle sensor} that provides heading information in addition to angle.</li>
 * <li>{@link org.strongback.components.Gyroscope} - A gyroscope is a device that measures angular velocity (in degrees per second) about a single axis.
 * A gyroscope can indirectly determine angular displacement by integrating velocity with respect to time, which is why it extends
 * {@link org.strongback.components.Compass} (which extends {@link org.strongback.components.AngleSensor}).
 * <li>{@link org.strongback.components.TwoAxisAccelerometer} - A combination of two single-axis {@link org.strongback.components.Accelerometer}s. It can be used to represent
 * a physical 2- or 3-axis accelerometer, such as the ADXL193 (which may no longer be supported by WPILib).</li>
 * <li>{@link org.strongback.components.ThreeAxisAccelerometer} - A combination of two single-axis {@link org.strongback.components.Accelerometer}s. It can be used to represent
 * a physical 3-axis accelerometer, such as the ADXL345 or built-in accelerometer on the RoboRIO.</li>
 * <li>{@link org.strongback.components.PowerPanel} - An abstraction of the sensors on the Power Distribution Panel.</li>
 * <li>{@link org.strongback.components.SolenoidWithPosition} - A {@link org.strongback.components.Solenoid} that can determine its position. Typically this would be used to
 * represent a pneumatic solenoid with reed switches.</li>
 * </ul>
 * <h2>Utility interfaces</h2>
 * <p>The following are several very abstract utility interfaces used by the low- and high-level components mentioned above:
 * <ul>
 * <li>{@link org.strongback.components.Stoppable} - Something that can be stopped.</li>
 * <li>{@link org.strongback.components.Zeroable} - Something that can be zeroed.</li>
 * </ul>
 * <h2>Functional interfaces</h2>
 * <p>
 * Many of the component interfaces have been designed as {@link java.lang.FunctionalInterface @FunctionalInterface}s, which
 * is an interface with exactly one abstract method. Instances of functional interfaces can be created with lambda expressions,
 * method references, or constructor references, making them very easy to instantiate and use.
 * <h2>Hardware implementations</h2>
 * <p>
 * Strongback provides the {@link org.strongback.hardware.Hardware} class that is able to instantiate component implementations
 * that use the WPILib classes and which represent physical hardware on robots.
 */
package org.strongback.components;

