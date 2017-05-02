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

import org.strongback.Executable;
import org.strongback.Logger;
import org.strongback.Strongback;

/**
 * The scheduler used to execute {@link Command}s.
 *
 * @see Strongback#submit(Command)
 */
public class Scheduler implements Executable {

    private static CommandListener NO_OP = (command, state) -> {
    };

    public static interface CommandListener {
        public void record(Command command, CommandState state);

        public static CommandListener noOp() {
            return NO_OP;
        }
    }

    private final Commands commands = new Commands();
    private final CommandRunner.Context context;

    public Scheduler(Logger logger) {
        this(logger, null);
    }

    public Scheduler(Logger logger, CommandListener listener) {
        Logger log = logger != null ? logger : Logger.noOp();
        CommandListener commandListener = listener != null ? listener : CommandListener.noOp();
        this.context = CommandRunner.Context.with(commandListener, log);
    }

    /**
     * Kill all commands.
     */
    public void killAll() {
        commands.killAll();
    }

    /**
     * Schedule a {@link Command} to be added to the {@link Scheduler}.
     *
     * @param command the {@link Command} to be added
     */
    public void submit(Command command) {
        if (command != null) {
            CommandRunner runner = buildRunner(command, null);
            commands.add(runner);
        }
    }

    private CommandRunner buildRunner(Command command, CommandRunner last) {
        if (command instanceof CommandGroup) {
            CommandGroup cg = (CommandGroup) command;
            Command[] commands = cg.getCommands();
            switch (cg.getType()) {
                case SEQUENTIAL:
                    for (int i = commands.length - 1; i >= 0; i--) {
                        last = buildRunner(commands[i], last);
                    }
                    return last;
                case PARRALLEL:
                    CommandRunner[] crs = new CommandRunner[commands.length];
                    for (int i = 0; i < crs.length; i++) {
                        crs[i] = buildRunner(commands[i], null);
                    }
                    return new CommandRunner(context, last, crs);
                case FORK:
                    assert commands.length == 1;
                    return new CommandRunner(context, last, new CommandRunner(context, buildRunner(commands[0], null)));
            }
            // This line should never happen, the switch will throw an exception first
            throw new IllegalStateException("Unexpected command type: " + cg.getType());
        }
        return new CommandRunner(context, last, command);
    }

    /**
     * Steps once though all of the {@link Command}s in the {@link Scheduler}.
     *
     * @param timeInMillis the current system time in milliseconds
     */
    @Override
    public void execute(long timeInMillis) {
        commands.step(timeInMillis);
    }

    /**
     * Tests if there are any {@link Command}s currently executing or pending execution.
     *
     * @return {@code true} if there are no {@link Command}s executing or pending; {@code false} otherwise
     */
    public boolean isEmpty() {
        return commands.isEmpty();
    }
}