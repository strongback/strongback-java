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

import org.strongback.annotation.Immutable;
import org.strongback.components.ThreeAxisAccelerometer;
import org.strongback.components.TwoAxisAccelerometer;

/**
 * A {@link TwoAxisAccelerometer} implementation useful for testing, where the two accelerometers are mocks themselves and
 * can be explicitly set in the test case
 * so that the known acceleration values are read by the component that uses an {@link TwoAxisAccelerometer}.
 *
 * @author Randall Hauch
 */
@Immutable
public class MockThreeAxisAccelerometer extends MockTwoAxisAccelerometer implements ThreeAxisAccelerometer {

    private final MockAccelerometer z = new MockAccelerometer();

    @Override
    public MockAccelerometer getZDirection() {
        return z;
    }

    @Override
    public String toString() {
        return "" + getXDirection().getAcceleration() + ", " + getYDirection().getAcceleration() + ", " + getZDirection().getAcceleration() + " g/s\u00B2";
    }
}
