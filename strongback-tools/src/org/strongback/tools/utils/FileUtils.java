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

package org.strongback.tools.utils;

import java.io.File;
import java.util.function.Supplier;

/**
 * Utility methods for working with files
 *
 * @author Zach Anderson
 *
 */
public class FileUtils {
    /**
     * Convenience method that replaces the {@code ~} character at the beginning of the supplied string with the user's home
     * directory.
     *
     * @param path the path to resolve
     * @return the path with the {@code ~} replaced with the user's home directory
     */
    public static final String resolveHome(String path) {
        return resolveHome(path, () -> System.getProperty("user.home"));
    }

    /**
     * Convenience method that resolves a file path if it starts with {@code ~}.
     *
     * @param path the path to resolve
     * @param userHomeSupplier the value to be used for the home directory path; may not be null
     * @return the path with the {@code ~} replaced with the user's home directory
     */
    public static final String resolveHome(String path, Supplier<String> userHomeSupplier) {
        if (path.length() == 0) return path;
        if (path.charAt(0) == '~') {
            path = userHomeSupplier.get() + path.substring(1);
        }
        return path;
    }

    /**
     * Convenience method that resolves a file path if it starts with {@code ~}.
     *
     * @param path the path to resolve
     * @return an absolute {@link File} representing that path
     */
    public static final File resolvePath(String path) {
        return resolvePath(path,() -> System.getProperty("user.home"));
    }

    /**
     * Convenience method that resolves a file path if it starts with {@code ~}.
     *
     * @param path the path to resolve
     * @param userHomeSupplier the value to be used for the home directory path; may not be null
     * @return an absolute {@link File} representing that path
     */
    public static final File resolvePath(String path, Supplier<String> userHomeSupplier) {
        path = resolveHome(path, userHomeSupplier);
        return new File(path).getAbsoluteFile();
    }
}
