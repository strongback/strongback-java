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
import org.strongback.components.DistanceSensor;

/**
 * A {@link DistanceSensor} implementation useful for testing, where the distance can be explicitly set in the test
 * case so that the known value is read by the component that uses an {@link DistanceSensor}.
 *
 * @author Randall Hauch
 */
@ThreadSafe
public class MockDistanceSensor extends MockZeroable implements DistanceSensor {

    @Override
    public MockDistanceSensor zero() {
        super.zero();
        return this;
    }

    @Override
    public double getDistanceInInches() {
        return super.getValue();
    }

    /**
     * Set the distance in inches {@link #getDistanceInInches() returned} by this object.
     *
     * @param distance the new distance in inches
     * @return this instance to enable chaining methods; never null
     * @see #setDistanceInFeet(double)
     */
    public MockDistanceSensor setDistanceInInches(double distance) {
        super.setValue(distance);
        return this;
    }

    /**
     * Set the distance in feet {@link #getDistanceInInches() returned} by this object.
     *
     * @param distance the new distance in feet
     * @return this instance to enable chaining methods; never null
     * @see #setDistanceInInches(double)
     */
    public MockDistanceSensor setDistanceInFeet(double distance) {
        return setDistanceInInches(distance * 12.0);
    }

    @Override
    public String toString() {
        return Double.toString(getDistanceInInches()) + " inches";
    }
}
