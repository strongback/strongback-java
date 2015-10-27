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

/**
 * A basic controller.
 *
 * @author Randall Hauch
 */
public interface Controller {

    /**
     * Calculate the next controller output. This usually involves reading one or more inputs and computing the output based
     * upon a model of the system.
     *
     * @return <code>true</code> if the execution resulted in an error that is within the tolerance of the setpoint, or
     *         <code>false</code> if this controller has not completed
     */
    public boolean computeOutput();

    /**
     * Sets the target value for this controller.
     *
     * @param setpoint the desired setpoint that this controller will use as a target
     * @return this object so that methods can be chained; never null
     */
    public Controller setpoint(double setpoint);

    /**
     * Sets the absolute tolerance for this controller.
     *
     * @param tolerance the maximum absolute error which is tolerable in the units of the input object
     * @return this object so that methods can be chained; never null
     */
    public Controller tolerance(double tolerance);

    /**
     * Reset any error values stored from previous {@link #computeOutput() executions}.
     *
     * @return this object so that methods can be chained; never null
     */
    public Controller reset();

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

}
