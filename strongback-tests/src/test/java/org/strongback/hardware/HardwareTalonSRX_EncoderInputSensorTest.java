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
import org.strongback.hardware.HardwareTalonSRX.EncoderInputSensor;

public class HardwareTalonSRX_EncoderInputSensorTest {

    private static final Delta DELTA = Delta.delta(0.00001);

    private double positionInEdges = 0.0;
    private double velocityInEdges = 0.0;
    private double cyclePeriodInSeconds = 0.1;
    private double rotationInDegrees = 0.0;
    private int encoderPulsesPerRotation = 360;
    private int encoderEdgesPerPulse = 4; // 4x mode, or 4 rising edges per pulse
    private EncoderInputSensor sensor;

    @Before
    public void beforeEach() {
        cyclePeriodInSeconds = 0.1;
    }

    protected EncoderInputSensor createSensor() {
        sensor = new EncoderInputSensor(() -> positionInEdges, () -> velocityInEdges, encoderPulsesPerRotation / 360.0, // pulses
                                                                                                                        // per
                                                                                                                        // degree
                encoderEdgesPerPulse, () -> cyclePeriodInSeconds);
        return sensor;
    }

    @Test
    public void shouldReturnZeroWhenMeasuredPositionAndVelocityAreZero() {
        encoderPulsesPerRotation = 360;
        positionInEdges = 0.0;
        velocityInEdges = 0.0;
        createSensor();
        assertThat(sensor.getAngle()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.getHeading()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.getRate()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.rawPositionForAngleInDegrees(0.0d)).isEqualTo(positionInEdges, DELTA);
    }

    @Test
    public void shouldReturnZeroWhenMeasuredPositionIsHalfRotationAndVelocityAreZero() {
        encoderPulsesPerRotation = 360;
        positionInEdges = 180 * 4;  // edges for 180 degrees
        velocityInEdges = 0.0;
        createSensor();
        assertThat(sensor.getAngle()).isEqualTo(180.0d, DELTA);
        assertThat(sensor.getHeading()).isEqualTo(180.0d, DELTA);
        assertThat(sensor.getRate()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.rawPositionForAngleInDegrees(180.0d)).isEqualTo(positionInEdges, DELTA);
    }

    @Test
    public void shouldReturnZeroWhenMeasuredPositionIsOneAndOneHalfRotationAndVelocityAreZero() {
        encoderPulsesPerRotation = 360;
        positionInEdges = (180+360) * 4;  // edges for 540 degrees
        velocityInEdges = 0.0;
        createSensor();
        assertThat(sensor.getAngle()).isEqualTo(540.0d, DELTA);
        assertThat(sensor.getHeading()).isEqualTo(180.0d, DELTA);
        assertThat(sensor.getRate()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.rawPositionForAngleInDegrees(540.0d)).isEqualTo(positionInEdges, DELTA);
    }

    @Test
    public void shouldReturnZeroWhenMeasuredPositionIsThreeAndOneHalfRotationAndVelocityAreZero() {
        encoderPulsesPerRotation = 360;
        positionInEdges = (180+360*3) * 4;  // edges for 1260 degrees
        velocityInEdges = 0.0;
        createSensor();
        assertThat(sensor.getAngle()).isEqualTo(1260.0d, DELTA);
        assertThat(sensor.getHeading()).isEqualTo(180.0d, DELTA);
        assertThat(sensor.getRate()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.rawPositionForAngleInDegrees(1260.0d)).isEqualTo(positionInEdges, DELTA);
    }

    @Test
    public void shouldReturnZeroWhenMeasuredPositionIsThreeAndOneHalfRotationAndVelocityAreZeroWithPrecisionEncoder() {
        encoderPulsesPerRotation = 250;
        rotationInDegrees = 180+360*3;
        positionInEdges = rotationInDegrees * encoderPulsesPerRotation /360 * 4;
        velocityInEdges = 0.0;
        createSensor();
        assertThat(sensor.getAngle()).isEqualTo(1260.0d, DELTA);
        assertThat(sensor.getHeading()).isEqualTo(180.0d, DELTA);
        assertThat(sensor.getRate()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.rawPositionForAngleInDegrees(1260.0d)).isEqualTo(positionInEdges, DELTA);
    }

    @Test
    public void shouldHandleMeasuredPositionOfOneAndVelocityOfTwenty() {
        double rotationsPerSecond = 20;
        encoderPulsesPerRotation = 360;
        rotationInDegrees = 360;
        positionInEdges = rotationInDegrees * encoderPulsesPerRotation /360 * 4;
        velocityInEdges = rotationsPerSecond * 4 * 360.0 * cyclePeriodInSeconds; // per cycle, or 28800 position units / second
        createSensor();
        assertThat(sensor.getAngle()).isEqualTo(360.0, DELTA);
        assertThat(sensor.getHeading()).isEqualTo(0.0d, DELTA);
        assertThat(sensor.getRate()).isEqualTo(rotationsPerSecond * 360.0, DELTA); // degrees per second
        assertThat(sensor.rawPositionForAngleInDegrees(360.0d)).isEqualTo(positionInEdges, DELTA);
    }

}
