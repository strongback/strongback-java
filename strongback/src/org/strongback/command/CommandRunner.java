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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.strongback.Logger;
import org.strongback.Strongback;
import org.strongback.command.Scheduler.CommandListener;

/**
 * Manages all of the executable state information for a {@link Command}.
 */
final class CommandRunner {

    static interface Context {
        Logger logger();

        CommandListener listener();

        static Context with(CommandListener listener, Logger logger) {
            return new Context() {
                @Override
                public CommandListener listener() {
                    return listener;
                }

                @Override
                public Logger logger() {
                    return logger;
                }
            };
        }
    }

    private static final CommandListener NO_OP_LISTENER = CommandListener.noOp();
    private static final Context DEFAULT_CONTEXT = Context.with(NO_OP_LISTENER, Strongback.logger());

    private boolean timed = false;
    private long timeoutInMillis;
    private long endTime;
    private volatile boolean cancelled = false;
    private final Command command;
    private CommandRunner[] children = null;
    private CommandRunner next;
    private CommandState state = CommandState.UNINITIALIZED;
    private final Context context;

    static CommandRunner create(Command command) {
        return create(DEFAULT_CONTEXT, command);
    }

    static CommandRunner create(Context context, Command command) {
        return buildRunner(context, command, null);
    }

    private static CommandRunner buildRunner(Context context, Command command, CommandRunner last) {
        if (command instanceof CommandGroup) {
            CommandGroup cg = (CommandGroup) command;
            Command[] commands = cg.getCommands();
            switch (cg.getType()) {
                case SEQUENTIAL:
                    for (int i = commands.length - 1; i >= 0; i--) {
                        last = buildRunner(context, commands[i], last);
                    }
                    return last;
                case PARRALLEL:
                    CommandRunner[] crs = new CommandRunner[commands.length];
                    for (int i = 0; i < crs.length; i++) {
                        crs[i] = buildRunner(context, commands[i], null);
                    }
                    return new CommandRunner(context, last, crs);
                case FORK:
                    assert commands.length == 1;
                    return new CommandRunner(context, last, new CommandRunner(context, buildRunner(context, commands[0], null)));
            }
            // This line should never happen, the switch will throw an exception first
            throw new IllegalStateException("Unexpected command type: " + cg.getType());
        }
        return new CommandRunner(context, last, command);
    }

    private CommandRunner(Context context, CommandRunner next, Command command) {
        // A command and a next is a node
        this.command = command;
        this.next = next;
        this.timeoutInMillis = (long) (command.getTimeoutInSeconds() * 1000);
        this.context = context != null ? context : DEFAULT_CONTEXT;
    }

    private CommandRunner(Context context, CommandRunner next, CommandRunner... commands) {
        // A next and several children is a branch
        if (commands.length != 0) this.children = commands;
        this.next = next;
        this.command = null;
        this.context = context != null ? context : DEFAULT_CONTEXT;
    }

    /**
     * Steps through all of the state logic for its {@link Command}.
     *
     * @param timeInMillis the current system time in milliseconds
     * @return {@code true} if this {@link CommandRunner} is ready to be terminated; {@code false} otherwise
     */
    boolean step(long timeInMillis) {
        if (cancelled) {
            state = CommandState.INTERUPTED;
        }
        // if we have a timeout
        if (timeoutInMillis != 0) {
            endTime = timeInMillis + timeoutInMillis;
            timed = true;
            timeoutInMillis = 0;
        }
        if (timed && timeInMillis >= endTime) {
            state = CommandState.FINISHED;
        }

        // If we don't have children or a command, we are a fork and must be done
        if (children == null && command == null) return true;

        // If we have children, but no command, we are a branch
        if (children != null && command == null) {
            assert command == null;
            // We are done as long as none of our children are not
            boolean childrenDone = true;
            for (CommandRunner command : children) {
                if (!command.step(timeInMillis)) childrenDone = false;
            }
            return childrenDone;
        }

        // If we have a command, but no children, manage our command

        // If we are uninitialized initialize us
        if (state == CommandState.UNINITIALIZED) {
            try {
                listener().record(command, state);
                command.initialize();
                state = CommandState.RUNNING;
            } catch (Throwable t) {
                logger().error(t, "Error while initializing " + command.getClass().getName() + " command: " + command);
                state = CommandState.INTERUPTED;
            }
        }

        // If we should be running
        if (state == CommandState.RUNNING) {
            try {
                listener().record(command, state);
                if (command.execute()) state = CommandState.FINISHED;
            } catch (Throwable t) {
                logger().error(t, "Error while executing " + command.getClass().getName() + " command: " + command);
                state = CommandState.INTERUPTED;
            }
        }

        // If we were interrupted
        if (state == CommandState.INTERUPTED) {
            try {
                listener().record(command, state);
                command.interrupted();
            } catch (Throwable t) {
                logger().error(t, "Error while interrupting " + command.getClass().getName() + " command: " + command);
            }
            state = CommandState.FINALIZED;
        }

        // If we are pending finalization
        if (state == CommandState.FINISHED) {
            listener().record(command, state);
            try {
                command.end();
            } catch (Throwable t) {
                logger().error(t, "Error while ending " + command.getClass().getName() + " command: " + command);
            }
            state = CommandState.FINALIZED;
            listener().record(command, state);
        }

        return state == CommandState.FINALIZED;
    }

    private Logger logger() {
        return context.logger();
    }

    private CommandListener listener() {
        return context.listener();
    }

    void after(Commands commandList) {
        // Add our own next (if we have one) and the next of our children (if we have them)
        if (next != null) {
            commandList.add(next);
        }
        if (children != null) {
            for (CommandRunner command : children)
                command.after(commandList);
        }
    }

    /**
     * Schedules its {@link Command} to be canceled next iteration.
     */
    public void cancel() {
        // We want to change the state to INTERRUPTED, but for thread-safety we can't actually change the `state` field here
        // since it is actively used within the `step(...)` method. So instead, we'll set the `cancelled` flag (this is the
        // only place this is done) ...
        cancelled = true;
        if (children != null) {
            for (CommandRunner runner : children) {
                runner.cancel();
            }
        }
        if (next != null) next = null;
    }

    @Override
    public String toString() {
        if (command != null) {
            return next == null ? command.toString() : command.toString() + " -> " + next;
        }

        if (children != null) {
            return next == null ? Arrays.toString(children) : Arrays.toString(children) + " -> " + next;
        }

        return "FORK<" + next.toString() + ">";
    }

    CommandState state() {
        return state;
    }

    boolean isCancelled() {
        return cancelled;
    }

    public boolean isInterruptible() {
        if (command != null) {
            return command.isInterruptible();
        } else if (children != null) {
            for (CommandRunner runner : children) {
                if (!runner.isInterruptible()) return false;
            }
        }
        return true;
    }

    public Set<Requirable> getRequired() {
        Set<Requirable> required = new HashSet<>();
        if (command != null) {
            required.addAll(command.getRequirements());
        } else if (children != null) {
            for (CommandRunner runner : children) {
                required.addAll(runner.getRequired());
            }
        }
        return required;
    }
}