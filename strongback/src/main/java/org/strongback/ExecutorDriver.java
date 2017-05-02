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
import java.util.concurrent.atomic.AtomicReference;

import org.strongback.Strongback.ExcessiveExecutionHandler;
import org.strongback.annotation.ThreadSafe;
import org.strongback.components.Clock;
import org.strongback.components.Stoppable;

/**
 * An executor that invokes registered {@link Executable}s on a fixed period.
 */
@ThreadSafe
final class ExecutorDriver implements Stoppable {

    private final String name;
    private final Clock timeSystem;
    private final Logger logger;
    private final Executables executables;
    private final AtomicReference<Thread> thread = new AtomicReference<>();
    private final ExcessiveExecutionHandler delayInformer;
    private final long executionPeriodInMillis;
    private volatile boolean running = false;
    private volatile CountDownLatch stopped = null;
    private final int mediumPriorityFrequency = 2;
    private final int lowPriorityFrequency = 4;

    ExecutorDriver(String name, Executables executables, Clock timeSystem, long executionPeriodInMillis, Logger logger,
            ExcessiveExecutionHandler delayInformer) {
        this.name = name;
        this.timeSystem = timeSystem;
        this.executionPeriodInMillis = executionPeriodInMillis;
        this.logger = logger;
        this.executables = executables;
        this.delayInformer = delayInformer != null ? delayInformer : ExecutorDriver::noDelay;
    }

    /**
     * Start the execution of this {@link ExecutorDriver} in a separate thread. During each execution, all registered
     * {@link Executable}s will be called in the order they were registered.
     * <p>
     * Calling this method when already started has no effect.
     *
     * @see #stop()
     */
    public void start() {
        thread.getAndUpdate(thread -> {
            if (thread == null) {
                thread = new Thread(this::run);
                thread.setName(name);
                // run with a bit higher priority to reduce thread context switches
                thread.setPriority(8);
                stopped = new CountDownLatch(1);
                running = true;
                thread.start();
            }
            return thread;
        });
    }

    /**
     * Stop this executor from executing, and block until the thread has completed all work (or until the timeout has occurred).
     * <p>
     * Calling this method when already stopped has no effect.
     *
     * @see #start()
     */
    @Override
    public void stop() {
        // Get the latch we'll use to wait for the thread to complete
        CountDownLatch latch = stopped;
        // Atomically mark the thread as completed and change our reference to it ...
        Thread oldThread = thread.getAndUpdate(thread -> {
            if (thread != null) {
                running = false;
            }
            return null;
        });
        if (oldThread != null && latch != null) {
            // Wait (at most 10 seconds) for the thread to complete ...
            try {
                latch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

    private void run() {
        try {
            long startTimeInMillis = 0L;
            long stopTimeInMillis = 0L;
            long nextTimeInMillis = 0L;
            int loopsUntilNextMediumPriority = mediumPriorityFrequency;
            int loopsUntilNextLowPriority = lowPriorityFrequency;

            // Get read-only arrays with the various executable items ...
            final Executable[] highPriorityItems = executables.highPriorityExecutablesAsArrays();
            final Executable[] mediumPriorityItems = executables.mediumPriorityExecutablesAsArrays();
            final Executable[] lowPriorityItems = executables.lowPriorityExecutablesAsArrays();
            final int numHighPriorityItems = highPriorityItems.length;
            final int numMediumPriorityItems = mediumPriorityItems.length;
            final int numLowPriorityItems = lowPriorityItems.length;

            while (running) {
                // Start a new cycle ...
                --loopsUntilNextMediumPriority;
                --loopsUntilNextLowPriority;
                startTimeInMillis = timeSystem.currentTimeInMillis();

                // First execute the HIGH priority items ...
                for (int i=0; i!=numHighPriorityItems; ++i) {
                    Executable executable = highPriorityItems[i];
                    if (!running) return;
                    try {
                        executable.execute(timeSystem.currentTimeInMillis());
                    } catch (Throwable e) {
                        logger.error(e);
                    }
                }

                // Execute the MEDIUM priority items every other time ...
                if (loopsUntilNextMediumPriority <= 0) {
                    for (int i=0; i!=numMediumPriorityItems; ++i) {
                        Executable executable = mediumPriorityItems[i];
                        if (!running) return;
                        try {
                            executable.execute(timeSystem.currentTimeInMillis());
                        } catch (Throwable e) {
                            logger.error(e);
                        }
                    }
                    // Reset the counter ...
                    loopsUntilNextMediumPriority = mediumPriorityFrequency;
                }

                // Execute the LOW priority items every `lowPriorityFrequency` times ...
                if (loopsUntilNextLowPriority <= 0) {
                    for (int i=0; i!=numLowPriorityItems; ++i) {
                        Executable executable = lowPriorityItems[i];
                        if (!running) return;
                        try {
                            executable.execute(timeSystem.currentTimeInMillis());
                        } catch (Throwable e) {
                            logger.error(e);
                        }
                    }
                    // Reset the counter ...
                    loopsUntilNextLowPriority = lowPriorityFrequency;
                }

                // Compute the time it took to run all of these ...
                stopTimeInMillis = timeSystem.currentTimeInMillis();
                long durationInMillis = stopTimeInMillis - startTimeInMillis;
                if (durationInMillis > executionPeriodInMillis) {
                    // It took too long to run our executables ...
                    delayInformer.handle(durationInMillis, executionPeriodInMillis);
                } else {
                    // Pause until our next period begins ...
                    nextTimeInMillis = startTimeInMillis + executionPeriodInMillis;
                    while (timeSystem.currentTimeInMillis() < nextTimeInMillis) {
                        // busy loop ...
                    }
                }
            }
        } finally {
            CountDownLatch latch = stopped;
            if (latch != null) latch.countDown();
        }
    }

    private static void noDelay(long actual, long desired) {
        // do nothing
    }
}