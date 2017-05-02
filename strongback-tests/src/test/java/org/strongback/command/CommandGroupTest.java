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

import org.junit.Before;
import org.junit.Test;
import org.strongback.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.fest.assertions.Assertions.assertThat;

public class CommandGroupTest {
    private Scheduler scheduler;
    private Queue<String> list;
    private Command[] c;
    private Command[] d;

    @Before
    public void beforeEach() {
        scheduler = new Scheduler(Logger.noOp(),null);
        list = new LinkedList<>();
        TestCommand.reset();
        c = new Command[10];
        for (int i = 0; i < c.length; i++)
            c[i] = new TestCommand(list);
        DelayCommand.reset();
        d = new Command[10];
        for (int i = 0; i < d.length; i++)
            d[i] = new DelayCommand(list, i);
    }

    @Test
    public void shouldCancelTimedOutCommand() {
        scheduler.submit(c[0]);
        scheduler.submit(c[1]);
        scheduler.submit(d[1]);

        assertThat(list).isEmpty();

        scheduler.execute(0);

        assertThat(list).isEqualTo(listOf("C0 init", "C0 exe", "C0 fin", "C1 init", "C1 exe", "C1 fin", "D1 init", "D1 exe"));
        list.clear();

        scheduler.execute(1);
        assertThat(list).isEqualTo(listOf("D1 exe" ));
        list.clear();

        scheduler.execute(2);
        assertThat(list).isEqualTo(listOf( "D1 exe" ));
        list.clear();

        scheduler.execute(1000);
        assertThat(list).isEqualTo(listOf( "D1 fin" ));
        list.clear();
    }

