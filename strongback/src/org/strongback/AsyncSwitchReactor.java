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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import org.strongback.annotation.Immutable;
import org.strongback.annotation.ThreadSafe;
import org.strongback.components.Switch;

/**
 * A threadsafe {@link SwitchReactor} implementation that relies upon being periodically {@link Executable#execute(long)
 * executed}. This class is carefully written to ensure that all functions are registered atomically even while
 * {@link #execute(long)} is being called.
 *
 * @author Randall Hauch
 */
@ThreadSafe
final class AsyncSwitchReactor implements Executable, SwitchReactor {

    private final ConcurrentMap<Switch, Container> listeners = new ConcurrentHashMap<>();

    @Override
    public void execute(long time) {
        listeners.forEach((swtch, container) -> container.notifyListeners(swtch.isTriggered()));
    }

    @Override
    public void onTriggered(Switch swtch, Runnable function) {
        listeners.computeIfAbsent(swtch,(s)->new Container()).addWhenTriggered(function);
    }

    @Override
    public void onUntriggered(Switch swtch, Runnable function) {
        listeners.computeIfAbsent(swtch,(s)->new Container()).addWhenUntriggered(function);
    }

    @Override
    public void whileTriggered(Switch swtch, Runnable function) {
        listeners.computeIfAbsent(swtch,(s)->new Container()).addWhileTriggered(function);
    }

    @Override
    public void whileUntriggered(Switch swtch, Runnable function) {
        listeners.computeIfAbsent(swtch,(s)->new Container()).addWhileUntriggered(function);
    }

    /**
     * A container class for all listener functions associated with a specific {@link Switch}. The class is threadsafe to allow
     * for new listener functions to be added while the existing functions are called based upon the switch's current state.
     * <p>
     * To achieve efficient and lock-free concurrent operations, each of the functions for a specific Switch state or transition
     * are maintained in a simple linked-list structure (see {@link Listener}). Each immutable Listener is created to hold one
     * function and an optional "next" listener. To add a new function, a new Listener object is created with the function and
     * the current Listener object for that state, and the Container's reference to that state's listeners is updated with the
     * new Listener object. In essence, new functions are added to the front of the linked list without using any locking.
     * <p>
     * It is not currently possible to remove functions that have been registered.
     *
     * @author Randall Hauch
     */
    @ThreadSafe
    private static final class Container {
        private boolean previouslyTriggered;
        private final AtomicReference<Listener> whenTriggered = new AtomicReference<>();
        private final AtomicReference<Listener> whenUntriggered = new AtomicReference<>();
        private final AtomicReference<Listener> whileTriggered = new AtomicReference<>();
        private final AtomicReference<Listener> whileUntriggered = new AtomicReference<>();

        public void notifyListeners(boolean nowTriggered) {
            notifyAtomicallyWhen(()->!previouslyTriggered && nowTriggered, whenTriggered);
            notifyAtomicallyWhen(()->previouslyTriggered && !nowTriggered, whenUntriggered);
            notifyAtomicallyWhen(()->previouslyTriggered && nowTriggered, whileTriggered);
            notifyAtomicallyWhen(()->!previouslyTriggered && !nowTriggered, whileUntriggered);
            previouslyTriggered = nowTriggered;
        }

        private void notifyAtomicallyWhen(BooleanSupplier criteria, AtomicReference<Listener> listenerRef ) {
            Listener listener = listenerRef.get();
            if ( listener != null && criteria.getAsBoolean() ) listener.fire();
        }

        public void addWhenTriggered(Runnable function) {
            whenTriggered.updateAndGet((existing)->new Listener(function,existing));
        }

        public void addWhenUntriggered(Runnable function) {
            whenUntriggered.updateAndGet((existing)->new Listener(function,existing));
        }

        public void addWhileTriggered(Runnable function) {
            whileTriggered.updateAndGet((existing)->new Listener(function,existing));
        }

        public void addWhileUntriggered(Runnable function) {
            whileUntriggered.updateAndGet((existing)->new Listener(function,existing));
        }
    }

    /**
     * One node in a linked list of listener functions.
     *
     * @author Randall Hauch
     */
    @Immutable
    private static final class Listener {
        private final Runnable function;
        private final Listener next;

        public Listener(Runnable function, Listener next) {
            this.function = function;
            this.next = next;
        }

        public void fire() {
            function.run();
            if (next != null) next.fire();
        }
    }
}