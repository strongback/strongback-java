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

import org.strongback.annotation.Immutable;

/**
 * @author Randall Hauch
 *
 */
@Immutable
final class NoOpLogger implements Logger {

    public static final Logger INSTANCE = new NoOpLogger();

    private NoOpLogger() {
    }

    @Override
    public void error(Throwable t) {
    }

    @Override
    public void error(Throwable t, String message) {
    }

    @Override
    public void error(String message) {
    }

    @Override
    public void warn(String message) {
    }

    @Override
    public void info(String message) {
    }

    @Override
    public void debug(String message) {
    }

    @Override
    public void trace(String message) {
    }

}
