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

package org.strongback.control;

import org.strongback.Executable;
import org.strongback.Executor;
import org.strongback.Strongback;
import org.strongback.command.Requirable;

/**
 * A simple controller that can manage a component (typically a motor) based upon a desired {@link #withTarget(double) target value}
 * and {@link #withTolerance(double) tolerance}.
 *
 * @author Randall Hauch
 * @see PIDController
 */
public interface Controller extends Requirable {

    /**
     * Determine if this controller is {@link #enable() enabled}.
     *
     * @return <code>true</code> if enabled, or <code>false</code> otherwise
     */
    public boolean isEnabled();

    /**
    * Enable this controller so that it does read inputs, compute errors, and generate outputs when {@link #computeOutput()} is
    * called.
    *
    * @return this object so that methods can be chained; never null
    */
   public Controller enable();

   /**
    * Disable this controller to <em>not</em> read inputs, compute errors, and generate outputs when {@link #computeOutput()}
    * is called.
    *
    * @return this object so that methods can be chained; never null
    */
   public Controller disable();

    /**
     * Get the current measured value for this controller.
     * @return the current value
     */
    public double getValue();

    /**
     * Get the target value for this controller.
     * @return the target value
     * @see #withTarget(double)
     */
    public double getTarget();

    /**
     * Sets the target value for this controller.
     *
     * @param target the desired setpoint that this controller will use as a target
     * @return this object so that methods can be chained; never null
     * @see #getTarget()
     */
    public Controller withTarget(double target);

    /**
     * Sets the absolute tolerance for this controller.
     *
     * @param tolerance the maximum absolute error which is tolerable in the units of the input object
     * @return this object so that methods can be chained; never null
     * @see #getTolerance()
     */
    public Controller withTolerance(double tolerance);

    /**
     * Get the absolute tolerance for this controller.
     * @return the target value
     * @see #withTolerance(double)
     */
    public double getTolerance();

    /**
     * Determines whether the supplied value is within the {@link #getTolerance() tolerance} of the {@link #getTarget() target}.
     *
     * @param value the proposed value
     * @return <code>true</code> if the proposed value is within the tolerance of the target, or <code>false</code> otherwise
     */
    public default boolean checkTolerance(double value) {
        return Math.abs(value) <= (getTarget() - getTolerance());
    }

    /**
     * Determines whether the input to the controller is currently within the {@link #getTolerance() tolerance} of the
     * {@link #getTarget() target}.
     *
     * @return <code>true</code> if the current value is within the tolerance of the target, or <code>false</code> otherwise
     */
    public default boolean isWithinTolerance() {
        return checkTolerance(getValue());
    }

    /**
     * Calculate the next controller output. This usually involves reading one or more inputs and computing the output based
     * upon a model of the system.
     *
     * @return <code>true</code> if the execution completed because it resulted in a value that is within the tolerance of the
     *         setpoint, or <code>false</code> if this controller is not {@link #isEnabled() enabled} or has not yet reached the
     *         setpoint given the tolerance
     */
    public boolean computeOutput();

    /**
     * Reset any error values stored from previous {@link #computeOutput() executions}.
     *
     * @return this object so that methods can be chained; never null
     */
    public Controller reset();

    /**
     * Return whether the {@link Executable} instance returned by {@link #executable()} does anything useful. Software-based
     * controllers may often return <code>true</code>, whereas many hardware-based controllers may return <code>false</code>.
     * @return <code>true</code> if the {@link #executable()} does useful work and enables the controller to operate
     * continually and automatically, or <code>false</code> if the {@link #executable()} does nothing.
     */
    public default boolean hasExecutable() {
        return true;
    }

    /**
     * Get the {@link Executable} instance that will continuously {@link #computeOutput() execute} this controller to read
     * inputs from the source and generate outputs to reach the {@link #getTarget() target}.
     * <p>
     * If this is used, then this same controller should <em>never</em> be used with commands. This is not checked, so robot
     * programs are responsible for ensuring this does not happen.
     *
     * @return the {@link Executable} object that can be registered with an {@link Executor} (typically Strongback's
     *         {@link Strongback#executor() central executor}); never null and always the same instance for this controller
     */
    public Executable executable();

}
