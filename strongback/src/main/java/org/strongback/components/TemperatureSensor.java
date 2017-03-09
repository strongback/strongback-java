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

import org.strongback.annotation.ThreadSafe;

/**
 * A sensor the reports the temperature.
 */
@ThreadSafe
@FunctionalInterface
public interface TemperatureSensor {
    /**
     * Gets the current temperature in degrees Celsius.
     *
     * @return the current temperature in Celsius
     */
    public double getTemperatureInCelsius();
    /**
     * Gets the current temperature in degrees Fahrenheit.
     *
     * @return the current temperature in Fahrenheit
     */
    default public double getTemperatureInFahrenheit() {
        return getTemperatureInCelsius() * 9.0/5.0 + 32.0;
    }
}