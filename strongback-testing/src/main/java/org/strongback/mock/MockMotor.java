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

import org.strongback.components.Motor;

/**
 * A {@link Motor} implementation useful for testing. This motor does nothing but maintain a record of the current speed.
 *
 * @author Randall Hauch
 *
 */
public class MockMotor implements Motor {

    private volatile double speed = 0;

    MockMotor(double speed) {
        this.speed = speed;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public MockMotor setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    @Override
    public MockMotor invert() {
        return new MockMotor(speed) {
            @Override
            public MockMotor setSpeed(double speed) {
                super.setSpeed(-1 * speed);
                return this;
            }

            @Override
            public double getSpeed() {
                return -1 * super.getSpeed();
            }
        };
    }


    @Override
    public String toString() {
        return Double.toString(getSpeed());
    }
}
