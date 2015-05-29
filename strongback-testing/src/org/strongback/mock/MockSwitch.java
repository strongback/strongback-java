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

import org.strongback.components.Switch;

/**
 * A {@link Switch} implementation useful for testing, where the triggered state can be explicitly set in the test
 * case so that component using the Switch can determine if it is triggered.
 *
 */
public class MockSwitch implements Switch {

    private boolean triggered = false;

    @Override
    public boolean isTriggered() {
        return triggered;
    }

    /**
     * Set whether this switch is to be triggered.
     * @param triggered true if the switch is to be triggered, or false otherwise
     * @return this object to allow chaining of methods; never null
     */
    public MockSwitch setTriggered( boolean triggered ) {
        this.triggered = triggered;
        return this;
    }

    /**
     * Set this switch as being triggered.
     * @return this object to allow chaining of methods; never null
     */
    public MockSwitch setTriggered() {
        setTriggered(true);
        return this;
    }

    /**
     * Set this switch as being not triggered.
     * @return this object to allow chaining of methods; never null
     */
    public MockSwitch setNotTriggered() {
        setTriggered(false);
        return this;
    }

    @Override
    public String toString() {
        return triggered ? "closed" : "open";
    }
}
