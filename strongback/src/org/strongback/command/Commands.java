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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

final class Commands {

    private final Queue<CommandRunner> beingExecuted = new LinkedList<>();
    private final Queue<CommandRunner> pendingAddition = new LinkedList<>();
    private final Map<Requirable, CommandRunner> inUse = new HashMap<>();

    public Commands() {
    }

    public void step(long timeInMillis) {
        int pendingLength = pendingAddition.size();
        for (int i = 0; i < pendingLength; i++)
            if (reserve(pendingAddition.peek())) {
                pendingAddition.poll();
            } else {
                // What to do if can't reserve requirement
                // As of now, don't even try again
                pendingAddition.poll();
            }

        // Run all of the commands, if one is done, don't put it back in the queue
        int initialSize = beingExecuted.size();
        for (int i = 0; i < initialSize; i++) {
            CommandRunner runner = beingExecuted.poll();
            if (runner.step(timeInMillis)) {
                remove(runner);
            } else {
                beingExecuted.offer(runner);
            }
        }
    }

    void add(CommandRunner command) {
        // Add the command runner
        pendingAddition.offer(command);
    }

    private boolean reserve(CommandRunner command) {
        Set<Requirable> requirements = command.getRequired();

        // Verify that every requirement can be obtained
        for (Requirable required : requirements) {
            CommandRunner user = inUse.get(required);
            if (user != null && !user.isInterruptible()) return false;
        }

        // Reserve the requirements
        for (Requirable required : requirements) {
            if (inUse.containsKey(required)) inUse.get(required).cancel();
            inUse.put(required, command);
        }
        beingExecuted.offer(command);
        return true;
    }

    private void remove(CommandRunner runner) {
        runner.getRequired().forEach(required->inUse.remove(required));
        runner.after(this);
    }

    boolean isEmpty() {
        return pendingAddition.isEmpty() && beingExecuted.isEmpty();
    }

    void killAll() {
        pendingAddition.clear();
        for (int i = 0; i < beingExecuted.size(); i++) {
            CommandRunner c = beingExecuted.poll();
            c.cancel();
            c.step(0);
        }

    }
}
