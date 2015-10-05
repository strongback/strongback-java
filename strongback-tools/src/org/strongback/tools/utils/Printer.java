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

/**
 * Utility to manage printing to stdout and sterr. Provides methods to easily change the verbosity of output.
 * @author Zach Anderson
 *
 */
public class Printer {
    private boolean quiet = false;
    private boolean verbose = false;
    
    /**
     * Sets the verbosity of this {@link Printer}. If {@code q} is set, no messages will be displayed.
     * If {@code v} is set all messages will be displayed. If {@code v} is not set, only messages with a
     * verbosity of {@link Verbosity#ALWAYS} will be displayed.
     * @param q silence output
     * @param v verbose output
     */
    public void setVerbosity(boolean q, boolean v) {
        quiet = q;
        verbose = v;
    }
    
    /**
     * Print the specified {@link String} to stdout if the current verbosity allows it and this {@link Printer} is not silenced.
     * @param s the {@link String} to print
     * @param verbosity The {@link Verbosity} level to print the message at
     */
    public void print(String s, Verbosity verbosity) {
        if(!quiet) {
            switch(verbosity) {
                case ALWAYS:
                    System.out.println(s);
                    break;
                case VERBOSE:
                    if(verbose) System.out.println(s);
                    break;
            }
        }
    }
    
    /**
     * Print the specified {@link String} to stderr if this {@link Printer} is not silenced..
     * @param s the {@link String} to print
     */
    public void error(String s) {
        if(!quiet) System.err.println(s);
    }
    
    public static enum Verbosity { ALWAYS, VERBOSE }
}
