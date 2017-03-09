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

package org.strongback.component;

import static org.fest.assertions.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.strongback.components.Fuse;
import org.strongback.mock.MockClock;

/**
 * @author Randall Hauch
 *
 */
public class FuseTest {

    protected void assertTriggered(Fuse f) {
        assertThat(f.isTriggered()).isEqualTo(true);
    }

    protected void assertNotTriggered(Fuse f) {
        assertThat(f.isTriggered()).isEqualTo(false);
    }

    @Test
    public void shouldTriggerAndReset() {
        Fuse f = Fuse.create();
        assertNotTriggered(f);
        for (int i = 0; i != 10; ++i) {
            f.trigger();
            assertTriggered(f);
            f.trigger();
            assertTriggered(f);
            f.reset();
            assertNotTriggered(f);
        }
    }

    @Test
    public void shouldAutoResetWhenMoreTimeHasPastThanDelay() {
        MockClock clock = new MockClock();
        Fuse f = Fuse.autoResetting(10, TimeUnit.SECONDS, clock);
        assertNotTriggered(f);
        // advance time by more than delay, and should still not be triggered ...
        clock.incrementBySeconds(100);
        assertNotTriggered(f);
        // Trigger ...
        f.trigger();
        assertTriggered(f);
        // advance time by exactly the delay, and should still be triggered ...
        clock.incrementBySeconds(10);
        assertTriggered(f);
        // advance time beyond the delay, and it should not be triggered ...
        clock.incrementBySeconds(1);
        assertNotTriggered(f);
    }
}
