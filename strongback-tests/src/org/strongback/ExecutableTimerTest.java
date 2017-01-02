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

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.strongback.Logger.Level;

import junit.framework.AssertionFailedError;

/**
 * This test attempts to measure the timing of the {@link Strongback#executor() Strongback Executor} instance at various periods
 * <p>
 * The code committed into Git are small execution rates and sample sizes to minimize the time required to run the tests. As
 * such, the results are probably not terribly meaningful. To obtain more meaningful results on your own platform, simply adjust
 * the execution rates and sample sizes and run locally (though do not commit these changes).
 *
 * @author Randall Hauch
 */
public class ExecutableTimerTest {

    @BeforeClass
    public static void beforeAll() {
        Strongback.configure().setLogLevel(Level.ERROR).recordNoData().recordNoEvents();
    }

    @AfterClass
    public static void afterAll() {
        Strongback.stop();
    }

    @Test
    public void shouldMeasureAndPrintTimingHistogramFor4MillisecondPeriod() throws InterruptedException {
        runTimer(4, 2000);
    }

    @Test
    public void shouldMeasureAndPrintTimingHistogramFor10MillisecondPeriod() throws InterruptedException {
        runTimer(10, 2000);
    }

    /**
     * Time for the given duration the execution using the supplied mode and execution period.
     *
     * @param mode the execution wait mode; may not be null
     * @param millisecondExecutionPeriod the execution period in milliseconds
     * @param sampleTimeInMilliseconds the sample time in milliseconds
     */
    protected void runTimer(int millisecondExecutionPeriod, int sampleTimeInMilliseconds) {
        try {
            Strongback.stop();
            Strongback.configure()
                      .useExecutionPeriod(millisecondExecutionPeriod, TimeUnit.MILLISECONDS);
            Strongback.start();
            assertThat(ExecutableTimer.measureTimingAndPrint(Strongback.executor(),
                                                             " for " + millisecondExecutionPeriod + " ms",
                                                             sampleTimeInMilliseconds / millisecondExecutionPeriod)
                                      .await(4, TimeUnit.SECONDS)
                                      .isComplete());
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new AssertionFailedError();
        }
    }
}
