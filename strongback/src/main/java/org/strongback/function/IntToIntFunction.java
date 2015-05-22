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

package org.strongback.function;

import java.util.function.Function;

/**
 * Represents a function that accepts an integer-valued argument and produces an
 * integer-valued result.  This is the {@code int}-to-{@code int} primitive
 * specialization for {@link Function}.
 * <p>This is a functional interface whose functional method is {@link #applyAsInt(int)}.
 *
 * @see Function
 * @author Randall Hauch
 */
@FunctionalInterface
public interface IntToIntFunction {

    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result
     */
    public int applyAsInt(int value);
}
