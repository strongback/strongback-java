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
import org.strongback.components.Counter;

/**
 * @author Randall Hauch
 *
 */
public class CounterTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowMaximumThatEqualsInitial() {
        Counter.circular(100, 100, 100);
    }

    @Test
    public void shouldCirculateAfterReachingMaximum() {
        Counter counter = Counter.circular(0, 100, 200);
        assertThat(counter.get()).isEqualTo(0);
        counter.increment();
        assertThat(counter.get()).isEqualTo(100);
        counter.increment();
        assertThat(counter.get()).isEqualTo(200);
        counter.increment();
        assertThat(counter.get()).isEqualTo(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNegativeIncrement() {
        Counter.circular(100, -100, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowNegativeInitialValue() {
        Counter.circular(0,-1,100);
    }

    @Test
    public void shouldCirculateAfterReachingMaximumWithDefaultIncrement() {
        Counter counter = Counter.circular(2);
        assertThat(counter.get()).isEqualTo(0);
        counter.increment();
        assertThat(counter.get()).isEqualTo(1);
        counter.increment();
        assertThat(counter.get()).isEqualTo(2);
        counter.increment();
        assertThat(counter.get()).isEqualTo(0);
    }

    @Test
    public void shouldZeroValue() {
        Counter counter = Counter.circular(200);
        assertThat(counter.get()).isEqualTo(0);
        counter.increment();
        assertThat(counter.get()).isEqualTo(1);
        counter.zero();
        assertThat(counter.get()).isEqualTo(0);
        counter.increment();
        assertThat(counter.get()).isEqualTo(1);
    }
}
