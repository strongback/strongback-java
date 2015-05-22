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
 * A {@link Logger} implementation that sends messages to {@link System#out} and {@link System#err}.
 *
 * @author Randall Hauch
 */
@ThreadSafe
final class SystemLogger implements Logger {

    private static final int ERROR = 2 << 0;
    private static final int WARN = 2 << 1;
    private static final int INFO = 2 << 2;
    private static final int DEBUG = 2 << 3;
    private static final int TRACE = 2 << 4;

    private volatile int level = INFO | WARN | ERROR;

    @Override
    public void error(Throwable t) {
        if ((level & ERROR) == ERROR) t.printStackTrace();
    }

    @Override
    public void error(String message) {
        if ((level & ERROR) == ERROR) message("ERROR", message);
    }

    @Override
    public void error(Throwable t, String message) {
        if ((level & ERROR) == ERROR) {
            message("ERROR", message);
            t.printStackTrace();
        }
    }

    @Override
    public void warn(String message) {
        if ((level & WARN) == WARN) message("WARN", message);
    }

    @Override
    public void info(String message) {
        if ((level & INFO) == INFO) message("INFO", message);
    }

    @Override
    public void debug(String message) {
        if ((level & DEBUG) == DEBUG) message("DEBUG", message);
    }

    @Override
    public void trace(String message) {
        if ((level & TRACE) == TRACE) message("TRACE", message);
    }

    private void message(String level, String message) {
        System.out.println(level + " " + message);
    }

    public SystemLogger enable(Level level) {
        switch (level) {
            case TRACE:
                this.level = TRACE | DEBUG | INFO | WARN | ERROR;
                break;
            case DEBUG:
                this.level = DEBUG | INFO | WARN | ERROR;
                break;
            case INFO:
                this.level = INFO | WARN | ERROR;
                break;
            case WARN:
                this.level = WARN | ERROR;
                break;
            case ERROR:
                this.level = ERROR;
                break;
        }
        return this;
    }
}
