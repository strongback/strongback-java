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

package org.strongback.command;

import org.strongback.control.PIDController;

/**
 * A command that uses the supplied PID controller and moves to the specified setpoint. You can subclass this command class or
 * use it as is.
 *
 * @author Randall Hauch
 * @see Command#approach(PIDController, double, double)
 * @see Command#approach(double, PIDController, double, double)
 */
public class PIDCommand extends Command {

    private final PIDController controller;
    private final Runnable initializer;

    /**
     * Create a command that uses the supplied PID controller and moves to the specified setpoint.
     *
     * @param controller the PID+FF controller; may not be null
     * @param setpoint the desired value for the input to the controller
     * @param requirements the {@link Requirable}s this {@link Command} requires
     */
    public PIDCommand(PIDController controller, double setpoint, Requirable... requirements) {
        super(0.0, requirements);
        this.controller = controller;
        this.initializer = () -> controller.setpoint(setpoint).enable();
    }

    /**
     * Create a command that uses the supplied PID controller and moves within the specified tolerance of the specified
     * setpoint.
     *
     * @param controller the PID+FF controller; may not be null
     * @param setpoint the desired value for the input to the controller
     * @param tolerance the absolute tolerance for how close the controller should come before completing the command
     * @param requirements the {@link Requirable}s this {@link Command} requires
     */
    public PIDCommand(PIDController controller, double setpoint, double tolerance, Requirable... requirements) {
        super(0.0, requirements);
        this.controller = controller;
        this.initializer = () -> controller.setpoint(setpoint).tolerance(tolerance).enable();
    }

    /**
     * Create a command that uses the supplied PID controller and moves to the specified setpoint.
     *
     * @param timeoutInSeconds how long in seconds this command executes before terminating, zero is forever
     * @param controller the PID+FF controller; may not be null
     * @param setpoint the desired value for the input to the controller
     * @param requirements the {@link Requirable}s this {@link Command} requires
     */
    public PIDCommand(double timeoutInSeconds, PIDController controller, double setpoint, Requirable... requirements) {
        super(timeoutInSeconds, requirements);
        this.controller = controller;
        this.initializer = () -> controller.setpoint(setpoint).enable();
    }

    /**
     * Create a command that uses the supplied PID controller and moves within the specified tolerance of the specified
     * setpoint.
     *
     * @param timeoutInSeconds how long in seconds this command executes before terminating, zero is forever
     * @param controller the PID+FF controller; may not be null
     * @param setpoint the desired value for the input to the controller
     * @param tolerance the absolute tolerance for how close the controller should come before completing the command
     * @param requirements the {@link Requirable}s this {@link Command} requires
     */
    public PIDCommand(double timeoutInSeconds, PIDController controller, double setpoint, double tolerance,
            Requirable... requirements) {
        super(timeoutInSeconds, requirements);
        this.controller = controller;
        this.initializer = () -> controller.setpoint(setpoint).tolerance(tolerance).enable();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (initializer != null) initializer.run();
    }

    @Override
    public boolean execute() {
        return controller.computeOutput();
    }

    @Override
    public void end() {
        super.end();
        controller.disable();
    }

}
