package org.strongback.executable.utils;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Parser {
    
    // TODO I don't think this is in English
    /**
     * Lightweight argument parser, usage information should be handled by the caller. Handles an array of GNU style command line
     * arguments and populates a {@link Map}. Arguments without a parameter (flags) are mapped to null. Arguments that do 
     * have a parameter are mapped to that parameter.    
     * @param args arguments to parse
     * @param params a String list of arguments that require parameters {@code "abc"} means arguments a, b, and c all require parameters
     * @param syntax a String representing the syntax of the arguments. Each possible set of required arguments should be listed 
     *               separated by a {@code |}. Any arguments required to not be present should be preceded by a {@code !}. Any
     *               arguments not mentioned are assumed to always be valid.
     *               Example: {@code "abc|d!e|e!d|fgh"}
     * @return
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


