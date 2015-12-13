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

import org.strongback.Executable;
import org.strongback.control.Controller;

/**
 * A {@link Controller} implementation that tests can use to manually {@link #setValue(double) set the value}.
 */
public class MockController implements Controller {

    private boolean enabled = true;
    private double setpoint = 0.0d;
    private double tolerance = 0.0d;
    private double value = 0.0d;
    private final Executable executable = (time)->computeOutput();

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Controller enable() {
        enabled = true;
        return this;
    }

    @Override
    public Controller disable() {
        enabled = false;
        return this;
    }

    @Override
    public double getTarget() {
        return setpoint;
    }

    @Override
    public MockController withTarget(double target) {
        this.setpoint = target;
        return this;
    }

    @Override
    public double getTolerance() {
        return tolerance;
    }

    @Override
    public MockController withTolerance(double tolerance) {
        this.tolerance = tolerance;
        return this;
    }

    @Override
    public boolean computeOutput() {
        return isWithinTolerance();
    }

    @Override
    public MockController reset() {
        return this;
    }

    @Override
    public boolean hasExecutable() {
        return true;
    }

    @Override
    public Executable executable() {
        return executable;
    }

    @Override
    public double getValue() {
        return value;
    }

    public MockController setValue( double value ) {
        this.value = value;
        return this;
    }

}
