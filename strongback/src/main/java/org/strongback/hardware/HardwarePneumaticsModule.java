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

import org.strongback.annotation.ThreadSafe;
import org.strongback.components.CurrentSensor;
import org.strongback.components.PneumaticsModule;
import org.strongback.components.Relay;
import org.strongback.components.Switch;

import edu.wpi.first.wpilibj.Compressor;

/**
 * A {@link PneumaticsModule} implementation based upon the WPILib's {@link Compressor} class, which represents the Pneumatics
 * Control Module.
 *
 * @author Randall Hauch
 */
@ThreadSafe
class HardwarePneumaticsModule implements PneumaticsModule {

    private final Compressor pcm;
    private final Relay closedLoop;
    private final Faults instantaneousFaults;
    private final Faults stickyFaults;

    HardwarePneumaticsModule(Compressor pcm) {
        this.pcm = pcm;
        this.closedLoop = Relay.instantaneous(this.pcm::setClosedLoopControl, this.pcm::getClosedLoopControl);
        this.instantaneousFaults = new Faults() {
            @Override
            public Switch currentTooHigh() {
                return pcm::getCompressorCurrentTooHighFault;
            }
            @Override
            public Switch notConnected() {
                return pcm::getCompressorNotConnectedFault;
            }
            @Override
            public Switch shorted() {
                return pcm::getCompressorShortedFault;
            }
        };
        this.stickyFaults = new Faults() {
            @Override
            public Switch currentTooHigh() {
                return pcm::getCompressorCurrentTooHighStickyFault;
            }
            @Override
            public Switch notConnected() {
                return pcm::getCompressorNotConnectedStickyFault;
            }
            @Override
            public Switch shorted() {
                return pcm::getCompressorShortedStickyFault;
            }
        };
    }

    @Override
    public CurrentSensor compressorCurrent() {
        return pcm::getCompressorCurrent;
    }

    @Override
    public Switch compressorRunningSwitch() {
        return pcm::enabled;
    }

    @Override
    public Relay automaticMode() {
        return closedLoop;
    }

    @Override
    public Switch lowPressureSwitch() {
        return pcm::getPressureSwitchValue;
    }

    @Override
    public Faults compressorFaults() {
        return instantaneousFaults;
    }

    @Override
    public Faults compressorStickyFaults() {
        return stickyFaults;
    }

    @Override
    public PneumaticsModule clearStickyFaults() {
        pcm.clearAllPCMStickyFaults();
        return this;
    }
}
