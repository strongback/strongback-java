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

import java.util.ArrayList;
import java.util.List;

import org.strongback.components.PowerPanel;

/**
 * A mock implementation of the {@link PowerPanel}, which has 16 channels
 * @author Randall Hauch
 */
public class MockPowerPanel implements PowerPanel {

    private final List<MockCurrentSensor> channels;
    private final MockTemperatureSensor temperature = new MockTemperatureSensor().setTemperature(72.0);
    private final MockCurrentSensor totalCurrent = new MockCurrentSensor().setCurrent(10.0);
    private final MockVoltageSensor voltage = new MockVoltageSensor().setVoltage(12.0);

    /**
     * Create a mock power panel with the specified number of channels.
     * @param numChannels the number of channels; must be positive
     */
    public MockPowerPanel( int numChannels ) {
        channels = new ArrayList<>(numChannels);
        for ( int i= 0; i!= numChannels; ++i ) {
            channels.add(new MockCurrentSensor());
        }
    }

    @Override
    public MockCurrentSensor getCurrentSensor(int channel) {
        return channels.get(channel);
    }

    @Override
    public MockTemperatureSensor getTemperatureSensor() {
        return temperature;
    }

    @Override
    public MockCurrentSensor getTotalCurrentSensor() {
        return totalCurrent;
    }

    @Override
    public MockVoltageSensor getVoltageSensor() {
        return voltage;
    }

}
