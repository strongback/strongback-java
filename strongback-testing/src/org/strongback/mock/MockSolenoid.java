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
import org.strongback.components.Solenoid;

/**
 * A {@link Solenoid} implementation useful for testing, where the current (in amps) can be explicitly set in the test
 * case so that the known value is read by the component that uses an {@link CurrentSensor}.
 *
 * @author Randall Hauch
 */
@ThreadSafe
public class MockSolenoid implements Solenoid {

    private volatile Direction direction = Direction.STOPPED;
    private final boolean completeImmediately;

    protected MockSolenoid( boolean completeImmediately ) {
        this.completeImmediately = completeImmediately;
    }

    @Override
    public MockSolenoid extend() {
        direction = Direction.EXTENDING;
        if ( completeImmediately ) direction = Direction.STOPPED;
        return this;
    }

    @Override
    public MockSolenoid retract() {
        direction = Direction.EXTENDING;
        if ( completeImmediately ) direction = Direction.STOPPED;
        return this;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    /**
     * Stop any movement of this solenoid.
     * @return this object so that methods can be chained together; never null
     */
    public MockSolenoid stop() {
        direction = Direction.STOPPED;
        return this;
    }

    @Override
    public String toString() {
        return getDirection().name();
    }
}
