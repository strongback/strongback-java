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

/**
 * A motor controlled by a Talon SRX with built-in current sensor, position (angle) sensor, and optional external limit switches
 * wired into the SRX so that it can automatically stop the forward and reverse directions when the limit switches are
 * triggered.
 */
public interface TalonSRX extends LimitedMotor {

    @Override
    public TalonSRX setSpeed(double speed);

    /**
     * Get the angle sensor (encoder) hooked up to the Talon SRX motor controller.
     *
     * @return the angle sensor; never null, but if not hooked up the sensor will always return a meaningless value
     */
    public AngleSensor getAngleSensor();

    /**
     * Get the Talon SRX's current sensor.
     *
     * @return the current sensor; never null
     */
    public CurrentSensor getCurrentSensor();

}