    @Test
    public void shoudKillCommandThatUsesRequirementOfAnother() {
        Requirable required = new Requirable() {
        };
        CommandWithRequirement r0 = new CommandWithRequirement(required, 0, list, true);
        CommandWithRequirement r1 = new CommandWithRequirement(required, 1, list, false);
        CommandWithRequirement r2 = new CommandWithRequirement(required, 2, list, true);

        scheduler.submit(r0);
        scheduler.submit(r1);
        scheduler.submit(r2);

        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("R0 inter", "R1 init", "R1 exe", "R1 end" ));
        list.clear();
    }

    @Test
    public void shoudKillCommandGroupThatUsesRequirementOfAnother() {
        Requirable required = new Requirable() {
            @Override
            public String toString() {
                return "R0";
            }
        };
        CommandWithRequirement r0 = new CommandWithRequirement(required, 0, list, true);
        CommandWithRequirement r1 = new CommandWithRequirement(required, 1, list, true);
        CommandWithRequirement r2 = new CommandWithRequirement(required, 2, list, true);
        RequiredGroup g = new RequiredGroup(r0, r1, r2);

        CommandWithRequirement r3 = new CommandWithRequirement(required, 3, list, false);
        scheduler.submit(g);

        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("R0 init", "R0 exe", "R0 end" ));
        list.clear();

        scheduler.submit(r3);
        scheduler.execute(1);
        assertThat(list).isEqualTo(listOf("R1 inter", "R3 init", "R3 exe", "R3 end" ));
        list.clear();

        scheduler.execute(2);
        assertThat(list).isEmpty();
    }

    private final class RequiredGroup extends CommandGroup {
        public RequiredGroup(CommandWithRequirement... commands) {
            sequentially(commands);
        }
    }

    @Test
    public void shouldTimeoutDiagramFromBoard() {
        scheduler.submit(new DiagramFromBoard());
        // Nothing has executed yet
        assertThat(list).isEmpty();

        // First step should execute 0
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C0 init", "C0 exe", "C0 fin" ));
        list.clear();

        // Second step is a branch, nothing was executed, fork was primed
        scheduler.execute(1);
        assertThat(list).isEmpty();

        // Third step should execute 1 and 2 (parallelized) and 3 (just forked)
        scheduler.execute(2);
        assertThat(list).isEqualTo(listOf("C1 init", "C1 exe", "C1 fin", "C2 init", "C2 exe", "C2 fin", "C3 init", "C3 exe",
                        "C3 fin" ));
        list.clear();

        // Fourth step should execute 5 (main) and 4 (on fork)
        scheduler.execute(3);
        assertThat(list)
                .isEqualTo(listOf("C5 init", "C5 exe", "C5 fin", "C4 init", "C4 exe", "C4 fin" ));
        list.clear();

        // Fifth step is a branch, nothing was executed fork is dead, another fork was primed
        scheduler.execute(4);
        assertThat(list).isEmpty();

        // Sixth step should execute 6 (main) and 7 (just forked)
        scheduler.execute(5);
        assertThat(list)
                .isEqualTo(listOf("C6 init", "C6 exe", "C6 fin", "C7 init", "C7 exe", "C7 fin" ));
        list.clear();

        // Seventh step scheduler is empty, nothing executes
        scheduler.execute(6);
        assertThat(list).isEmpty();
    }

    @Test
    public void shouldExecuteCommandsInOrder() {
        Command c = new SeqCommandGroup();
        scheduler.submit(c);
        assertThat(list).isEmpty();
        // First step, C0 should have been added, run, and finished
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C0 init", "C0 exe", "C0 fin" ));

        // Second step, C1 should have been added, run, and finished
        scheduler.execute(0);
        assertThat(list)
                .isEqualTo(listOf("C0 init", "C0 exe", "C0 fin", "C1 init", "C1 exe", "C1 fin" ));

        // Third step, C2 should have been added, run, and finished
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C0 init", "C0 exe", "C0 fin", "C1 init", "C1 exe", "C1 fin", "C2 init", "C2 exe",
                        "C2 fin" ));
    }

    private final class SeqCommandGroup extends CommandGroup {
        @SuppressWarnings("synthetic-access")
        public SeqCommandGroup() {
            sequentially(c[0], c[1], c[2]);
        }
    }

    @Test
    public void shouldExecuteCommandsTogether() {
        Command c = new SimulCommandGroup();
        scheduler.submit(c);
        scheduler.execute(0);
        // After one step, all three should have run
        assertThat(list).isEqualTo(listOf("C0 init", "C0 exe", "C0 fin", "C1 init", "C1 exe", "C1 fin", "C2 init", "C2 exe",
                        "C2 fin" ));
    }

    private final class SimulCommandGroup extends CommandGroup {
        @SuppressWarnings("synthetic-access")
        public SimulCommandGroup() {
            simultaneously(c[0], c[1], c[2]);
        }
    }

    @Test
    public void shouldExecuteTwoCommandsTogetherAndOneAfter() {
        Command c = new TwoOneGroup();
        scheduler.submit(c);
        // After one step, first two should have run
        scheduler.execute(0);
        assertThat(list)
                .isEqualTo(listOf("C0 init", "C0 exe", "C0 fin", "C1 init", "C1 exe", "C1 fin" ));
        list.clear();

        // After two steps, all three should have run
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C2 init", "C2 exe", "C2 fin" ));
    }

    private final class TwoOneGroup extends CommandGroup {
        @SuppressWarnings("synthetic-access")
        public TwoOneGroup() {
            sequentially(simultaneously(c[0], c[1]), c[2]);
        }
    }

    @Test
    public void shouldModelDiagramFromBoard() {
        scheduler.submit(new DiagramFromBoard());
        // Nothing has executed yet
        assertThat(list).isEmpty();

        // First step should execute 0
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C0 init", "C0 exe", "C0 fin" ));
        list.clear();

        // Second step is a branch, nothing was executed, fork was primed
        scheduler.execute(0);
        assertThat(list).isEmpty();

        // Third step should execute 1 and 2 (parallelized) and 3 (just forked)
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C1 init", "C1 exe", "C1 fin", "C2 init", "C2 exe", "C2 fin", "C3 init", "C3 exe",
                        "C3 fin" ));
        list.clear();

        // Fourth step should execute 5 (main) and 4 (on fork)
        scheduler.execute(0);
        assertThat(list)
                .isEqualTo(listOf("C5 init", "C5 exe", "C5 fin", "C4 init", "C4 exe", "C4 fin" ));
        list.clear();

        // Fifth step is a branch, nothing was executed fork is dead, another fork was primed
        scheduler.execute(0);
        assertThat(list).isEmpty();

        // Sixth step should execute 6 (main) and 7 (just forked)
        scheduler.execute(0);
        assertThat(list)
                .isEqualTo(listOf("C6 init", "C6 exe", "C6 fin", "C7 init", "C7 exe", "C7 fin" ));
        list.clear();

        // Seventh step scheduler is empty, nothing executes
        scheduler.execute(0);
        assertThat(list).isEmpty();
    }

    private final class DiagramFromBoard extends CommandGroup {
        @SuppressWarnings("synthetic-access")
        public DiagramFromBoard() {
            sequentially(c[0], fork(sequentially(c[3], c[4])), simultaneously(c[1], c[2]), c[5], fork(c[7]), c[6]);
        }
    }

    @Test
    public void shouldModelOtherDiagramFromBoard() {
        scheduler.submit(new OtherDiagramFromBoard());

        // Nothing has executed yet
        assertThat(list).isEmpty();

        // First step should execute 0
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C0 init", "C0 exe", "C0 fin" ));
        list.clear();

        // Second step is a branch, nothing executes, fork is primed
        scheduler.execute(0);
        assertThat(list).isEmpty();

        // Third step should execute 3 (main) and 1 and 2 (just forked parallelized)
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C3 init", "C3 exe", "C3 fin", "C1 init", "C1 exe", "C1 fin", "C2 init", "C2 exe",
                        "C2 fin" ));
        list.clear();

        // Fourth step should execute 4 and 5 (parallelized) fork is dead
        scheduler.execute(0);
        assertThat(list)
                .isEqualTo(listOf("C4 init", "C4 exe", "C4 fin", "C5 init", "C5 exe", "C5 fin" ));
        list.clear();

        // Fifth step should execute 6
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C6 init", "C6 exe", "C6 fin" ));
        list.clear();

        // Sixth step scheduler is empty, nothing executed
        scheduler.execute(0);
        assertThat(list).isEmpty();

    }

    private <T> List<T> listOf(@SuppressWarnings("unchecked") T... values) {
        return Arrays.asList(values);
    }

    private final class OtherDiagramFromBoard extends CommandGroup {
        @SuppressWarnings("synthetic-access")
        public OtherDiagramFromBoard() {
            sequentially(c[0], fork(simultaneously(c[1], c[2])), c[3], simultaneously(c[4], c[5]), c[6]);
        }
    }

    @Test
    public void shouldExecuteComposedNamedCommandGroups() {
        scheduler.submit(new OuterCommandGroup());

        assertThat(list).isEmpty();

        // First step should come from the OuterCommandGroup
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C0 init", "C0 exe", "C0 fin" ));
        list.clear();

        // Second step should come from NestedCommandGroup
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C1 init", "C1 exe", "C1 fin" ));
        list.clear();

        // Third step should come from NestedCommandGroup
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C2 init", "C2 exe", "C2 fin" ));
        list.clear();

        // Fourth step should resume the OuterCommandGroup sequence
        scheduler.execute(0);
        assertThat(list).isEqualTo(listOf("C3 init", "C3 exe", "C3 fin" ));
        list.clear();

        // Fifth step is empty, nothing executed
        scheduler.execute(0);
        assertThat(list).isEmpty();
    }

    private final class OuterCommandGroup extends CommandGroup {
        @SuppressWarnings("synthetic-access")
        public OuterCommandGroup() {
            sequentially(c[0], new InnerNamedCommandGroup(), c[3]);
        }

        private final class InnerNamedCommandGroup extends CommandGroup {
            @SuppressWarnings("synthetic-access")
            public InnerNamedCommandGroup() {
                sequentially(c[1], c[2]);
            }
        }
    }

    private static final class TestCommand extends Command {
        private static int commandID = 0;

        public static void reset() {
            commandID = 0;
        }

        private final Queue<String> list;
        private final int id;

        /**
         * @param list the list to log to
         */
        public TestCommand(Queue<String> list) {
            this.list = list;
            id = commandID;
            commandID++;
        }

        @Override
        public void initialize() {
            list.offer("C" + id + " init");
        }

        @Override
        public boolean execute() {
            list.offer("C" + id + " exe");
            return true;
        }

        @Override
        public void end() {
            list.offer("C" + id + " fin");
        }

        @Override
        public String toString() {
            return "C" + id;
        }

    }

    private static final class DelayCommand extends Command {
        private static int commandID = 0;

        public static void reset() {
            commandID = 0;
        }

        private final Queue<String> list;
        private final int id;

        /**
         * @param list the list to log to
         * @param length how long to delay
         */
        public DelayCommand(Queue<String> list, double length) {
            super(length);
            this.list = list;
            id = commandID;
            commandID++;
        }

        @Override
        public void initialize() {
            list.offer("D" + id + " init");
        }

        @Override
        public boolean execute() {
            list.offer("D" + id + " exe");
            return false;
        }

        @Override
        public void end() {
            list.offer("D" + id + " fin");
        }

        @Override
        public String toString() {
            return "D" + id;
        }
    }

    private final class CommandWithRequirement extends Command {
        private final Queue<String> list;
        private final int number;

        public CommandWithRequirement(Requirable required, int number, Queue<String> list, boolean interruptible) {
            super(required);
            this.list = list;
            this.number = number;
            if (!interruptible) setNotInterruptible();
        }

        @Override
        public void initialize() {
            list.offer("R" + number + " init");
        }

        @Override
        public boolean execute() {
            list.offer("R" + number + " exe");
            return true;
        }

        @Override
        public void interrupted() {
            list.offer("R" + number + " inter");
        }

        @Override
        public void end() {
            list.offer("R" + number + " end");
        }

        @Override
        public String toString() {
            return "R" + number;
        }
    }
}
