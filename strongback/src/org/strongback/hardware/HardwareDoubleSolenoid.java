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

import org.strongback.components.Solenoid;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

/**
 * Wrapper for WPILib {@link DoubleSolenoid}.
 *
 * @author Zach Anderson
 * @see Solenoid
 * @see Hardware
 * @see edu.wpi.first.wpilibj.DoubleSolenoid
 */
final class HardwareDoubleSolenoid implements Solenoid {
    private final DoubleSolenoid solenoid;

    private Direction direction;

    HardwareDoubleSolenoid(DoubleSolenoid solenoid, Direction initialDirection ) {
        assert solenoid != null;
        assert initialDirection != null;
        this.solenoid = solenoid;
        this.direction = initialDirection;
        checkState();
    }

    protected void checkState() {
        if ( solenoid.get() == Value.kForward ) {
            direction = Direction.EXTENDING;
        } else if ( solenoid.get() == Value.kReverse ) {
            direction = Direction.RETRACTING;
        } else {
            direction = Direction.STOPPED;
        }
    }

    @Override
    public Direction getDirection() {
        checkState();
        return direction;
    }

    @Override
    public HardwareDoubleSolenoid extend() {
        solenoid.set(Value.kForward);
        direction = Direction.EXTENDING;
        checkState();
        return this;
    }

    @Override
    public HardwareDoubleSolenoid retract() {
        solenoid.set(Value.kReverse);
        direction = Direction.RETRACTING;
        checkState();
        return this;
    }

    @Override
    public String toString() {
        return "direction = " + direction;
    }
}