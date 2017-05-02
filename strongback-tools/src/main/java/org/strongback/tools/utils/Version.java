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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Version {

    private static final Properties PROPS;

    static {
        PROPS = new Properties();
        try (InputStream stream = Version.class.getClassLoader().getResourceAsStream("strongback.properties")) {
            if ( stream != null ) PROPS.load(stream);
            else System.err.println("Unable to find the strongback.properties file");
        } catch (IOException e) {
            System.err.println("Unable to read the strongback.properties file");
            e.printStackTrace(System.err);
        }
    }

    public static String versionNumber() {
        return PROPS.getProperty("strongback.version");
    }

    public static String buildDate() {
        return PROPS.getProperty("build.date");
    }

    private Version() {
    }

}
