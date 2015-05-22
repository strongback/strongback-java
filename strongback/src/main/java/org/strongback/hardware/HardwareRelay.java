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

import org.strongback.components.Relay;

import edu.wpi.first.wpilibj.Relay.Value;

/**
 * Wrapper for the WPILib <code>Relay</code>, and which has no delay and thus is only
 * {@link org.CommandState.robot.component.Relay.State#ON} or {@link org.CommandState.robot.component.Relay.State#OFF}.
 * This class cannot be constructed directly, use <code>HardwareFactory</code> to get instances of it.
 *
 * @author Zach Anderson
 * @see Relay
 * @see Hardware
 * @see edu.wpi.first.wpilibj.Relay
 */
final class HardwareRelay implements Relay {

    private final edu.wpi.first.wpilibj.Relay relay;

    HardwareRelay(int channel) {
        this.relay = new edu.wpi.first.wpilibj.Relay(channel);
    }

    @Override
    public HardwareRelay on() {
        relay.set(Value.kForward);
        return this;
    }

    @Override
    public HardwareRelay off() {
        relay.set(Value.kOff);
        return this;
    }

    @Override
    public State state() {
        Value value = relay.get();
        if (value == Value.kForward || value == Value.kOn) return State.ON;
        if (value == Value.kReverse || value == Value.kOff) return State.OFF;
        return State.UNKOWN;
    }
}