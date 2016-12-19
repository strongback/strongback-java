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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.strongback.annotation.ThreadSafe;

/**
 * A simple threadsafe list of {@link Executable} instances.
 *
 * @author Randall Hauch
 */
@ThreadSafe
final class Executables implements Executor {

    private final CopyOnWriteArrayList<Executable> highPriority = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Executable> mediumPriority = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Executable> lowPriority = new CopyOnWriteArrayList<>();

    Executables() {
    }

    @Override
    public boolean register(Executable r, Priority priority) {
        if (priority != null) {
            unregister(r);
            switch (priority) {
                case HIGH:
                    mediumPriority.remove(r);
                    lowPriority.remove(r);
                    return highPriority.addIfAbsent(r);
                case MEDIUM:
                    highPriority.remove(r);
                    lowPriority.remove(r);
                    return mediumPriority.addIfAbsent(r);
                case LOW:
                    highPriority.remove(r);
                    mediumPriority.remove(r);
                    return lowPriority.addIfAbsent(r);
            }
        }
        return false;
    }

    @Override
    public boolean unregister(Executable r) {
        if (r != null) {
            // Remove from all
            boolean removed = highPriority.remove(r);
            removed = mediumPriority.remove(r) || removed;
            removed = lowPriority.remove(r) || removed;
            return removed;
        }
        return false;
    }

    @Override
    public void unregisterAll() {
        highPriority.clear();
        mediumPriority.clear();
        lowPriority.clear();
    }

    public List<Executable> lowPriorityExecutables() {
        return lowPriority;
    }

    public List<Executable> mediumPriorityExecutables() {
        return mediumPriority;
    }

    public List<Executable> highPriorityExecutables() {
        return highPriority;
    }

    protected Executable[] lowPriorityExecutablesAsArrays() {
        return lowPriority.toArray(new Executable[0]); // will be reallocated with correct size
    }

    protected Executable[] mediumPriorityExecutablesAsArrays() {
        return mediumPriority.toArray(new Executable[0]); // will be reallocated with correct size
    }

    protected Executable[] highPriorityExecutablesAsArrays() {
        return highPriority.toArray(new Executable[0]); // will be reallocated with correct size
    }

}
