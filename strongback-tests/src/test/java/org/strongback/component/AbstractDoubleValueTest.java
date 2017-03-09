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

import java.util.function.DoubleSupplier;

import org.fest.assertions.Delta;
import org.junit.Before;

/**
 * @author Randall Hauch
 *
 */
abstract class AbstractDoubleValueTest {

    private static double value;

    protected static double getValue() {
        return value;
    }

    protected static void setValue( double value) {
        AbstractDoubleValueTest.value = value;
    }

    protected static void assertValue(double value, DoubleSupplier getter, double result ) {
        setValue(value);
        assertThat(getter.getAsDouble()).isEqualTo(result, Delta.delta(0.0001));
    }

    @Before
    public void beforeEach() {
        value = 0;
    }
}
