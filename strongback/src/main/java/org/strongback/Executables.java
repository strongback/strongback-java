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

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.strongback.annotation.ThreadSafe;

/**
 * A simple threadsafe list of {@link Executable} instances.
 * @author Randall Hauch
 */
@ThreadSafe
final class Executables implements Executor, Iterable<Executable> {

    private final CopyOnWriteArrayList<Executable> executables = new CopyOnWriteArrayList<>();

    Executables() {
    }

    @Override
    public boolean register(Executable r) {
        return executables.addIfAbsent(r);
    }

    @Override
    public boolean unregister(Executable r) {
        return executables.remove(r);
    }

    @Override
    public void unregisterAll() {
        executables.clear();
    }

    @Override
    public Iterator<Executable> iterator() {
        return executables.iterator();
    }

}
