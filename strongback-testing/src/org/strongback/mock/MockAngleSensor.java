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
import org.strongback.components.AngleSensor;

/**
 * An {@link AngleSensor} implementation useful for testing, where the angle can be explicitly set in the test case so that the
 * known value is read by the component that uses an {@link AngleSensor}.
 *
 * @author Randall Hauch
 */
@ThreadSafe
public class MockAngleSensor extends MockZeroable implements AngleSensor {

    @Override
    public MockAngleSensor zero() {
        super.zero();
        return this;
    }

    @Override
    public double getAngle() {
        return super.getValue();
    }

    /**
     * Set the angle value {@link #getAngle() returned} by this object.
     *
     * @param angle the angle value
     * @return this instance to enable chaining methods; never null
     */
    public MockAngleSensor setAngle(double angle) {
        super.setValue(angle);
        return this;
    }

    @Override
    public String toString() {
        return Double.toString(getAngle()) + "\u00B0";
    }
}
