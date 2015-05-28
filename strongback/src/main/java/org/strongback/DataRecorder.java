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

import java.util.function.IntSupplier;

import org.strongback.components.Motor;
import org.strongback.components.Switch;

/**
 * @author Randall Hauch
 *
 */
public interface DataRecorder {
    /**
     * Registers the value of the specified {@link IntSupplier} to be logged. This method will remove any previously-registered
     * supplier, switch, or motor with the same name.
     *
     * @param name the name of this data supplier
     * @param supplier the {@link IntSupplier} of the value to be logged
     * @return this instance so methods can be chained together; never null
     * @throws IllegalArgumentException if the {@code supplier} parameter is null
     * @throws IllegalStateException if the data logger is already {@link #start() started}
     */
    public DataRecorder register(String name, IntSupplier supplier);

    /**
     * Registers a {@link Switch} to be logged. This method will remove any previously-registered supplier, switch, or motor
     * with the same name.
     *
     * @param name the name of the {@link Switch}
     * @param swtch the {@link Switch} to be logged
     * @return this instance so methods can be chained together; never null
     * @throws IllegalArgumentException if the {@code swtch} parameter is null
     * @throws IllegalStateException if the data logger is already {@link #start() started}
     */
    public DataRecorder register(String name, Switch swtch);

    /**
     * Registers a {@link Motor} to be logged. This method will remove any previously-registered supplier, switch, or motor with
     * the same name.
     *
     * @param name the name of the {@link Motor}
     * @param motor the {@link Motor} to be logged
     * @return this instance so methods can be chained together; never null
     * @throws IllegalArgumentException if the {@code motor} parameter is null
     * @throws IllegalStateException if the data logger is already {@link #start() started}
     */
    public DataRecorder register(String name, Motor motor);
}
