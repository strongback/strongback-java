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
import java.security.InvalidParameterException;

/**
 * Utility methods for working with files
 * @author Zach Anderson
 *
 */
public class FileUtils {
    /**
     * Convenience method that resolves a filepath if it starts with ~
     * @param path the path to resolve
     * @return an absolute {@link File} representing that path
     */
    public static final File resolvePath(String path) {
        if(path.length()==0) throw new InvalidParameterException();
        
        if(path.charAt(0) == '~')
            path = System.getProperty("user.home") + path.substring(1);
        
        File file = new File(path).getAbsoluteFile();
        return file;
    }
}
