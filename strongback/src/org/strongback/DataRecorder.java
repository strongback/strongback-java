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

package org.strongback;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

import org.strongback.components.SpeedSensor;
import org.strongback.components.Switch;

/**
 * @author Randall Hauch
 *
 */
public interface DataRecorder {
    /**
     * Registers by name a function that will be periodically polled to obtain and record an integer value. This method will
     * remove any previously-registered supplier, switch, or motor with the same name.
     *
     * @param name the name of this data supplier
     * @param supplier the {@link IntSupplier} of the value to be logged
     * @return this instance so methods can be chained together; never null
     * @throws IllegalArgumentException if the {@code supplier} parameter is null
     */
    public DataRecorder register(String name, IntSupplier supplier);

    /**
     * Registers by name a function that will be periodically polled to obtain a double value and scale it to an integer value
     * that can be recorded. This method will remove any previously-registered supplier, switch, or motor with the same name.
     *
     * @param name the name of this data supplier
     * @param scale the scale factor to multiply the supplier's double values before casting to an integer value
     * @param supplier the {@link IntSupplier} of the value to be logged
     * @return this instance so methods can be chained together; never null
     * @throws IllegalArgumentException if the {@code supplier} parameter is null
     */
    default public DataRecorder register(String name, double scale, DoubleSupplier supplier) {
        return register(name, () -> (int) (supplier.getAsDouble() * scale));
    }

    /**
     * Registers by name a switch that will be periodically polled to obtain and record the switch state. This method will
     * remove any previously-registered supplier, switch, or motor with the same name.
     *
     * @param name the name of the {@link Switch}
     * @param swtch the {@link Switch} to be logged
     * @return this instance so methods can be chained together; never null
     * @throws IllegalArgumentException if the {@code swtch} parameter is null
     */
    public DataRecorder register(String name, Switch swtch);

    /**
     * Registers by name a speed sensor that will be periodically polled to obtain and record the current speed. This method
     * will remove any previously-registered supplier, switch, or motor with the same name.
     *
     * @param name the name of the {@link SpeedSensor}
     * @param sensor the {@link SpeedSensor} to be logged
     * @return this instance so methods can be chained together; never null
     * @throws IllegalArgumentException if the {@code sensor} parameter is null
     */
    public DataRecorder register(String name, SpeedSensor sensor);

    /**
     * Registers by name a recordable object that likely has multiple channels. This method will remove any
     * previously-registered supplier, switch, or motor with the same name.
     *
     * @param name the name of the {@link SpeedSensor}
     * @param recordable the {@link DataRecordable} to be registered
     * @return this instance so methods can be chained together; never null
     * @throws IllegalArgumentException if the {@code sensor} parameter is null
     */
    public DataRecorder register(String name, DataRecordable recordable);
}
