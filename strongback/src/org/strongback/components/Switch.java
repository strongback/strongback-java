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

package org.strongback.components;

import org.strongback.annotation.ThreadSafe;


/**
 * A switch is any readable device that has an active state when it is triggered and an inactive state when it isn't.
 *
 * @author Zach Anderson
 */
@ThreadSafe
@FunctionalInterface
public interface Switch {
    /**
     * Checks if this switch is triggered.
     *
     * @return {@code true} if this switch was triggered, or {@code false} otherwise
     */
    public boolean isTriggered();

    /**
     * Create a switch that is always triggered.
     * @return the always-triggered switch; never null
     */
    public static Switch alwaysTriggered() {
        return ()->true;
    }

    /**
     * Create a switch that is never triggered.
     * @return the never-triggered switch; never null
     */
    public static Switch neverTriggered() {
        return ()->false;
    }
}