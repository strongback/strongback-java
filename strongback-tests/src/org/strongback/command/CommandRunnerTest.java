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

import static org.fest.assertions.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.strongback.Logger;
import org.strongback.command.Scheduler.CommandListener;

/**
 * @author Randall Hauch
 */
public class CommandRunnerTest {

    private static final long INITIAL_TIME = 5001;
    private static final CommandRunner.Context CONTEXT = new CommandRunner.Context() {
        @Override
        public CommandListener listener() {
            return CommandListener.noOp();
        }
        @Override
        public Logger logger() {
            return Logger.noOp();
        }
    };

    @Test
    public void shouldRunCommandWithTimeout() {
        WatchedCommand watched = WatchedCommand.watch(Command.pause(1000,TimeUnit.MILLISECONDS));
        CommandRunner runner = CommandRunner.create(watched);
        assertThat(runner.step(INITIAL_TIME)).isFalse();
        assertThat(runner.state()).isEqualTo(CommandState.RUNNING);
        assertIncomplete(watched);
        assertThat(runner.step(INITIAL_TIME + 999)).isFalse();
        assertThat(runner.state()).isEqualTo(CommandState.RUNNING);
        assertIncomplete(watched);
        assertThat(runner.step(INITIAL_TIME + 1000)).isTrue();
        assertThat(runner.state()).isEqualTo(CommandState.FINALIZED);
        assertComplete(watched);
    }

    @Test
    public void shouldInterruptCommandThatThrowsExceptionDuringInitialize() {
        WatchedCommand watched = WatchedCommand.watch(new Command() {
            @Override
            public void initialize() {
                throw new IllegalStateException();
            }
            @Override
            public boolean execute() {
                return false;
            }
        });
        CommandRunner runner = CommandRunner.create(CONTEXT,watched);
        assertThat(runner.step(INITIAL_TIME)).isTrue(); // completes because it is interrupted
        assertInterrupted(watched);
    }

    @Test
    public void shouldInterruptCommandThatThrowsExceptionDuringFirstExecute() {
        WatchedCommand watched = WatchedCommand.watch(Command.create((Runnable)()->{throw new IllegalStateException();}));
        CommandRunner runner = CommandRunner.create(CONTEXT,watched);
        assertThat(runner.step(INITIAL_TIME)).isTrue(); // completes because it is interrupted
        assertExecutedAtLeast(watched,1);
        assertInterrupted(watched);
    }

    @Test
    public void shouldInterruptCommandThatThrowsExceptionDuringSecondExecute() {
        WatchedCommand watched = WatchedCommand.watch(new Command() {
            private boolean once = false;
            @Override
            public boolean execute() {
                if ( once ) throw new IllegalStateException();
                once = true;
                return false;
            }
        });
        CommandRunner runner = CommandRunner.create(CONTEXT,watched);
        assertThat(runner.step(INITIAL_TIME)).isFalse(); // executed correctly the first time
        assertExecutedAtLeast(watched,1);
        assertThat(runner.step(INITIAL_TIME)).isTrue(); // completes because it is interrupted
        assertExecutedAtLeast(watched,2);
        assertInterrupted(watched);
    }

    @Test
    public void shouldSupportCommandGroups() {
        Command command = Command.pause(100, TimeUnit.MILLISECONDS);
        WatchedCommand watched = WatchedCommand.watch(command); // CommandGroups don't call end(), so watch Command
        CommandRunner tester = CommandRunner.create(CONTEXT, CommandGroup.runSequentially(watched));

        tester.step(1);
        assertIncomplete(watched);

        tester.step(101);
        assertComplete(watched);
    }

    protected void assertIncomplete( WatchedCommand watched ) {
        assertThat(watched.isInitialized()).isTrue();
        assertThat(watched.isExecuted()).isTrue();
        assertThat(watched.isEnded()).isFalse();
        assertThat(watched.isInterrupted()).isFalse();
    }

    protected void assertComplete( WatchedCommand watched ) {
        assertThat(watched.isInitialized()).isTrue();
        assertThat(watched.isExecuted()).isTrue();
        assertThat(watched.isEnded()).isTrue();
        assertThat(watched.isInterrupted()).isFalse();
    }

    protected void assertInterrupted( WatchedCommand watched ) {
        assertThat(watched.isInterrupted()).isTrue();
        assertThat(watched.isEnded()).isFalse();
    }

    protected void assertExecutedAtLeast( WatchedCommand watched, int minimum ) {
        assertThat(watched.isInitialized()).isTrue();
        assertThat(watched.isExecutedAtLeast(minimum)).isTrue();
    }

}
