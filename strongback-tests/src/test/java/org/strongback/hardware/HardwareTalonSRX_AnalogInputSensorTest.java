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

package org.strongback.hardware;

import static org.fest.assertions.Assertions.assertThat;

import org.fest.assertions.Delta;
import org.junit.Before;
import org.junit.Test;
import org.strongback.hardware.HardwareTalonSRX.AnalogInputSensor;

public class HardwareTalonSRX_AnalogInputSensorTest {

    private static final Delta DELTA = Delta.delta(1);

    private double analogPosition = 0.0;
    private double analogVelocity = 0.0; // changes in volts per cycle
    private double analogRange = 1023;   // 10 bit values
    private double analogTurnsOverVoltageRange = 1;
    private double analogVoltageRange = 3.3;
    private double cyclePeriodInSeconds = 0.1;
    private AnalogInputSensor sensor;

    @Before
    public void beforeEach() {
    }

    protected AnalogInputSensor createSensor() {
        sensor = new AnalogInputSensor(() -> analogPosition,
                                       () -> analogVelocity,
                                       analogRange,
                                       analogTurnsOverVoltageRange / analogVoltageRange,
                                       analogVoltageRange,
                                       () -> cyclePeriodInSeconds);
        return sensor;
    }

    @Test
    public void shouldReturnZeroWhenMeasuredPositionAndVelocityAreZero() {
        analogPosition = 0;
        analogTurnsOverVoltageRange = 1;
        createSensor();
        assertThat(sensor.getAngle()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.getHeading()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.getRate()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.rawPositionForAngleInDegrees(0.0d)).isEqualTo(analogPosition, DELTA);
    }

    @Test
    public void shouldReturnZeroWhenMeasuredPositionIsHalfRotationAndVelocityAreZero() {
        analogPosition = 1024/2 - 1;
        analogTurnsOverVoltageRange = 1;
        createSensor();
        assertThat(sensor.getAngle()).isEqualTo(180.0d, DELTA);
        assertThat(sensor.getHeading()).isEqualTo(180.0d, DELTA);
        assertThat(sensor.getRate()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.rawPositionForAngleInDegrees(180.0d)).isEqualTo(analogPosition, DELTA);
    }

    @Test
    public void shouldReturnZeroWhenMeasuredPositionIsOneRotationAndVelocityAreZero() {
        analogPosition = 1024-1;
        analogTurnsOverVoltageRange = 1;
        createSensor();
        assertThat(sensor.getAngle()).isEqualTo(360.0d, DELTA);
        assertThat(sensor.getHeading()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.getRate()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.rawPositionForAngleInDegrees(360.0d)).isEqualTo(analogPosition, DELTA);
    }

    @Test
    public void shouldReturnZeroWhenMeasuredPositionIsOneAndOneHalfRotationAndVelocityAreZero() {
        analogPosition = (int)(1024 * 1.5) - 1;
        analogTurnsOverVoltageRange = 1;
        createSensor();
        assertThat(sensor.getAngle()).isEqualTo(540.0d, DELTA);
        assertThat(sensor.getHeading()).isEqualTo(180.0d, DELTA);
        assertThat(sensor.getRate()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.rawPositionForAngleInDegrees(540.0d)).isEqualTo(analogPosition, DELTA);
    }

    @Test
    public void shouldReturnZeroWhenMeasuredPositionIsThreeAndOneHalfRotationAndVelocityAreZero() {
        analogPosition = (int)(1024 * 3.5) - 1;
        analogTurnsOverVoltageRange = 1;
        createSensor();
        assertThat(sensor.getAngle()).isEqualTo(1260.0d, DELTA);
        assertThat(sensor.getHeading()).isEqualTo(180.0d, DELTA);
        assertThat(sensor.getRate()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.rawPositionForAngleInDegrees(1260.0d)).isEqualTo(analogPosition, Delta.delta(5.0));
    }

    @Test
    public void shouldHandleMeasuredPositionAndVelocity() {
        analogPosition = (int)(1024 * 3.5) - 1;
        analogVelocity = 1023; // starts from 0 and does full rotation
        analogTurnsOverVoltageRange = 1;
        createSensor();
        assertThat(sensor.getAngle()).isEqualTo(1260.0d, DELTA);
        assertThat(sensor.getHeading()).isEqualTo(180.0d, DELTA);
        assertThat(sensor.getRate()).isEqualTo(360.0 / cyclePeriodInSeconds, DELTA); // degrees per second
        assertThat(sensor.rawPositionForAngleInDegrees(1260.0d)).isEqualTo(analogPosition, Delta.delta(5.0));
    }

}
