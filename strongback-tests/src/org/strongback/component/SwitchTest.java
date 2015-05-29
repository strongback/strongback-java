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

import org.junit.Test;
import org.strongback.components.Switch;

/**
 * @author Randall Hauch
 *
 */
public class SwitchTest {

    @Test
    public void shouldAlwaysBeTriggered() {
        Switch s = Switch.alwaysTriggered();
        for ( int i=0; i!=100; ++i) {
            assertThat(s.isTriggered()).isEqualTo(true);
        }
    }

    @Test
    public void shouldNeverBeTriggered() {
        Switch s = Switch.neverTriggered();
        for ( int i=0; i!=100; ++i) {
            assertThat(s.isTriggered()).isEqualTo(false);
        }
    }

}
