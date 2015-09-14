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

package org.strongback.components;

import java.util.function.Supplier;

import org.strongback.annotation.ThreadSafe;

/**
 * A special {@link Solenoid} that can determine when the solenoid is retracted, extended, or somewhere in between.
 */
@ThreadSafe
public interface SolenoidWithPosition extends Solenoid {

    /**
     * The possible positions for a limited motor.
     */
    public enum Position {
        /** The motor is fully extended. **/
        EXTENDED,
        /** The motor is fully retracted. **/
        RETRACTED,
        /** The motor is not fully retracted or fully extended, but the exact position is unknown. **/
        UNKNOWN
    }

    /**
     * Get the current position of this solenoid.
     *
     * @return the current position; never null
     */
    public Position getPosition();

    /**
     * Determines if this <code>Solenoid</code> is extended.
     *
     * @return {@code true} if this solenoid is fully extended, or {@code false} otherwise
     */
    default public boolean isExtended() {
        return getPosition() == Position.EXTENDED;
    }

    /**
     * Determines if this <code>Solenoid</code> is retracted.
     *
     * @return {@code true} if this solenoid is fully retracted, or {@code false} otherwise
     */
    default public boolean isRetracted() {
        return getPosition() == Position.RETRACTED;
    }


    /**
     * Create a solenoid that uses the supplied function to determine the position.
     * @param solenoid the solenoid; may not be null
     * @param positionSupplier the function that returns the position; may not be null
     * @return the {@link SolenoidWithPosition} instance; never null
     */
    public static SolenoidWithPosition create(Solenoid solenoid, Supplier<Position> positionSupplier ) {
        return new SolenoidWithPosition() {
            private Position position = positionSupplier.get();

            @Override
            public Position getPosition() {
                position = positionSupplier.get();
                return position;
            }

            @Override
            public Direction getDirection() {
                return solenoid.getDirection();
            }

            @Override
            public SolenoidWithPosition extend() {
                solenoid.extend();
                position = positionSupplier.get();
                return this;
            }

            @Override
            public SolenoidWithPosition retract() {
                solenoid.retract();
                position = positionSupplier.get();
                return this;
            }

            @Override
            public String toString() {
                return "SolenoidWithPosition: " + position + " (solenoid: " + solenoid + ")";
            }
        };
    }

    /**
     * Create a solenoid that uses the two given switches to determine position.
     * @param solenoid the solenoid; may not be null
     * @param retractSwitch the switch that determines if the solenoid is retracted; may not be null
     * @param extendSwitch the switch that determines if the solenoid is extended; may not be null
     * @return the {@link SolenoidWithPosition} instance; never null
     */
    public static SolenoidWithPosition create(Solenoid solenoid, Switch retractSwitch, Switch extendSwitch ) {
        return create(solenoid,()->{
            if ( extendSwitch.isTriggered() ) {
                if ( retractSwitch.isTriggered() ) {
                    // Both switches are triggered -- WTF???
                    return Position.UNKNOWN;
                }
                // Extended but not retracted ...
                return Position.EXTENDED;
            }
            if ( retractSwitch.isTriggered() ) {
                // Retracted but not extended
                return Position.RETRACTED;
            }
            // Neither switch is triggered ...
            return Position.UNKNOWN;
        });
    }
}