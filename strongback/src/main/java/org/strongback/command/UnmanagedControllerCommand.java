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

import org.strongback.control.Controller;

/**
 * A command that will use an unmanaged {@link Controller} to move toward the controller's {@link Controller#withTarget(double)
 * target}. Unlike {@link ControllerCommand} that assumes the controller is automatically managed elsewhere, this command will
 * manually invoke {@link Controller#computeOutput()} each time {@link #execute()} is called.
 * <p>
 * Although this command can be used directly via {@link Command#use(Controller, Runnable)}, it is also designed to be
 * subclassed to create custom, reusable, and concrete commands tailored for your robot. Typically this involves only defining
 * custom public constructors to make the commands easy to use, and inheriting all other functionality.
 *
 * @see Command#use(Controller, Runnable)
 * @see Command#use(double, Controller, Runnable)
 * @see ControllerCommand
 * @author Randall Hauch
 */
public class UnmanagedControllerCommand extends ControllerCommand {

    /**
     * Create a command that uses the supplied dedicated controller to moves within the tolerance of the target. The initializer
     * can be used to update the controller's target, tolerance, and any other controller settings when the command is
     * initialized.
     *
     * @param controller the PID+FF controller; may not be null
     * @param initializer the controller initialization function, which should update the controller's target, tolerance, and
     *        any other setting; may be null
     * @param requirements the {@link Requirable}s this {@link Command} requires
     */
    protected UnmanagedControllerCommand(Controller controller, Runnable initializer, Requirable... requirements) {
        super(controller, initializer, requirements);
    }

    /**
     * Create a command that uses the supplied dedicated controller to moves within the tolerance of the target, timing out if
     * the command takes longer than {@code durationInSeconds}. The initializer can be used to update the controller's target,
     * tolerance, and any other controller settings when the command is initialized.
     *
     * @param timeoutInSeconds how long in seconds this command executes before terminating, zero is forever
     * @param controller the PID+FF controller; may not be null
     * @param initializer the controller initialization function, which should update the controller's target, tolerance, and
     *        any other setting; may be null
     * @param requirements the {@link Requirable}s this {@link Command} requires
     */
    protected UnmanagedControllerCommand(double timeoutInSeconds, Controller controller, Runnable initializer,
            Requirable... requirements) {
        super(timeoutInSeconds, controller, initializer, requirements);
    }

    @Override
    public boolean execute() {
        return controller.computeOutput();
    }
}
