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

import org.junit.Before;
import org.junit.Test;
import org.strongback.components.AngleSensor;

/**
 * @author Randall Hauch
 *
 */
public class AngleSensorTest extends AbstractDoubleValueTest {

    protected static void assertAngle(double newAngle) {
        assertValue(newAngle, sensor::getAngle, newAngle);
    }

    protected static void assertAngle(double newAngle, double result) {
        assertValue(newAngle, sensor::getAngle, result);
    }

    private static AngleSensor sensor = AngleSensor.create(AngleSensorTest::getValue);

    @Override
    @Before
    public void beforeEach() {
        setValue(0);
        sensor.zero();
    }

    @Test
    public void shouldReturnPositiveAngle() {
        assertAngle(0.0);
        assertAngle(10.0);
        assertAngle(100.0);
        assertAngle(0.001);
    }

    @Test
    public void shouldReturnNegativeAngle() {
        assertAngle(-0.0);
        assertAngle(-10.0);
        assertAngle(-100.0);
        assertAngle(-0.001);
    }

    @Test
    public void shouldRemoveZeroFromValue() {
        setValue(45);
        sensor.zero();
        assertAngle(45, 0);
        assertAngle(90, 45);
        assertAngle(0, -45);
    }

    @Test
    public void shouldInvertAngle() {
        AngleSensor inverted = AngleSensor.invert(sensor);
        assertAngle(0.0);
        assertThat(inverted.getAngle()).isEqualTo(0);
        assertAngle(100.0);
        assertThat(inverted.getAngle()).isEqualTo(-100);
        assertAngle(-100.0);
        assertThat(inverted.getAngle()).isEqualTo(100);

        setValue(45);
        sensor.zero();
        assertAngle(45, 0);
        assertThat(inverted.getAngle()).isEqualTo(0);
        assertAngle(90, 45);
        assertThat(inverted.getAngle()).isEqualTo(-45);
        assertAngle(0, -45);
        assertThat(inverted.getAngle()).isEqualTo(45);
    }
}
