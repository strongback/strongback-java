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

import org.strongback.annotation.ThreadSafe;
import org.strongback.components.CurrentSensor;

/**
 * A {@link CurrentSensor} implementation useful for testing, where the current (in amps) can be explicitly set in the test
 * case so that the known value is read by the component that uses an {@link CurrentSensor}.
 *
 * @author Randall Hauch
 */
@ThreadSafe
public class MockCurrentSensor implements CurrentSensor {

    private volatile double currentInAmps = 0;

    @Override
    public double getCurrent() {
        return currentInAmps;
    }

    /**
     * Set the current (in amps) {@link #getCurrent() returned} by this object.
     *
     * @param currentInAmps the new current reading
     * @return this instance to enable chaining methods; never null
     */
    public MockCurrentSensor setCurrent(double currentInAmps) {
        this.currentInAmps = currentInAmps;
        return this;
    }

    @Override
    public String toString() {
        return Double.toString(getCurrent()) + "A";
    }

}
