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

import static org.fest.assertions.Assertions.assertThat;

import org.fest.assertions.Delta;
import org.junit.Before;
import org.junit.Test;
import org.strongback.components.Compass;

/**
 * @author Randall Hauch
 *
 */
public class CompassTest extends AngleSensorTest {

    private static final double[][] POSITIVE_ANGLES = { { 0.0, 0.0 }, { 180.0, 180.0 }, { 359.0, 359.0 }, { 360.0, 0.0 },
            { 360.001, 0.001 }, { 361.0, 1.0 }, { 360.0 * 2, 0.0 }, { 360.0 * 3, 0.0 }, { 360.0 * 4, 0.0 } };

    private static final double[][] NEGATIVE_ANGLES = { { -1.0, 359 }, { -90.0, 270 }, { -180, 180 }, { -270, 90 },
            { -360, 0 }, { -361, 359 }, { -360 * 2, 0 } };

    private static final Delta NOMINAL_TOLERANCE = Delta.delta(0.0001);

    protected static void assertAngle(double newAngle) {
        assertValue(newAngle, sensor::getAngle, newAngle);
    }

    protected static void assertAngle(double newAngle, double result) {
        assertValue(newAngle, sensor::getAngle, result);
    }

    protected static void assertProperHeading(double angle, double expectedHeading) {
        setValue(angle);
        assertThat(sensor.getAngle()).isEqualTo(angle, NOMINAL_TOLERANCE);
        assertThat(sensor.getHeading()).isEqualTo(expectedHeading, NOMINAL_TOLERANCE);
    }

    private static Compass sensor = Compass.create(CompassTest::getValue);

    @Override
    @Before
    public void beforeEach() {
        super.beforeEach();
        setValue(0);
        sensor.zero();
    }

    @Override
    @Test
    public void shouldReturnPositiveAngle() {
        assertAngle(0.0);
        assertAngle(10.0);
        assertAngle(100.0);
        assertAngle(0.001);
    }

    @Override
    @Test
    public void shouldReturnNegativeAngle() {
        assertAngle(-0.0);
        assertAngle(-10.0);
        assertAngle(-100.0);
        assertAngle(-0.001);
    }

    @Override
    @Test
    public void shouldRemoveZeroFromValue() {
        setValue(45);
        sensor.zero();
        assertAngle(45, 0);
        assertAngle(90, 45);
        assertAngle(0, -45);
    }

    @Test
    public void shouldProperlyComputeHeadingForPositiveAngles() {
        for (double[] pair : POSITIVE_ANGLES) {
            assertProperHeading(pair[0], pair[1]);
        }
    }

    @Test
    public void shouldProperlyComputeHeadingForNegativeAngles() {
        for (double[] pair : NEGATIVE_ANGLES) {
            assertProperHeading(pair[0], pair[1]);
        }
    }

}
