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

package org.strongback.mock;

import org.strongback.annotation.ThreadSafe;
import org.strongback.components.TemperatureSensor;

/**
 * A {@link TemperatureSensor} implementation useful for testing, where the temperature can be explicitly set in the test case
 * so that the known value is read by the component that uses an {@link TemperatureSensor}.
 *
 * @author Randall Hauch
 */
@ThreadSafe
public class MockTemperatureSensor implements TemperatureSensor {

    private volatile double temp = 0;

    @Override
    public double getTemperatureInCelsius() {
        return temp;
    }

    /**
     * Set the temperature value {@link #getTemperatureInCelsius() returned} by this object.
     *
     * @param temp the temperature
     * @return this object to allow chaining of methods; never null
     */
    public MockTemperatureSensor setTemperature(double temp) {
        this.temp = temp;
        return this;
    }

    @Override
    public String toString() {
        return Double.toString(getTemperatureInFahrenheit()) + "\u00B0F";
    }
}
