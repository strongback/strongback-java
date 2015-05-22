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

import org.strongback.components.TalonSRX;

/**
 * @author Randall Hauch
 *
 */
public class MockTalonSRX extends MockMotor implements TalonSRX {

    private final MockAngleSensor encoder = new MockAngleSensor();
    private final MockCurrentSensor current = new MockCurrentSensor();
    private final MockSwitch forwardLimitSwitch = new MockSwitch();
    private final MockSwitch reverseLimitSwitch = new MockSwitch();

    protected MockTalonSRX(double speed) {
        super(speed);
    }

    @Override
    public MockTalonSRX setSpeed(double speed) {
        super.setSpeed(speed);
        return this;
    }

    @Override
    public MockAngleSensor getAngleSensor() {
        return encoder;
    }

    @Override
    public MockCurrentSensor getCurrentSensor() {
        return current;
    }

    @Override
    public MockSwitch getForwardLimitSwitch() {
        return forwardLimitSwitch;
    }

    @Override
    public MockSwitch getReverseLimitSwitch() {
        return reverseLimitSwitch;
    }

}
