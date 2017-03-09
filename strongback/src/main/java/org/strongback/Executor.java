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

import org.strongback.annotation.ThreadSafe;

/**
 * An executor that invokes registered {@link Executable}s on a fixed period.
 */
@ThreadSafe
public interface Executor {

    public static enum Priority {
        HIGH, MEDIUM, LOW;
    }

    /**
     * Register a high priority {@link Executable} task so that it is called repeatedly on Strongback's executor thread.
     *
     * @param task the executable task
     * @return {@code true} if the executable task was registered for the first time as a high priority, or {@code false} if
     *         {@code task} was null or was already registered with this executor at high priority
     * @deprecated Use {@link #register(Executable, Priority)} instead
     */
    @Deprecated
    default boolean register(Executable task) {
        return register(task, Priority.HIGH);
    }

    /**
     * Register an {@link Executable} task with the given priority so that it is called repeatedly on Strongback's executor
     * thread. If the given task is already registered with a different priority, this method reassigns it to the desired
     * priority; if the given task is already registered with the desired priority, this method does nothing.
     * <p>
     * This executor runs high priority tasks every cycle, medium priority tasks slightly every other cycles, and low priority
     * tasks every 4 cycles. All {@link Executable} tasks are called on the first cycle.
     *
     * @param task the executable task
     * @param priority the priority of the executable; may not be null
     * @return {@code true} if the executable task was registered for the first time at the given priority, or {@code false} if
     *         {@code task} was null or was already registered with this executor at the given priority
     */
    public boolean register(Executable task, Priority priority);

    /**
     * Unregister an {@link Executable} task to no longer be called.
     *
     * @param task the executable task
     * @return true if the executable task was unregistered, or false if it was null or not registered with this executor
     */
    public boolean unregister(Executable task);

    /**
     * Unregister all {@link Executable} tasks.
     */
    public void unregisterAll();
}