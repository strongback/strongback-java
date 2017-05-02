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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility methods for constructing various kinds of collections.
 *
 * @author Randall Hauch
 */
public final class Collections {

    private Collections() {
    }

    /**
     * Create an immutable set from the supplied items.
     *
     * @param elements the elements to put into the new immutable set
     * @return the new immutable set; never null but possibly empty if {@code elements} is null or empty
     * @param <T> the type of elements in the set
     */
    public static <T> Set<T> immutableSet(Collection<T> elements) {
        if (elements == null || elements.isEmpty()) return emptySet();
        return java.util.Collections.unmodifiableSet(new LinkedHashSet<>(elements));
    }

    /**
     * Create an immutable set from the supplied items.
     *
     * @param elements the elements to put into the new immutable set
     * @return the new immutable set; never null but possibly empty if {@code elements} is null or empty
     * @param <T> the type of elements in the set
     */
    public static <T> Set<T> immutableSet(@SuppressWarnings("unchecked") T... elements) {
        if (elements == null || elements.length == 0) return emptySet();
        Set<T> result = new LinkedHashSet<>(elements.length);
        for (T element : elements) {
            if (element != null) result.add(element);
        }
        return java.util.Collections.unmodifiableSet(result);
    }

    /**
     * Create an empty immutable set.
     *
     * @return the new immutable set; never null
     * @param <T> the type of elements in the set
     */
    public static <T> Set<T> emptySet() {
        return java.util.Collections.emptySet();
    }
}
