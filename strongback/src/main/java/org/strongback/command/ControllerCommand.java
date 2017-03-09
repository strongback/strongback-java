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

import org.strongback.Strongback;
import org.strongback.control.Controller;
import org.strongback.control.SoftwarePIDController;

/**
 * A command that uses the supplied {@link Controller} to move toward the controller's {@link Controller#withTarget(double)
 * target}. The controller is assumed to operate automatically and continuously, independently of this command. For example, it
 * might be a hardware-based controller (e.g., PID control on the Talon SRX), or a software-based controller (e.g.,
 * {@link SoftwarePIDController}) that is registered with Strongback's {@link Strongback#executor() executor} to run in the
 * background. (See {@link UnmanagedControllerCommand} for a command that will explicitly run the controller only when commands
 * use it.)
 * <p>
 * Although this command can be used directly via {@link Command#reuse(Controller, Runnable)}, it is also designed to be
 * subclassed to create custom, reusable, and concrete commands tailored for your robot. Typically this involves only defining
 * custom public constructors to make the commands easy to use, and inheriting all other functionality.
 *
 * @see Command#reuse(Controller, Runnable)
 * @see Command#reuse(double, Controller, Runnable)
 * @see UnmanagedControllerCommand
 * @author Randall Hauch
 */
public class ControllerCommand extends Command {

    protected final Controller controller;
    private final Runnable initializer;

    /**
     * Create a command that uses the given shared and managed controller to move within the controller's tolerance of the
     * target. The initializer can be used to update the controller's target, tolerance, and any other controller settings when
     * the command is initialized.
     *
     * @param sharedController the PID+FF controller; may not be null
     * @param initializer the controller initialization function, which should update the controller's target, tolerance, and
     *        any other setting; may be null
     * @param requirements the {@link Requirable}s this {@link Command} requires
     */
    protected ControllerCommand(Controller sharedController, Runnable initializer, Requirable... requirements) {
        this(0.0, sharedController, initializer, requirements);
    }

    /**
     * Create a command that uses the given shared and managed controller to move within the controller's tolerance of the
     * target, timing out if the command takes longer than {@code durationInSeconds}. The initializer can be used to update the
     * controller's target, tolerance, and any other controller settings when the command is initialized.
     *
     * @param timeoutInSeconds how long in seconds this command executes before terminating, zero is forever
     * @param sharedController the shared controller; may not be null
     * @param initializer the controller initialization function, which should update the controller's target, tolerance, and
     *        any other setting; may be null
     * @param requirements the {@link Requirable}s this {@link Command} requires
     */
    protected ControllerCommand(double timeoutInSeconds, Controller sharedController, Runnable initializer,
            Requirable... requirements) {
        super(timeoutInSeconds, requirements);
        this.controller = sharedController;
        this.initializer = initializer; // may be null
    }

    @Override
    public final void initialize() {
        super.initialize();
        if (initializer != null) initializer.run();
        controller.enable();
        preExecute();
    }

    /**
     * Called one time at the end of {@link #initialize()}, after the initializer has run and after the controller has been
     * enabled. This method does nothing by default, but can be overridden in subclasses for specific behavior.
     */
    protected void preExecute() {
        // does nothing by default
    }

    @Override
    public boolean execute() {
        // All we have to do is determine whether the controller is within tolerance, in which case we're done ...
        return controller.isWithinTolerance();
    }

    @Override
    public void end() {
        super.end();
    }
}
