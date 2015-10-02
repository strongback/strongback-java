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
import java.util.function.LongConsumer;

import org.strongback.annotation.ThreadSafe;
import org.strongback.components.Clock;
import org.strongback.components.Stoppable;
import org.strongback.util.Metronome;

/**
 * An executor that invokes registered {@link Executable}s on a fixed period.
 */
@ThreadSafe
final class ExecutorDriver implements Stoppable {

    private final String name;
    private final Clock timeSystem;
    private final Metronome met;
    private final Logger logger;
    private final Iterable<Executable> executables;
    private final AtomicReference<Thread> thread = new AtomicReference<>();
    private final LongConsumer delayInformer;
    private volatile boolean running = false;
    private volatile CountDownLatch stopped = null;

    ExecutorDriver(String name, Iterable<Executable> executables, Clock timeSystem, Metronome metronome, Logger logger, LongConsumer delayInformer ) {
        this.name = name;
        this.timeSystem = timeSystem;
        this.met = metronome;
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
            if (thread != null) running = false;
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
            long timeInMillis = 0L;
            long lastTimeInMillis = 0L;
            while (true) {
                timeInMillis = timeSystem.currentTimeInMillis();
                delayInformer.accept( timeInMillis - lastTimeInMillis);
                for (Executable executable : executables) {
                    if (!running) return;
                    try {
                        executable.execute(timeInMillis);
                    } catch (Throwable e) {
                        logger.error(e);
                    }
                }
                lastTimeInMillis = timeInMillis;
                met.pause();
            }
        } finally {
            CountDownLatch latch = stopped;
            if (latch != null) latch.countDown();
        }
    }

    private static void noDelay( long delay ) {
        // do nothing
    }
}