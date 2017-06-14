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


/**
 * A Strongback testing utility that can be used in unit tests
 */
public class CommandTester {
    private final CommandRunner runner;

    public CommandTester(Command command) {
        runner = CommandRunner.create(command);
    }

    /**
     * Steps through all of the state logic for its {@link Command}.
     *
     * @param timeInMillis the current system time in milliseconds
     * @return {@code true} if this {@link CommandRunner} is ready to be terminated; {@code false} otherwise
     */
    public boolean step(long timeInMillis) {
        return runner.step(timeInMillis);
    }

    /**
     * Schedules its {@link Command} to be canceled next iteration.
     */
    public void cancel() {
        runner.cancel();
    }

    public boolean isCancelled() {
        return runner.isCancelled();
    }

    @Override
    public String toString() {
        return runner.toString();
    }
}