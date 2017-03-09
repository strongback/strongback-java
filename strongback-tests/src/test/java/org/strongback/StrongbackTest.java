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

package org.strongback;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.strongback.Logger.Level;
import org.strongback.components.Clock;

/**
 * Tests that check the functionality of the {@link Strongback.Engine}.
 */
public class StrongbackTest {

    private static final SystemLogger LOGGER = new SystemLogger();

    private Clock clock;
    private Strongback.Engine engine;

    @Before
    public void beforeEach() {
        LOGGER.enable(Level.INFO);
        clock = Clock.system();
        engine = new Strongback.Engine(clock, LOGGER);
    }

    @After
    public void afterEach() {
        try {
            if (engine != null && engine.isRunning()) {
                engine.stop();
            }
        } finally {
            engine = null;
        }
    }

    @Test
    public void shouldNotBeRunningWhenCreated() {
        assertThat(engine.isRunning()).isFalse();
    }

    @Test
    public void shouldStartWithDefaultConfiguration() {
        engine.logConfiguration();
        assertThat(engine.isRunning()).isFalse();
        assertThat(engine.start()).isTrue();
        assertThat(engine.isRunning()).isTrue();
    }

    @Test
    public void shouldAllowChangingExecutionPeriodWhenNotRunning() {
        assertThat(engine.isRunning()).isFalse();
        assertThat(engine.getExecutionPeriod()).isEqualTo(20);
        engine.setExecutionPeriod(5);
        assertThat(engine.getExecutionPeriod()).isEqualTo(5);
        assertThat(engine.start()).isTrue();
        engine.logConfiguration();
    }

    @Test
    public void shouldNotAllowChangingExecutionPeriodWhenRunning() {
        assertThat(engine.isRunning()).isFalse();
        assertThat(engine.start()).isTrue();
        assertThat(engine.getExecutionPeriod()).isEqualTo(20);
        LOGGER.enable(Level.OFF);
        assertThat(engine.setExecutionPeriod(5)).isFalse();
        LOGGER.enable(Level.INFO);
        assertThat(engine.getExecutionPeriod()).isEqualTo(20);
    }

}
