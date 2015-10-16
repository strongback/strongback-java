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

package org.strongback.component;

import org.junit.Before;
import org.junit.Test;
import org.strongback.components.DistanceSensor;

/**
 * @author Randall Hauch
 *
 */
public class DistanceSensorTest extends AbstractDoubleValueTest {

    protected static void assertDistance(double distance) {
        assertValue(distance, sensor::getDistanceInInches, distance);
    }

    protected static void assertDistance(double distance, double result) {
        assertValue(distance, sensor::getDistanceInInches, result);
    }

    protected static void assertDistanceInFeet(double distance) {
        assertValue(distance, sensor::getDistanceInFeet, distance/12.0);
    }

    private static DistanceSensor sensor = DistanceSensor.create(DistanceSensorTest::getValue);

    @Override
    @Before
    public void beforeEach() {
        setValue(0);
        sensor.zero();
    }

    @Test
    public void shouldReturnPositiveDistance() {
        assertDistance(0.0);
        assertDistance(10.0);
        assertDistance(100.0);
        assertDistance(0.001);
    }

    @Test
    public void shouldReturnPositiveDistanceInFeet() {
        assertDistanceInFeet(0.0);
        assertDistanceInFeet(12.0);
        assertDistanceInFeet(144.0);
        assertDistanceInFeet(1);
    }

    @Test
    public void shouldReturnNegativeDistance() {
        assertDistance(-0.0);
        assertDistance(-10.0);
        assertDistance(-100.0);
        assertDistance(-0.001);
    }

    @Test
    public void shouldRemoveZeroFromValue() {
        setValue(45);
        sensor.zero();
        assertDistance(45, 0);
        assertDistance(90, 45);
        assertDistance(0, -45);
    }
}
