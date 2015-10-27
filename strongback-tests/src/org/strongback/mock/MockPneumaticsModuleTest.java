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

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class MockPneumaticsModuleTest {

    private MockPneumaticsModule module;

    @Before
    public void beforeEach() {
        this.module = new MockPneumaticsModule();
    }

    @Test
    public void shouldNotBeAutomaticModeByDefault() {
        assertThat(module.automaticMode().isOn()).isTrue();
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(false);
    }

    @Test
    public void shouldNotRunCompressorAutomaticallyWhenSetToAutomaticModeAndLowPressureIsNotTriggered() {
        module.lowPressureSwitch().setNotTriggered();   // start out with enough pressure
        module.automaticMode().on();
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(false);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(0.0);

        // drop pressure should run compressor ...
        module.lowPressureSwitch().setTriggered();
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(true);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(MockPneumaticsModule.COMPRESSOR_CURRENT_WHEN_RUNNING);

        // raise pressure should turn off compressor ...
        module.lowPressureSwitch().setNotTriggered();
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(false);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(0.0);

        // drop pressure should run compressor ...
        module.lowPressureSwitch().setTriggered();
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(true);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(MockPneumaticsModule.COMPRESSOR_CURRENT_WHEN_RUNNING);

        // Turn off auto mode when running ...
        module.automaticMode().off();
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(false);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(0.0);
    }

    @Test
    public void shouldRunCompressorAutomaticallyWhenSetToAutomaticModeAndLowPressureIsTriggered() {
        module.lowPressureSwitch().setTriggered();   // start out with low pressure
        module.automaticMode().on();
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(true);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(MockPneumaticsModule.COMPRESSOR_CURRENT_WHEN_RUNNING);

        // raise pressure should turn off compressor ...
        module.lowPressureSwitch().setNotTriggered();
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(false);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(0.0);

        // drop pressure should run compressor ...
        module.lowPressureSwitch().setTriggered();
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(true);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(MockPneumaticsModule.COMPRESSOR_CURRENT_WHEN_RUNNING);

        // raise pressure should turn off compressor ...
        module.lowPressureSwitch().setNotTriggered();
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(false);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(0.0);

        // Turn off auto mode when not running ...
        module.automaticMode().off();
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(false);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(0.0);
    }

    @Test
    public void shouldKeepStickyFaultStates() {
        assertThat(module.compressorFaults().currentTooHigh().isTriggered()).isEqualTo(false);
        assertThat(module.compressorFaults().notConnected().isTriggered()).isEqualTo(false);
        assertThat(module.compressorFaults().shorted().isTriggered()).isEqualTo(false);
        assertThat(module.compressorStickyFaults().currentTooHigh().isTriggered()).isEqualTo(false);
        assertThat(module.compressorStickyFaults().notConnected().isTriggered()).isEqualTo(false);
        assertThat(module.compressorStickyFaults().shorted().isTriggered()).isEqualTo(false);

        module.compressorFaults().currentTooHigh().trigger();
        assertThat(module.compressorFaults().currentTooHigh().isTriggered()).isEqualTo(false);
        assertThat(module.compressorStickyFaults().currentTooHigh().isTriggered()).isEqualTo(true);

        // Start the compressor ...
        module.lowPressureSwitch().setTriggered();   // start out with low pressure
        module.automaticMode().on();

        module.compressorFaults().notConnected().trigger();
        assertThat(module.compressorFaults().notConnected().isTriggered()).isEqualTo(false);
        assertThat(module.compressorStickyFaults().notConnected().isTriggered()).isEqualTo(true);

        // Compressor should stop after a fault
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(false);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(0.0);

        // But we can start it again ...
        module.lowPressureSwitch().setTriggered();
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(true);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(MockPneumaticsModule.COMPRESSOR_CURRENT_WHEN_RUNNING);

        module.compressorFaults().shorted().trigger();
        assertThat(module.compressorFaults().shorted().isTriggered()).isEqualTo(false);
        assertThat(module.compressorStickyFaults().shorted().isTriggered()).isEqualTo(true);

        // Compressor should stop after a fault
        assertThat(module.compressorRunningSwitch().isTriggered()).isEqualTo(false);
        assertThat(module.compressorCurrent().getCurrent()).isEqualTo(0.0);

        module.clearStickyFaults();

        assertThat(module.compressorFaults().currentTooHigh().isTriggered()).isEqualTo(false);
        assertThat(module.compressorFaults().notConnected().isTriggered()).isEqualTo(false);
        assertThat(module.compressorFaults().shorted().isTriggered()).isEqualTo(false);
        assertThat(module.compressorStickyFaults().currentTooHigh().isTriggered()).isEqualTo(false);
        assertThat(module.compressorStickyFaults().notConnected().isTriggered()).isEqualTo(false);
        assertThat(module.compressorStickyFaults().shorted().isTriggered()).isEqualTo(false);
    }

}
