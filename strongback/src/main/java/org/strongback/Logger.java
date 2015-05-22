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

import org.strongback.annotation.ThreadSafe;

/**
 * A simple logging framework.
 *
 * @author Randall Hauch
 */
@ThreadSafe
public interface Logger {

    public static enum Level {
        ERROR, WARN, INFO, DEBUG, TRACE;
    }

    /**
     * Log an exception at the error level. The exception and message are logged only if error-level (or higher) logging is
     * enabled.
     *
     * @param t the exception
     */
    public void error(Throwable t);

    /**
     * Log an exception and a custom error message at the error level. The exception and message are logged only if error-level
     * (or higher) logging is enabled.
     *
     * @param t the exception
     * @param message the error message
     */
    public void error(Throwable t, String message);

    /**
     * Log a message at the error level. The message is logged only if error-level (or higher) logging is enabled.
     *
     * @param message the error message
     */
    public void error(String message);

    /**
     * Log a message at the warning level. The message is logged only if warning-level (or higher) logging is enabled.
     *
     * @param message the warning message
     */
    public void warn(String message);

    /**
     * Log a message at the information level. The message is logged only if information-level (or higher) logging is enabled.
     *
     * @param message the message
     */
    public void info(String message);

    /**
     * Log a message at the debug level. The message is logged only if debug-level (or higher) logging is enabled.
     *
     * @param message the message
     */
    public void debug(String message);

    /**
     * Log a message at the trace level. The message is logged only if trace-level (or higher) logging is enabled.
     *
     * @param message the message
     */
    public void trace(String message);

    /**
     * Get a {@link Logger} implementation that does nothing with log messages.
     * @return the no-operation logger; never null
     */
    public static Logger noOp() {
        return NoOpLogger.INSTANCE;
    }
}
