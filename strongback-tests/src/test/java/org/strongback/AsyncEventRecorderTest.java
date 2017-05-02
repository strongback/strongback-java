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

import org.junit.Before;
import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;

public class AsyncEventRecorderTest {

    protected static final String EVENT_TYPE_1 = "event_type_1";
    protected static final String EVENT_TYPE_2 = "event_type_2";
    protected static final String EVENT_TYPE_3 = "event_type_3";

    private AsyncEventRecorder recorder;
    private AccumulatingEventWriter writer;
    private MockClock clock;

    @Before
    public void beforeEach() {
        clock = Mock.clock();
        writer = new AccumulatingEventWriter();
        recorder = new AsyncEventRecorder(writer, clock);
    }

    @Test
    public void shouldProperlyPassEventsThroughQueuedWriter() {
        recorder.record(EVENT_TYPE_1,true);
        recorder.record(EVENT_TYPE_1,false);
        recorder.record(EVENT_TYPE_2,false);
        recorder.record(EVENT_TYPE_2,true);
        recorder.record(EVENT_TYPE_2,false);
        // Verify we haven't written anything yet
        writer.assertEmpty();
        // Now execute the recorder (which will write out everything its recorder so far) ...
        recorder.execute(clock.currentTimeInMillis());
        // Verify that events have been written ...
        writer.assertMatch(clock.currentTimeInMillis(), EVENT_TYPE_1, true);
        writer.assertMatch(clock.currentTimeInMillis(), EVENT_TYPE_1, false);
        writer.assertMatch(clock.currentTimeInMillis(), EVENT_TYPE_2, false);
        writer.assertMatch(clock.currentTimeInMillis(), EVENT_TYPE_2, true);
        writer.assertMatch(clock.currentTimeInMillis(), EVENT_TYPE_2, false);
        writer.assertEmpty();
        // Increment the clock and execute the recorder again (which has nothing to write) ...
        clock.incrementBySeconds(1);
        recorder.execute(clock.currentTimeInMillis());
        writer.assertEmpty();
        // Write a few more events and execute the recorder ...
        recorder.record(EVENT_TYPE_3,10);
        recorder.execute(clock.currentTimeInMillis());
        // Verify that the record was written ...
        writer.assertMatch(clock.currentTimeInMillis(), EVENT_TYPE_3, 10);
        writer.assertEmpty();
    }

}
