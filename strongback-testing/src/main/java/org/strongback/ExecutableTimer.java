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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.strongback.Strongback.Configurator;
import org.strongback.annotation.ThreadSafe;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.UniformReservoir;

/**
 * A command that measures the {@link Strongback.Configurator#useExecutionPeriod(long, java.util.concurrent.TimeUnit) execution
 * rate} of the Strongback {@link Executor}. This is purely for test purposes.
 * <p>
 *
 * @author Randall Hauch
 */
@ThreadSafe
public final class ExecutableTimer implements Executable {

    /**
     * Measure the timing of the {@link Executor} and print the histogram of timing results to {@link System#out}. The resulting
     * timer will be {@link Executor#register(Executable) registered} with the Executor, and upon completion will automatically
     * unregister itself.
     * <p>
     * This method can be used to print a histogram of {@link Strongback#executor() Strongback's executor}. The following
     * example uses 1000 samples, which corresponds to 5 seconds if the {@link Configurator#useExecutionPeriod(long, TimeUnit)
     * execution rate} is 5 milliseconds:
     *
     * <pre>
     * ExecutableTimer.measureTimingAndPrint(Strongback.executor(), 200 * 5);
     * </pre>
     *
     * The result would be something like the following:
     *
     * @param executor the Executor to measure; may not be null
     * @param desc the description
     * @param numberOfSamples the number of samples to measure
     * @return the executable timer
     */
    public static ExecutableTimer measureTimingAndPrint(Executor executor, String desc, int numberOfSamples) {
        return measureTiming(executor, numberOfSamples, snapshot -> {
            System.out.println("Execution timing statistics" + (desc != null ? " (" + desc + ")" : ""));
            System.out.println("  Size of histogram:      " + snapshot.getValues().length);
            System.out.println("  Minimum (ms):           " + snapshot.getMin());
            System.out.println("  Maximum (ms):           " + snapshot.getMax());
            System.out.println("  Mean (ms):              " + snapshot.getMean());
            System.out.println("  Median (ms):            " + snapshot.getMedian());
            System.out.println("  Standard dev (ms):      " + snapshot.getStdDev());
            System.out.println("  75th percentile (ms):   " + snapshot.get75thPercentile());
            System.out.println("  95th percentile (ms):   " + snapshot.get95thPercentile());
            System.out.println("  98th percentile (ms):   " + snapshot.get98thPercentile());
            System.out.println("  99th percentile (ms):   " + snapshot.get99thPercentile());
            System.out.println("  99.9th percentile (ms): " + snapshot.get999thPercentile());
            //snapshot.dump(System.out);
        });
    }

    public static ExecutableTimer measureTiming(Executor executor, int numberOfSamples, Consumer<Snapshot> atCompletion) {
        ExecutableTimer timer = new ExecutableTimer(numberOfSamples, (theTimer) -> {
            executor.unregister(theTimer);
            if (atCompletion != null) atCompletion.accept(theTimer.getResults());
        });
        // Register the timer with the executor ...
        executor.register(timer);
        return timer;
    }

    private final CountDownLatch latch = new CountDownLatch(1);
    private final Consumer<ExecutableTimer> resultsHandler;
    private final Histogram histogram;
    private final int numSamples;
    private final AtomicLong lastStartTime = new AtomicLong();
    private final AtomicLong count = new AtomicLong(-1);

    /**
     * Create an executable timer that measures the specified number of samples.
     *
     * @param numberOfSamples the maximum number of samples to take; must be positive
     * @param uponCompletion the function that will be called
     */
    private ExecutableTimer(int numberOfSamples, Consumer<ExecutableTimer> uponCompletion) {
        histogram = new Histogram(new UniformReservoir(numberOfSamples));
        numSamples = numberOfSamples;
        resultsHandler = uponCompletion;
    }

    @Override
    public void execute(long timeInMillis) {
        long currentCount = count.getAndIncrement();
        if (currentCount < 0) {
            lastStartTime.set(timeInMillis);
        } else if (currentCount < numSamples) {
            histogram.update(timeInMillis - lastStartTime.getAndSet(timeInMillis));
        } else {
            count.set(-1);
            // We've completed, so first run the results handler and only then release the latch ...
            resultsHandler.accept(this);
            latch.countDown();
        }
    }

    public boolean isComplete() {
        return latch.getCount() == -1 && histogram.getCount() > 0;
    }

    public ExecutableTimer await(long timeout, TimeUnit unit) throws InterruptedException {
        latch.await(timeout, unit);
        return this;
    }

    public ExecutableTimer await() throws InterruptedException {
        latch.await();
        return this;
    }

    public Snapshot getResults() {
        return histogram.getSnapshot();
    }
}
