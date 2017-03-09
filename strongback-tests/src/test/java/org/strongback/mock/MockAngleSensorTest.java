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

import org.junit.Before;

/**
 * @author Randall Hauch
 */
public class MockAngleSensorTest extends AbstractDoubleMockTest {

    private MockAngleSensor mock;

    @Before
    public void beforeEach() {
        mock = new MockAngleSensor();
        initialize(mock::setAngle, mock::getAngle);
    }
}
