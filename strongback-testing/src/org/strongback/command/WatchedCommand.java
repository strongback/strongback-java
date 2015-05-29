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

package org.strongback.command;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.AssertionFailedError;

/**
 * @author Randall Hauch
 *
 */
public class WatchedCommand extends Command {

    public static WatchedCommand watch(Command command) {
        return new WatchedCommand(command);
    }

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicInteger executed = new AtomicInteger(0);
    private final AtomicBoolean ended = new AtomicBoolean(false);
    private final AtomicBoolean interrupted = new AtomicBoolean(false);
    private final Command delegate;

    public WatchedCommand(Command delegate) {
        super(delegate.getTimeoutInSeconds(), delegate.getRequirements());
        this.delegate = delegate;
    }

    @Override
    public void initialize() {
        try {
            delegate.initialize();
        } finally {
            if (!initialized.compareAndSet(false, true)) throw new AssertionFailedError(
                    "initialize() called more than once on the command: " + delegate);
        }
    }

    @Override
    public boolean execute() {
        try {
            return delegate.execute();
        } finally {
            executed.incrementAndGet();
        }
    }

    @Override
    public void end() {
        try {
            delegate.end();
        } finally {
            if (!ended.compareAndSet(false, true)) throw new AssertionFailedError(
                    "end() called more than once on the command: " + delegate);
        }
    }

    @Override
    public void interrupted() {
        try {
            delegate.interrupted();
        } finally {
            if (!interrupted.compareAndSet(false, true)) throw new AssertionFailedError(
                    "interrupted() called more than once on the command: " + delegate);
        }
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    public boolean isExecuted() {
        return isExecutedAtLeast(1);
    }

    public boolean isExecutedAtLeast(int minimum) {
        return executed.get() >= minimum;
    }

    public boolean isEnded() {
        return ended.get();
    }

    public boolean isInterrupted() {
        return interrupted.get();
    }

}
