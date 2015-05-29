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
import org.strongback.components.LimitedMotor;
import org.strongback.components.LimitedMotor.Position;
import org.strongback.components.Motor.Direction;
import org.strongback.mock.Mock;
import org.strongback.mock.MockMotor;
import org.strongback.mock.MockSwitch;

/**
 * @author Randall Hauch
 *
 */
public class LimitedMotorTest {

    private MockSwitch forwardSwitch = Mock.notTriggeredSwitch();
    private MockSwitch reverseSwitch = Mock.notTriggeredSwitch();
    private MockMotor motor = Mock.stoppedMotor();
    LimitedMotor limited = LimitedMotor.create(motor, forwardSwitch, reverseSwitch);

    @Before
    public void beforeEach() {
        forwardSwitch.setNotTriggered();
        reverseSwitch.setNotTriggered();
        motor.stop();
    }

    @Test
    public void shouldLimitMotorInForwardDirection() {
        limited.forward(1.0);
        assertForwardRotation(1.0);
        limited.forward(0.1);
        assertForwardRotation(0.1);
        forwardSwitch.setTriggered();
        assertStoppedAtForwardLimit();
    }

    @Test
    public void shouldLimitMotorInReverseDirection() {
        limited.reverse(1.0);
        assertReverseRotation(1.0);
        limited.reverse(0.1);
        assertReverseRotation(0.1);
        reverseSwitch.setTriggered();
        assertStoppedAtReverseLimit();
    }


    @Test
    public void shouldNotLimitMotorInForwardDirectionWhenReverseLimitIsSwitched() {
        limited.forward(1.0);
        assertForwardRotation(1.0);
        limited.forward(0.1);
        assertForwardRotation(0.1);
        reverseSwitch.setTriggered();   // doesn't affect anything ...
        assertForwardRotation(0.1);
        forwardSwitch.setTriggered();
        assertStoppedAtUnknonwPosition();   // since both switches are triggered, we don't know where we are
    }

    @Test
    public void shouldNotLimitMotorInReverseDirectionWhenForwardLimitIsSwitched() {
        limited.reverse(1.0);
        assertReverseRotation(1.0);
        limited.reverse(0.1);
        assertReverseRotation(0.1);
        forwardSwitch.setTriggered();   // doesn't affect anything ...
        assertReverseRotation(0.1);
        reverseSwitch.setTriggered();
        assertStoppedAtUnknonwPosition();   // since both switches are triggered, we don't know where we are
    }
    protected void assertForwardRotation(double speed) {
        assertThat(limited.getSpeed()).isEqualTo(speed);
        assertThat(limited.isAtForwardLimit()).isEqualTo(false);
        //assertThat(limited.isAtReverseLimit()).isEqualTo(false);  // not necessarily the case in all situations
        assertThat(limited.getPosition()).isEqualTo(Position.UNKNOWN);
        assertThat(limited.getDirection()).isEqualTo(Direction.FORWARD);
    }

    protected void assertReverseRotation(double speed) {
        assertThat(limited.getSpeed()).isEqualTo(-1*speed);
        //assertThat(limited.isAtForwardLimit()).isEqualTo(false);  // not necessarily the case in all situations
        assertThat(limited.isAtReverseLimit()).isEqualTo(false);
        assertThat(limited.getPosition()).isEqualTo(Position.UNKNOWN);
        assertThat(limited.getDirection()).isEqualTo(Direction.REVERSE);
    }

    protected void assertStoppedAtForwardLimit() {
        assertThat(limited.getSpeed()).isEqualTo(0.0);
        assertThat(limited.isAtForwardLimit()).isEqualTo(true);
        //assertThat(limited.isAtReverseLimit()).isEqualTo(false);  // not necessarily the case in all situations
        assertThat(limited.getPosition()).isEqualTo(Position.FORWARD_LIMIT);
        assertThat(limited.getDirection()).isEqualTo(Direction.STOPPED);
    }

    protected void assertStoppedAtReverseLimit() {
        assertThat(limited.getSpeed()).isEqualTo(0.0,Delta.delta(0.00001));
        //assertThat(limited.isAtForwardLimit()).isEqualTo(false);  // not necessarily the case in all situations
        assertThat(limited.isAtReverseLimit()).isEqualTo(true);
        assertThat(limited.getPosition()).isEqualTo(Position.REVERSE_LIMIT);
        assertThat(limited.getDirection()).isEqualTo(Direction.STOPPED);
    }

    protected void assertStoppedAtUnknonwPosition() {
        assertThat(limited.getSpeed()).isEqualTo(0.0,Delta.delta(0.00001));
        //assertThat(limited.isAtForwardLimit()).isEqualTo(false);  // not necessarily the case in all situations
        assertThat(limited.isAtReverseLimit()).isEqualTo(true);
        assertThat(limited.getPosition()).isEqualTo(Position.UNKNOWN);
        assertThat(limited.getDirection()).isEqualTo(Direction.STOPPED);
    }

}
