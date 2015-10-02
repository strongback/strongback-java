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

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Zero setup argument parser. 
 * @author Zach Anderson
 *
 */
public class Parser {
    
    /**
     * String based argument parser, syntax information and arguments that require a parameter are specified as strings. 
     * Does not handle any usage or help information, that should be printed elsewhere. Parses an array of GNU style command 
     * line arguments and populates a {@link Map}. Arguments without a parameter (flags) are mapped to null. Arguments 
     * that do have a parameter are mapped to that parameter.  
     * @param args arguments to parse
     * @param params a String list of arguments that require parameters {@code "abc"} means arguments a, b, and c all require parameters
     * @param syntax a String representing the syntax of the arguments. Each possible set of required arguments should be listed 
     *               separated by a {@code |}. Any arguments required to not be present should be preceded by a {@code !}. Any
     *               arguments not mentioned are assumed to always be valid. If the argument list does not meet this syntax, an
     *               {@link InvalidParameterException} is thrown.
     *               Example: {@code "abc|d!e|e!d|fgh"} - a, b, and c are required, or d is required and e cannot be present, and so on.
     * @return a {@link Map} mapping <{@link String}s to {@link String}s representing all of the command line arguments.
     */
    public static Map<String, String> parse(String[] args, String params, String syntax) {
        Map<String, String> map = new HashMap<>();
        String lastArg = "";
        for(String arg : args) {
            if(arg.length()>0) {
                if(arg.startsWith("-")) {
                    if(arg.startsWith("--")) {
                        // Hook for handling long options
                    } else {
                        for(char c : arg.toCharArray()) {
                            lastArg = String.valueOf(c);
                            map.put(lastArg, null);
                        }
                    }
                } else {
                    map.put(lastArg, arg);
                }
            }
        }
        
        // Verify arguments have required parameters
        for(char key : params.toCharArray()) {
            if(map.containsKey(String.valueOf(key)) && map.get(String.valueOf(key))==null) 
                throw new InvalidParameterException("Missing parameter for argument: -"+key);
        }
        
        // Test syntax (pipe is a regex character)
        for(String req : syntax.split("\\|")) {
            boolean failed = false;
            for(int i = 0; i < req.length(); i++) {
                if(req.charAt(i)=='!') {
                    // Failure if map contains the next character
                    if(map.containsKey(String.valueOf(req.charAt(++i)))) failed = true;
                } else {
                    // Failure if map doesn't contain this character
                    if(!map.containsKey(String.valueOf(req.charAt(i)))) failed = true;
                }
            }
            if(!failed) return Collections.unmodifiableMap(map);
        }
        throw new InvalidParameterException("Bad syntax");
    }
}


