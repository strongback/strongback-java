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

package org.strongback.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Utility class for constructing custom iterators.
 *
 * @author Randall Hauch
 */
public final class Iterators {

    /**
     * Create an immutable iterator around the supplied {@link Iterable} object.
     *
     * @param iterable the iterable object
     * @return an immutable iterator, or an {@link #empty() empty} iterator if {@code iterable} is null
     * @param <T> the type of element to iterate over
     */
    public static <T> Iterator<T> immutable(Iterable<T> iterable) {
        return iterable != null ? immutable(iterable.iterator()) : empty();
    }

    /**
     * Create an immutable iterator around the supplied {@link Iterator}.
     *
     * @param iterator the existing iterator
     * @return an immutable iterator, or an {@link #empty() empty} iterator if {@code iterable} is null
     * @param <T> the type of element to iterate over
     */
    public static <T> Iterator<T> immutable(Iterator<T> iterator) {
        return iterator == null ? empty() : new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }
        };
    }

    /**
     * Create an empty iterator.
     *
     * @return the empty iterator; never null
     * @param <T> the type of element to iterate over
     */
    public static <T> Iterator<T> empty() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                throw new NoSuchElementException();
            }
        };
    }

    private Iterators() {
    }
}
