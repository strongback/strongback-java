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

/**
 * A writer used to periodically write data.
 *
 * @author Randall Hauch
 * @see Strongback.Configurator#recordDataTo(java.util.function.Function)
 */
public interface DataWriter extends AutoCloseable {

    /**
     * Writes the current status of the data channels.
     *
     * @param time the current time in milliseconds
     */
    public void write(long time);

    /**
     * Frees the resources used by this {@link DataWriter}.
     */
    @Override
    public void close();
}
