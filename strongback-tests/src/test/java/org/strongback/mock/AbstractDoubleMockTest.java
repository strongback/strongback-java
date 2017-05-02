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

import static org.fest.assertions.Assertions.assertThat;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import org.fest.assertions.Delta;
import org.junit.Before;
import org.junit.Test;
import org.strongback.components.Zeroable;

/**
 * A base class for tests of mock components that have double getters and setters.
 *
 * @author Randall Hauch
 */
public abstract class AbstractDoubleMockTest {

    protected static double[] TEST_VALUES = { 0.01, 1.0, 0.0000001, 2.000001, 5.5, 0.99999, -1.0, -0.01, -0.000001, -2.00001,
            -5.5 };
    protected static double TEST_VALUE = TEST_VALUES[0];
    protected static Delta NEAR_ZERO = Delta.delta(0.0000000000000001d);
    protected static Delta NOMINAL_TOLERANCE = Delta.delta(0.00001d);

    private DoubleConsumer setter;
    private DoubleSupplier getter;
    private Zeroable zeroable;

    /**
     * Initialize the test with the mock class's setter and getter methods. This should be called within the {@link Before}
     * method of the subclass.
     *
     * @param setter the method that sets the double value; may not be null
     * @param getter the method that gets the double value; may not be null
     */
    protected void initialize(DoubleConsumer setter, DoubleSupplier getter) {
        this.setter = setter;
        this.getter = getter;
        this.zeroable = null;
    }

    /**
     * Initialize the test with the {@link Zeroable} mock class's setter and getter methods. This should be called within the
     * {@link Before} method of the subclass.
     *
     * @param setter the method that sets the double value; may not be null
     * @param getter the method that gets the double value; may not be null
     * @param zeroable the {@link Zeroable} reference; may be null if the component is not {@link Zeroable}
     */
    protected void initialize(DoubleConsumer setter, DoubleSupplier getter, Zeroable zeroable) {
        this.setter = setter;
        this.getter = getter;
        this.zeroable = zeroable;
    }

    @Test
    public void shouldAllowSettingAndReadingValues() {
        for (double value : TEST_VALUES) {
            assertSetAndGet(value);
        }
    }

    @Test
    public void shouldGetLastSetValue() {
        // Call the setter multiple times ...
        double lastValue = Double.NaN;
        for (double value : TEST_VALUES) {
            setter.accept(value);
            lastValue = value;
        }
        // And calling get once should return the last value that was set ...
        assertThat(getter.getAsDouble()).isEqualTo(lastValue, NOMINAL_TOLERANCE);
    }

    @Test
    public void shouldZeroWhenValueIsZero() {
        if (zeroable == null) return;
        setter.accept(0.0);
        zeroable.zero();
        assertThat(getter.getAsDouble()).isEqualTo(0.0,NEAR_ZERO);
    }

    @Test
    public void shouldZeroWhenValueIsNonZero() {
        if (zeroable == null) return;
        for (double value : TEST_VALUES) {
            assertSetAndZeroAndGet(value);
        }
    }

    protected void assertSetAndGet(double value) {
        assertSetAndGet(value, NOMINAL_TOLERANCE);
    }

    protected void assertSetAndGet(double value, Delta tolerance) {
        setter.accept(value);
        double actual = getter.getAsDouble();
        assertThat(actual).isEqualTo(value, tolerance);
    }

    protected void assertSetAndZeroAndGet(double value) {
        // reset to 0 value and zero ...
        setter.accept(0.0);
        zeroable.zero();
        assertThat(getter.getAsDouble()).isEqualTo(0.0, NEAR_ZERO);

        // Set a non-zero value ...
        setter.accept(value);
        zeroable.zero();

        // Check that the value is now zero ...
        double actual = getter.getAsDouble();
        assertThat(actual).isEqualTo(0.0, NOMINAL_TOLERANCE);

        // Set the value to multiples of the original ...
        for (int i = 2; i != 6; ++i) {
            double nonZeroed = value * i;
            setter.accept(nonZeroed);
            // Check that the difference is properly adjusted (even for negative values) ...
            actual = getter.getAsDouble();
            assertThat(actual).isEqualTo(nonZeroed - value, NOMINAL_TOLERANCE);
        }

        // Set the value to negative multiples of the original ...
        for (int i = 2; i != 6; ++i) {
            double nonZeroed = -value * i;
            setter.accept(nonZeroed);
            // Check that the difference is properly adjusted (even for negative values) ...
            actual = getter.getAsDouble();
            assertThat(actual).isEqualTo(nonZeroed - value, NOMINAL_TOLERANCE);
        }
    }

}
