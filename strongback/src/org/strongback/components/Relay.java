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

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.strongback.annotation.ThreadSafe;

/**
 * A relay is a device that can be turned on and off. Note that a relay has one of 5 possible states:
 * <ol>
 * <li>ON - the relay is in the "on" position;</li>
 * <li>OFF - the relay is in the "off" position;</li>
 * <li>SWITCHING_ON - the relay was in the "off" position but has been changed and is not yet in the "on" position;</li>
 * <li>SWITCHING_OFF - the relay was in the "on" position but has been changed and is not yet in the "off" position; and</li>
 * <li>UNKNOWN - the relay position is not known</li>
 * </ol>
 * <p>
 * Not all Relay implementations use all relay states. Very simple relays that have no delay will simply only use
 * {@link State#ON} and {@link State#OFF}, while relays that have some delay might also use {@link State#SWITCHING_ON} and
 * {@link State#SWITCHING_OFF}. Those relay implementations that may not know their position upon startup may start out in the
 * UNKNOWN state.
 *
 * @author Zach Anderson
 *
 */
@ThreadSafe
public interface Relay {

    static enum State {
        /** The relay is presently switching into the "ON" state but has not yet completed the change. */
        SWITCHING_ON,
        /** The relay is presently in the "on" position. */
        ON,
        /** The relay is presently switching into the "OFF" state but has not yet completed the change. */
        SWITCHING_OFF,
        /** The relay is presently in the "off" position. */
        OFF,
        /** The actual state of the relay is not known. */
        UNKOWN
    }

    /**
     * Get the current state of this relay.
     *
     * @return the current state; never null
     */
    State state();

    /**
     * Turn on this relay.
     *
     * @return this object to allow chaining of methods; never null
     */
    Relay on();

    /**
     * Turn off this relay.
     *
     * @return this object to allow chaining of methods; never null
     */
    Relay off();

    /**
     * Check whether this relay is known to be on. This is equivalent to calling {@code state() == State.ON}.
     *
     * @return {@code true} if this relay is on; or {@code false} otherwise
     */
    default boolean isOn() {
        return state() == State.ON;
    }

    /**
     * Check whether this relay is known to be off. This is equivalent to calling {@code state() == State.OFF}.
     *
     * @return {@code true} if this relay is off; or {@code false} otherwise
     */
    default boolean isOff() {
        return state() == State.OFF;
    }

    /**
     * Check if this relay is switching on. This is equivalent to calling {@code state() == State.SWITCHING_ON}.
     *
     * @return {@code true} if this relay is in the process of switching from off to on; or {@code false} otherwise
     */
    default boolean isSwitchingOn() {
        return state() == State.SWITCHING_ON;
    }

    /**
     * Check if this relay is switching off. This is equivalent to calling {@code state() == State.SWITCHING_OFF}.
     *
     * @return {@code true} if this relay is in the process of switching from on to off; or {@code false} otherwise
     */
    default boolean isSwitchingOff() {
        return state() == State.SWITCHING_OFF;
    }

    /**
     * Obtain a relay that remains in one fixed state, regardless of any calls to {@link #on()} or {@link #off()}.
     *
     * @param state the fixed state; may not be null
     * @return the constant relay; never null
     */
    static Relay fixed(State state) {
        return new Relay() {
            @Override
            public State state() {
                return state;
            }

            @Override
            public Relay on() {
                return this;
            }

            @Override
            public Relay off() {
                return this;
            }
        };
    }

    /**
     * Obtain a relay that instantaneously switches from one state to another using the given functions.
     *
     * @param switcher the function that switches the state, where <code>true</code> represents {@link State#ON} and
     *        <code>false</code> represents {@link State#OFF}; may not be null
     * @param onState the function that returns <code>true</code> if the current state is {@link State#ON}, or
     *        <code>false</code> otherwise; may not be null
     * @return the relay; never null
     */
    static Relay instantaneous(Consumer<Boolean> switcher, BooleanSupplier onState) {
        return new Relay() {
            @Override
            public State state() {
                return onState.getAsBoolean() ? State.ON : State.OFF;
            }

            @Override
            public Relay on() {
                switcher.accept(Boolean.TRUE);
                return this;
            }

            @Override
            public Relay off() {
                switcher.accept(Boolean.FALSE);
                return this;
            }
        };
    }
}