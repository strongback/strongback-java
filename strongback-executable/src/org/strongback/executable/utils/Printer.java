package org.strongback.executable.utils;

public class Printer {
    private boolean quiet = false;
    private boolean verbose = false;
    
    public void setVerbosity(boolean q, boolean v) {
        quiet = q;
        verbose = v;
    }
    
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
    
    public void error(String s) {
        if(!quiet) System.err.println(s);
    }
    
    public static enum Verbosity { ALWAYS, VERBOSE }
}
