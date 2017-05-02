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

import org.junit.Before;
import org.junit.Test;

/**
 * @author Randall Hauch
 */
public class MockCompassTest extends AbstractDoubleMockTest {

    private static final double[][] POSITIVE_ANGLES = { { 0.0, 0.0 }, { 180.0, 180.0 }, { 359.0, 359.0 }, { 360.0, 0.0 },
            { 360.001, 0.001 }, { 361.0, 1.0 }, { 360.0 * 2, 0.0 }, { 360.0 * 3, 0.0 }, { 360.0 * 4, 0.0 } };

    private static final double[][] NEGATIVE_ANGLES = { { -1.0, 359 }, { -90.0, 270 }, { -180, 180 }, { -270, 90 },
            { -360, 0 }, { -361, 359 }, { -360 * 2, 0 } };

    private MockCompass mock;

    @Before
    public void beforeEach() {
        mock = new MockCompass();
        initialize(mock::setAngle, mock::getAngle, mock);
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

    protected void assertProperHeading(double angle, double expectedHeading) {
        mock.setAngle(angle);
        assertThat(mock.getAngle()).isEqualTo(angle, NOMINAL_TOLERANCE);
        assertThat(mock.getHeading()).isEqualTo(expectedHeading, NOMINAL_TOLERANCE);
    }
}
