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

package org.strongback.util;

import static org.fest.assertions.Assertions.assertThat;

import org.fest.assertions.Delta;
import org.junit.Test;
import org.strongback.function.DoubleToDoubleFunction;

public class ValuesTest {

    private static final Delta TOLERANCE = Delta.delta(0.00001);

    @Test
    public void shouldMapRangeFromZeroCenteredToPositiveOne() {
        DoubleToDoubleFunction func = Values.mapRange(-1.0,1.0,0.0,1.0);

        // Verify minimum range is properly limited ...
        assertThat(func.applyAsDouble(-1.0)).isEqualTo(0.0, TOLERANCE);
        assertThat(func.applyAsDouble(-1.01)).isEqualTo(0.0, TOLERANCE);
        assertThat(func.applyAsDouble(-2.0)).isEqualTo(0.0, TOLERANCE);

        // Verify maximum range is properly limited ...
        assertThat(func.applyAsDouble(1.0)).isEqualTo(1.0, TOLERANCE);
        assertThat(func.applyAsDouble(1.01)).isEqualTo(1.0, TOLERANCE);
        assertThat(func.applyAsDouble(2.0)).isEqualTo(1.0, TOLERANCE);

        // Verify mid-range
        assertThat(func.applyAsDouble(0.0)).isEqualTo(0.5, TOLERANCE);

        // Verify other values within the range ...
        assertThat(func.applyAsDouble(-0.75)).isEqualTo(0.125, TOLERANCE);
        assertThat(func.applyAsDouble(-0.5)).isEqualTo(0.25, TOLERANCE);
        assertThat(func.applyAsDouble(-0.25)).isEqualTo(0.375, TOLERANCE);
        assertThat(func.applyAsDouble(0.25)).isEqualTo(0.625, TOLERANCE);
        assertThat(func.applyAsDouble(0.5)).isEqualTo(0.75, TOLERANCE);
        assertThat(func.applyAsDouble(0.75)).isEqualTo(0.875, TOLERANCE);
    }

    @Test
    public void shouldMapRangeUsingTranslationOnly() {
        DoubleToDoubleFunction func = Values.mapRange(0.0,4.0,10.0,14.0);

        // Verify minimum range is properly limited ...
        assertThat(func.applyAsDouble(0.0)).isEqualTo(10.0, TOLERANCE);
        assertThat(func.applyAsDouble(-0.01)).isEqualTo(10.0, TOLERANCE);
        assertThat(func.applyAsDouble(-2.0)).isEqualTo(10.0, TOLERANCE);

        // Verify maximum range is properly limited ...
        assertThat(func.applyAsDouble(4.0)).isEqualTo(14.0, TOLERANCE);
        assertThat(func.applyAsDouble(4.01)).isEqualTo(14.0, TOLERANCE);
        assertThat(func.applyAsDouble(6.0)).isEqualTo(14.0, TOLERANCE);

        // Verify mid-range
        assertThat(func.applyAsDouble(2.0)).isEqualTo(12.0, TOLERANCE);

        // Verify other values within the range ...
        assertThat(func.applyAsDouble(0.5)).isEqualTo(10.5, TOLERANCE);
        assertThat(func.applyAsDouble(1.0)).isEqualTo(11.0, TOLERANCE);
        assertThat(func.applyAsDouble(1.5)).isEqualTo(11.5, TOLERANCE);
        assertThat(func.applyAsDouble(2.0)).isEqualTo(12.0, TOLERANCE);
        assertThat(func.applyAsDouble(2.5)).isEqualTo(12.5, TOLERANCE);
        assertThat(func.applyAsDouble(3.0)).isEqualTo(13.0, TOLERANCE);
        assertThat(func.applyAsDouble(3.5)).isEqualTo(13.5, TOLERANCE);
        assertThat(func.applyAsDouble(4.0)).isEqualTo(14.0, TOLERANCE);
    }

    @Test
    public void shouldMapRangeUsingScaleOnly() {
        DoubleToDoubleFunction func = Values.mapRange(1.0,5.0).toRange(1.0,2.0);

        // Verify minimum range is properly limited ...
        assertThat(func.applyAsDouble(0.0)).isEqualTo(1.0, TOLERANCE);
        assertThat(func.applyAsDouble(-0.01)).isEqualTo(1.0, TOLERANCE);
        assertThat(func.applyAsDouble(-2.0)).isEqualTo(1.0, TOLERANCE);

        // Verify maximum range is properly limited ...
        assertThat(func.applyAsDouble(5.0)).isEqualTo(2.0, TOLERANCE);
        assertThat(func.applyAsDouble(5.01)).isEqualTo(2.0, TOLERANCE);
        assertThat(func.applyAsDouble(5.0)).isEqualTo(2.0, TOLERANCE);

        // Verify mid-range
        assertThat(func.applyAsDouble(3.0)).isEqualTo(1.5, TOLERANCE);

        // Verify other values within the range ...
        assertThat(func.applyAsDouble(1.04)).isEqualTo(1.01, TOLERANCE);
        assertThat(func.applyAsDouble(1.8)).isEqualTo(1.2, TOLERANCE);
        assertThat(func.applyAsDouble(2.0)).isEqualTo(1.25, TOLERANCE);
        assertThat(func.applyAsDouble(2.6)).isEqualTo(1.4, TOLERANCE);
        assertThat(func.applyAsDouble(3.4)).isEqualTo(1.6, TOLERANCE);
        assertThat(func.applyAsDouble(3.8)).isEqualTo(1.7, TOLERANCE);
        assertThat(func.applyAsDouble(4.2)).isEqualTo(1.8, TOLERANCE);
        assertThat(func.applyAsDouble(4.6)).isEqualTo(1.9, TOLERANCE);
    }

}
