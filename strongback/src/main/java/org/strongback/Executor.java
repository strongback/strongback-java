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

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.strongback.annotation.ThreadSafe;
import org.strongback.components.Clock;
import org.strongback.components.Stoppable;
import org.strongback.util.Metronome;

/**
 * An executor that invokes registered {@link Executable}s on a fixed period.
 */
@ThreadSafe
public final class Executor implements Stoppable {

    private final String name;
    private final Clock timeSystem;
    private final Metronome met;
    private final Logger logger;
    private final CopyOnWriteArrayList<Executable> executables = new CopyOnWriteArrayList<>();
    private final AtomicReference<Thread> thread = new AtomicReference<>();
    private volatile boolean running = false;

    Executor(String name, Clock timeSystem, Metronome metronome, Logger logger) {
        this.name = name;
        this.timeSystem = timeSystem;
        this.met = metronome;
        this.logger = logger;
    }

    /**
     * Register an {@link Executable} task to be called repeatedly. This can be called before or after this Executor has been
     * {@link #start() started} or if this Executor has been {@link #stop() stopped}.
     *
     * @param r the executable task
     * @return true if the executable task was registered, or false if it was null or was already registered with this executor
     */
    public boolean register(Executable r) {
        return r != null ? executables.addIfAbsent(r) : false;
    }

    /**
     * Unregister an {@link Executable} task to no longer be called. This can be called before or after this Executor has been
     * {@link #start() started} or if this Executor has been {@link #stop() stopped}.
     *
     * @param r the executable task
     * @return true if the executable task was unregistered, or false if it was null or not registered with this executor
     */
    public boolean unregister(Executable r) {
        return r != null ? executables.remove(r) : false;
    }

    /**
     * Unregister all {@link Executable} tasks. This can be called before or after this Executor has been
     * {@link #start() started} or if this Executor has been {@link #stop() stopped}.
     */
    public void unregisterAll() {
        executables.clear();
    }

    /**
     * Start the execution of this {@link Executor} in a separate thread. During each execution, all
     * {@link #register(Executable) registered} {@link Executable}s will be called in the order they were registered.
     * <p>
     * Calling this method when already started has no effect.
     *
     * @see #stop()
     */
    public void start() {
        thread.getAndUpdate(thread -> {
            if (thread == null) {
                thread = new Thread(this::update);
                thread.setName(name);
                running = true;
                thread.start();
            }
            return thread;
        });
    }

    /**
     * Stop this executor from executing.
     * <p>
     * Calling this method when already stopped has no effect.
     *
     * @see #start()
     */
    @Override
    public void stop() {
        thread.getAndUpdate(thread -> {
            if (thread != null) {
                running = false;
            }
            return null;
        });
    }

    private void update() {
        long timeInMillis = 0L;
        while (running) {
            timeInMillis = timeSystem.currentTimeInMillis();
            for (Executable executable : executables) {
                try {
                    executable.execute(timeInMillis);
                } catch (Throwable e) {
                    logger.error(e);
                }
            }
            met.pause();
        }
    }

}