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

package org.strongback.components;

import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;

import org.strongback.hardware.Hardware;

/**
 * A simple abstraction for the Power Distribution Panel (PDP).
 */
public interface PowerPanel {

    /**
     * Gets the current sensor for the specified channel on this {@link PowerPanel}.
     *
     * @param channel the channel to read
     * @return the current sensor for the given channel; never null
     */
    public CurrentSensor getCurrentSensor(int channel);

    /**
     * Gets the current output from the specified channel in amps.
     *
     * @param channel the channel to read
     * @return the current of the specified channel
     */
    default public double getCurrent(int channel) {
        return getCurrentSensor(channel).getCurrent();
    }

    /**
     * Gets the sensor that reports total current (in amps) for this {@link PowerPanel}.
     *
     * @return the total current sensor; never null
     */
    public CurrentSensor getTotalCurrentSensor();

    /**
     * Gets the total current (in amps) for this {@link PowerPanel}. This is equivalent to calling
     * {@code getTotalCurrentSensor().getCurrent()}.
     *
     * @return the total current; never null
     */
    default public double getTotalCurrent() {
        return getTotalCurrentSensor().getCurrent();
    }

    /**
     * Gets the sensor that reports the input voltage (in volts) of this {@link PowerPanel}.
     *
     * @return the input voltage sensor; never null
     */
    public VoltageSensor getVoltageSensor();

    /**
     * Gets the input voltage (in volts) of this {@link PowerPanel}. This is equivalent to calling
     * {@code getVoltageSensor().getVoltage()}.
     *
     * @return the input voltage; never null
     */
    default public double getVoltage() {
        return getVoltageSensor().getVoltage();
    }

    /**
     * Gets the sensor that reports the temperature (in degrees Celsius) of this {@link PowerPanel}.
     *
     * @return the temperature sensor; never null
     */
    public TemperatureSensor getTemperatureSensor();

    /**
     * Gets the temperature of this {@link PowerPanel} in degrees Celsius. This is equivalent to calling
     * {@code getTemperatureSensor().getTemperature()}.
     *
     * @return the temperature of this {@link PowerPanel}
     */
    default public double getTemperature() {
        return getTemperatureSensor().getTemperatureInCelsius();
    }

    /**
     * Create a new PowerPanel from functions that supply the current for each channel, total current, voltage, and temperature.
     *
     * @param currentForChannel the function that returns the current for a given channel; may not be null
     * @param totalCurrent the function that returns total current; may not be null
     * @param voltage the function that returns voltage; may not be null
     * @param temperature the function that returns temperature; may not be null
     * @return the power panel; never null
     * @see Hardware#powerPanel()
     */
    public static PowerPanel create(IntToDoubleFunction currentForChannel, DoubleSupplier totalCurrent, DoubleSupplier voltage,
            DoubleSupplier temperature) {
        return new PowerPanel() {

            @Override
            public CurrentSensor getCurrentSensor(int channel) {
                return () -> currentForChannel.applyAsDouble(channel);
            }

            @Override
            public CurrentSensor getTotalCurrentSensor() {
                return totalCurrent::getAsDouble;
            }

            @Override
            public VoltageSensor getVoltageSensor() {
                return voltage::getAsDouble;
            }

            @Override
            public TemperatureSensor getTemperatureSensor() {
                return voltage::getAsDouble;
            }
        };
    }

    /**
     * Create a new PowerPanel from functions that supply the current for each channel, total current, voltage, and temperature.
     *
     * @param currentSensorForChannel the function that returns the current sensor for a given channel; may not be null
     * @param totalCurrent the total current sensor; may not be null
     * @param voltage the voltage sensor; may not be null
     * @param temperature the temperature sensor; may not be null
     * @return the power panel; never null
     * @see Hardware#powerPanel()
     */
    public static PowerPanel create(Function<Integer, CurrentSensor> currentSensorForChannel, CurrentSensor totalCurrent,
            VoltageSensor voltage, TemperatureSensor temperature) {
        return new PowerPanel() {
            @Override
            public CurrentSensor getCurrentSensor(int channel) {
                return currentSensorForChannel.apply(channel);
            }

            @Override
            public CurrentSensor getTotalCurrentSensor() {
                return totalCurrent;
            }

            @Override
            public VoltageSensor getVoltageSensor() {
                return voltage;
            }

            @Override
            public TemperatureSensor getTemperatureSensor() {
                return temperature;
            }
        };
    }
}