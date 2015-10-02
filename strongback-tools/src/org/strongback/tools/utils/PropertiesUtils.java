package org.strongback.tools.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods to make working with .properties files a bit easier
 * @see java.util.Properties
 * @author Zach Anderson
 */
public class PropertiesUtils {
    /**
     * Loads the specified properties file into memory
     * @param file the properties {@link File} to load
     * @return a {@link Properties} based on the specified {@link File}
     * @throws FileNotFoundException if the specified {@link File} does not exist or is a directory
     * @throws IOException if an error occurs while reading the {@link File}
     */
    public static Properties load(File file) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(new FileReader(file));
        return props;
    }
    
    /**
     * Combines multiple {@link Properties} into a single {@link Properties} without 
     * modifying any of the original {@link Properties}. If the same key is defined in two {@link Properties},
     * it will be assigned the value appearing in the latest {@link Properties} in the list.
     * @param props the {@link Properties} to concatenate
     * @return a single {@link Properties} containing all of the key value pairs
     */
    public static Properties concat(Properties... props) {
        Properties out = new Properties();
        for(Properties prop : props) {
            prop.forEach(out::put);
        }
        return out;
    }
    
    /**
     * Replaces any ant style references <code>${property}</code> to another property in a {@link Properties} with the actual value of that property 
     * in depth first order.
     * @param props the {@link Properties} to modify
     * @throws InvalidPropertiesException if a referenced property is undefined or is defined in terms of itself, directly
     * or indirectly
     */
    public static void antify(Properties props) throws InvalidPropertiesException {
        Set<String> keys = props.stringPropertyNames();
        
        for(String key : keys) {
            Set<String> v = new HashSet<>();
            props.setProperty(key, resolve(key, props, v));
        }
    }
    
    private static String resolve(String key, Properties props, Set<String> visted) throws InvalidPropertiesException {
        if(visted.contains(key)) throw new InvalidPropertiesException(key + " is defined cyclically.");
        visted.add(key);
        
        if(!props.containsKey(key)) {
            if(System.getProperty(key)!=null) return System.getProperty(key);
            throw new InvalidPropertiesException(key + " is undefined.");
        }
        
        String value = props.getProperty(key);
        
        //One or more of any character preceded by ${ and followed by } but not including them
        Matcher grabber = Pattern.compile("(?<=\\$\\{).+(?=\\})").matcher(value);
        
        Set<String> toResolve = new HashSet<>();
        while(grabber.find()) {
            toResolve.add(grabber.group());
        }
        // No further resolution is needed
        if(toResolve.isEmpty()) return value;
        
        for(String s : toResolve) {
            String resolution = resolve(s, props, visted);
            value = value.replace("${"+s+"}", resolution);
        }
        
        // Set the value so it doesn't need to be resolved again later
        props.setProperty(key, value);
        return value;
    }
    
    public static class InvalidPropertiesException extends Exception{
        private static final long serialVersionUID = 1L;
        
        public InvalidPropertiesException(String msg) {
            super(msg);
        }
    }
}
