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
import org.strongback.components.Accelerometer;

/**
 * An {@link Accelerometer} implementation useful for testing, where the acceleration can be set in the test case so that it is
 * read by the component that uses an {@link Accelerometer}.
 *
 * @author Randall Hauch
 */
@ThreadSafe
public class MockAccelerometer implements Accelerometer {

    private volatile double accel;

    @Override
    public double getAcceleration() {
        return accel;
    }

    /**
     * Set the acceleration value {@link #getAcceleration() returned} by this object.
     *
     * @param accel the acceleration value
     * @return this instance to enable chaining methods; never null
     */
    public MockAccelerometer setAcceleration(double accel) {
        this.accel = accel;
        return this;
    }

    @Override
    public String toString() {
        return Double.toString(getAcceleration()) + " g/s\u00B2";
    }
}