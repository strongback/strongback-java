package org.strongback.executable;

import java.io.File;
import java.security.InvalidParameterException;

public class FileUtils {
    /**
     * Convince method that resolves a filepath if it starts with ~
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
