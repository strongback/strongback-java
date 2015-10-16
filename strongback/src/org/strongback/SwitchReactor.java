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

import java.util.function.Supplier;

import org.strongback.annotation.ThreadSafe;
import org.strongback.command.Command;
import org.strongback.components.Switch;

/**
 * A class that allows other components to react to the state of switches. For example, the reactor can be used to submit a
 * command whenever a Gamepad's left trigger button is pressed and submit a different command when the same trigger is released:
 *
 * <pre>
 *   Gamepad gamepad = ...
 *   reactor.onTriggered(gamepad.getRightTrigger(),()-&gt;Strongback.submit(new FireRepeatedlyCommand()));
 *   reactor.onUnTriggered(gamepad.getRightTrigger(),()-&gt;Strongback.submit(new StopFireCommand()));
 * </pre>
 * <p>
 * The reactor is threadsafe, meaning functions can be registered even while the reactor is forwarding state and state
 * transitions to the already-registered functions.
 * <p>
 * It is not currently possible to remove a function once it has been registered.
 */
@ThreadSafe
public interface SwitchReactor {

    /**
     * Submit a {@link Command} the moment when the specified {@link Switch} is triggered.
     *
     * @param swtch the {@link Switch}
     * @param commandSupplier the supplier of the command to submit; may not be null but may return a null command
     */
    default public void onTriggeredSubmit(Switch swtch, Supplier<Command> commandSupplier) {
        onTriggered(swtch,()->Strongback.submit(commandSupplier.get()));
    }

    /**
     * Submit a {@link Command} the moment when the specified {@link Switch} is untriggered.
     *
     * @param swtch the {@link Switch}
     * @param commandSupplier the supplier of the command to submit; may not be null but may return a null command
     */
    default public void onUntriggeredSubmit(Switch swtch, Supplier<Command> commandSupplier) {
        onTriggered(swtch,()->Strongback.submit(commandSupplier.get()));
    }

    /**
     * Register a {@link Runnable} function that is to be called the moment when the specified {@link Switch} is triggered.
     *
     * @param swtch the {@link Switch}
     * @param function the function to execute when the switch is triggered
     */
    public void onTriggered(Switch swtch, Runnable function);

    /**
     * Register a {@link Runnable} function that is to be called the moment when the specified {@link Switch} is untriggered.
     *
     * @param swtch the {@link Switch}
     * @param function the function to execute when the switch is untriggered
     */
    public void onUntriggered(Switch swtch, Runnable function);

    /**
     * Register a {@link Runnable} function to be called repeatedly whenever the specified {@link Switch} is in the triggered
     * state.
     *
     * @param swtch the {@link Switch}
     * @param function the function to execute while the switch remains triggered
     */
    public void whileTriggered(Switch swtch, Runnable function);

    /**
     * Register a {@link Runnable} function to be called repeatedly whenever the specified {@link Switch} is not in the
     * triggered state.
     *
     * @param swtch the {@link Switch}
     * @param function the function to execute while the switch remains untriggered
     */
    public void whileUntriggered(Switch swtch, Runnable function);
}