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

    /**
     * Register an {@link Executable} task to be called repeatedly.
     *
     * @param r the executable task
     * @return true if the executable task was registered, or false if it was null or was already registered with this executor
     */
    public boolean register(Executable r);

    /**
     * Unregister an {@link Executable} task to no longer be called.
     *
     * @param r the executable task
     * @return true if the executable task was unregistered, or false if it was null or not registered with this executor
     */
    public boolean unregister(Executable r);

    /**
     * Unregister all {@link Executable} tasks.
     */
    public void unregisterAll();
}