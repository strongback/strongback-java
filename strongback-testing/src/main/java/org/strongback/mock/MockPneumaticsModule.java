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

import org.strongback.components.Fuse;
import org.strongback.components.PneumaticsModule;
import org.strongback.components.Switch;

/**
 * A mock implementation of the {@link PneumaticsModule} that allows the caller to control the {@link #lowPressureSwitch()}.
 * When the module's {@link #automaticMode()} is enabled, then triggering the {@link #lowPressureSwitch() low pressure switch}
 * will start the compressor, and {@link MockSwitch#setNotTriggered() un-triggering} the {@link #lowPressureSwitch() low
 * pressure switch} will stop the compressor.
 *
 * @author Randall Hauch
 */
public class MockPneumaticsModule implements PneumaticsModule {

    public static double COMPRESSOR_CURRENT_WHEN_RUNNING = 10.0;

    private final StickyFaults stickyFaults = new StickyFaults();
    private final MockFaults instantaneousFaults = new MockFaults(stickyFaults);
    private final MockCurrentSensor current = new MockCurrentSensor();
    private final MockSwitch running = new MockSwitch().setNotTriggered();
    private final MockSwitch lowPressure = new MockSwitch() {
        @Override
        public MockSwitch setTriggered(boolean triggered) {
            super.setTriggered(triggered);
            if (automatic.isOn()) {
                // The caller is manually triggering the switch, but this affects whether compressor runs ...
                runCompressor(triggered);
            }
            return this;
        }
    };
    private final MockRelay automatic = new MockRelay() {
        @Override
        public MockRelay off() {
            super.off();
            runCompressor(false);
            return this;
        }

        @Override
        public MockRelay on() {
            super.on();
            return this;
        }
    };

    public MockPneumaticsModule() {
        automatic.on();
        lowPressure.setNotTriggered();
    }

    private void runCompressor( boolean isRunning ) {
        if ( isRunning ) {
            running.setTriggered(isRunning);
            current.setCurrent(COMPRESSOR_CURRENT_WHEN_RUNNING);
        } else {
            running.setNotTriggered();
            current.setCurrent(0.0);
        }
    }

    @Override
    public MockCurrentSensor compressorCurrent() {
        return current;
    }

    @Override
    public Switch compressorRunningSwitch() {
        return running;
    }

    @Override
    public MockSwitch lowPressureSwitch() {
        return lowPressure;
    }

    @Override
    public MockRelay automaticMode() {
        return automatic;
    }

    /**
     * These faults clear immediately after they are {@link Fuse#trigger() triggered}.
     * @see #compressorStickyFaults()
     */
    @Override
    public MockFaults compressorFaults() {
        return instantaneousFaults;
    }

    @Override
    public Faults compressorStickyFaults() {
        return stickyFaults;
    }

    @Override
    public MockPneumaticsModule clearStickyFaults() {
        stickyFaults.reset();
        return this;
    }

    private void triggerFault( MockSwitch stickySwitch ) {
        // Any fault should always stop the compressor ...
        stickySwitch.setTriggered();
        runCompressor(false);
    }

    public class MockFaults implements Faults {
        private final Fuse currentTooHigh;
        private final Fuse notConnected;
        private final Fuse shorted;
        protected MockFaults( StickyFaults sticky ) {
            // These should trip the sticky faults and then immediately reset ...
            currentTooHigh = Fuse.instantaneous(()->triggerFault(sticky.currentTooHigh));
            notConnected = Fuse.instantaneous(()->triggerFault(sticky.notConnected));
            shorted = Fuse.instantaneous(()->triggerFault(sticky.shorted));
        }

        @Override
        public Fuse currentTooHigh() {
            return currentTooHigh;
        }
        @Override
        public Fuse notConnected() {
            return notConnected;
        }
        @Override
        public Fuse shorted() {
            return shorted;
        }
    }

    protected static class StickyFaults implements Faults {
        private final MockSwitch currentTooHigh = new MockSwitch().setNotTriggered();
        private final MockSwitch notConnected = new MockSwitch().setNotTriggered();
        private final MockSwitch shorted = new MockSwitch().setNotTriggered();

        @Override
        public Switch currentTooHigh() {
            return currentTooHigh;
        }
        @Override
        public Switch notConnected() {
            return notConnected;
        }
        @Override
        public Switch shorted() {
            return shorted;
        }
        protected void reset() {
            currentTooHigh.setNotTriggered();
            notConnected.setNotTriggered();
            shorted.setNotTriggered();
        }
    }

}
