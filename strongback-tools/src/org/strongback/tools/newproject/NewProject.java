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

package org.strongback.tools.newproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.strongback.tools.utils.FileUtils;
import org.strongback.tools.utils.Parser;
import org.strongback.tools.utils.PropertiesUtils;
import org.strongback.tools.utils.PropertiesUtils.InvalidPropertiesException;

/**
 * Utility to create a new FRC robot project compatible with the strongback library
 * If -d and -n are specified, the project will be created in a new directory inside
 * -d with the name -n. If -r is specified, the project will be created directly inside
 * -r with the project name being the same as -r. No files will be overwritten unless -o
 * is specified.
 * 
 * <pre>usage: strongback newproject [options] -d &ltdirectory&gt -n &ltproject_name&gt
 *       strongback newproject [options] -r &ltproject_root&gt</pre>
 *
 * @author Zach Anderson
 */
public class NewProject {
        
    public static void main(String[] args) {
        // Load strongback properties
        Properties strongback = null;

        try {
            Properties stb = PropertiesUtils.load(FileUtils.resolvePath("~/strongback/strongback.properties"));
            PropertiesUtils.antify(stb);
            Properties wpi = PropertiesUtils.load(new File(stb.getProperty("wpilib.props")));
            PropertiesUtils.antify(wpi);
            strongback = PropertiesUtils.concat(stb, wpi);
        } catch (IOException e) {
            exit(Strings.MISSING_STRONGBK, ExitCodes.MISSING_FILE);
        } catch (InvalidPropertiesException e) {
            exit(Strings.BAD_PROPS + e.getLocalizedMessage(), ExitCodes.BAD_PROPS);
        }
        assert strongback != null;
        
        Map<String, String> params = null;
        try {
            params = Parser.parse(args, "npr", "v|h|nd!r|r!n!d");
        } catch(InvalidParameterException e) {
            System.err.println(e.getLocalizedMessage());
            System.out.println(Strings.HELP);
            exit("", ExitCodes.BAD_ARGS);
        }
        assert params != null;
        
        if(params.containsKey("h")) {
            System.out.println(Strings.HELP);
            exit();
        }

        if(params.containsKey("v")) {
            System.out.println(Strings.VERSION_HEAD);
            System.out.println(Strings.VERSION);
            exit();
        }
        
        File projectRoot;
        String projectName;
        String mainPackage;
        
        if(params.containsKey("r")) {
            projectRoot = FileUtils.resolvePath(params.get("r"));
            projectName = projectRoot.getName();
        } else {
            projectName = params.get("n");
            projectRoot = FileUtils.resolvePath(params.get("d") + File.separator + projectName);
        }
        
        if(params.containsKey("p")) {
            mainPackage = params.get("p");
        } else {
            mainPackage = "org.usfirst.frc.team"+ strongback.getProperty("team-number") +".robot";
        }
        
        /* Application Begins */
        
        // Source folders
        File src     = new File(projectRoot, "src"     + File.separator + mainPackage.replace('.', File.separatorChar));
        File testsrc = new File(projectRoot, "testsrc" + File.separator + mainPackage.replace('.', File.separatorChar));
        
        // Source files to copy
        File buildTemplate = new File(strongback.getProperty("strongback.templates.dir"), "build.xml.template");
        File propsTemplate = new File(strongback.getProperty("strongback.templates.dir"), "build.properties.template");
        File robotTemplate = new File(strongback.getProperty("strongback.templates.dir"), "Robot.java.template");
        File testTemplate  = new File(strongback.getProperty("strongback.templates.dir"), "TestRobot.java.template");
        
        // Eclipse specific
        File projectTemplate   = new File(strongback.getProperty("strongback.templates.dir"), "project.template");
        File classpathTemplate = new File(strongback.getProperty("strongback.templates.dir"), "classpath.template");
        
        // Destination files
        File buildProps = new File(projectRoot, "build.properties");
        File buildXML   = new File(projectRoot, "build.xml");
        File robotJava  = new File(src,         "Robot.java");
        File testJava   = new File(testsrc,     "TestRobot.java");
        
        // Eclipse specific
        File project   = new File(projectRoot, ".project");
        File classpath = new File(projectRoot, ".classpath");
        
        // If any of the files to write to already exist, give up and write message about the overwrite flag
        if(!params.containsKey("o")) {
            if(buildProps.exists()) exit(Strings.OVERWRITE_WARN + buildProps.getPath(), ExitCodes.OVERWRITE);
            if(buildXML.exists())   exit(Strings.OVERWRITE_WARN + buildXML.getPath(),   ExitCodes.OVERWRITE);
            if(robotJava.exists())  exit(Strings.OVERWRITE_WARN + robotJava.getPath(),  ExitCodes.OVERWRITE);
            if(testJava.exists())   exit(Strings.OVERWRITE_WARN + testJava.getPath(),   ExitCodes.OVERWRITE);
            
            // Eclipse specific
            if(params.containsKey("e")) {
                if(project.exists())   exit(Strings.OVERWRITE_WARN + project.getPath(),   ExitCodes.OVERWRITE);
                if(classpath.exists()) exit(Strings.OVERWRITE_WARN + classpath.getPath(), ExitCodes.OVERWRITE);
            }
        }
        
        // Verify templates exist
        if(!buildTemplate.exists()) exit(Strings.MISSING_TEMPLATE + buildTemplate.getPath(), ExitCodes.MISSING_FILE);
        if(!propsTemplate.exists()) exit(Strings.MISSING_TEMPLATE + propsTemplate.getPath(), ExitCodes.MISSING_FILE);
        if(!robotTemplate.exists()) exit(Strings.MISSING_TEMPLATE + robotTemplate.getPath(), ExitCodes.MISSING_FILE);
        if(!testTemplate.exists())  exit(Strings.MISSING_TEMPLATE + testTemplate.getPath(),  ExitCodes.MISSING_FILE);
        
        // Eclipse specific
        if(params.containsKey("e")) {
            if(!projectTemplate.exists())   exit(Strings.MISSING_TEMPLATE + projectTemplate.getPath(),   ExitCodes.MISSING_FILE);
            if(!classpathTemplate.exists()) exit(Strings.MISSING_TEMPLATE + classpathTemplate.getPath(), ExitCodes.MISSING_FILE);
        }
        
        // Make the directories
        if(!projectRoot.exists() & !projectRoot.mkdirs()) exit(Strings.FAILED_MKDIR + projectRoot.getPath(), ExitCodes.FAILED_MKDIR);
        if(!src.exists()         & !src.mkdirs())         exit(Strings.FAILED_MKDIR + src.getPath(),         ExitCodes.FAILED_MKDIR);
        if(!testsrc.exists()     & !testsrc.mkdirs())     exit(Strings.FAILED_MKDIR + testsrc.getPath(),     ExitCodes.FAILED_MKDIR);
        
        // Copy templates
        try {
            copyTo(buildTemplate, buildXML,   (line) -> line.replace("PROJECT_NAME", projectName));
            copyTo(propsTemplate, buildProps, (line) -> line.replace("PACKAGE", mainPackage));
            copyTo(robotTemplate, robotJava,  (line) -> line.replace("PACKAGE", mainPackage));
            copyTo(testTemplate,  testJava,   (line) -> line.replace("PACKAGE", mainPackage));
            
            // Eclipse specific
            if(params.containsKey("e")) {
                copyTo(projectTemplate,   project,   (line) -> line.replace("PROJECT_NAME", projectName));
                copyTo(classpathTemplate, classpath, (line) -> line);
            }
        } catch (IOException e) {
            exit(Strings.IO_EXCEPTION + e.getLocalizedMessage(), ExitCodes.IO_EXCEPT);
        }
   
        // Print success
        System.out.print(Strings.SUCCESS);
        try {
            System.out.println(projectRoot.getCanonicalPath());
        } catch (IOException e) {
            System.out.println(projectRoot.getPath());
        }
    }

    private static void copyTo(File input, File output, Function<String, String> each) throws IOException {
        BufferedReader testReader = new BufferedReader(new FileReader(input));
        BufferedWriter testWriter = new BufferedWriter(new FileWriter(output));
        try {
            while(testReader.ready()) {
                testWriter.write(each.apply(testReader.readLine().replace("DATE", new Date().toString())));
                testWriter.newLine();
            }
        } finally {
            testWriter.close();
            testReader.close();
        }
    }
    
    private static void exit(String exitMessage, int exitCode) {
        System.err.println(exitMessage);
        System.exit(exitCode);
    }
    
    private static void exit() {
        System.exit(ExitCodes.NORMAL);
    }
    
    private static final class Strings {
        public static final String LS = System.lineSeparator();
        /* Error Text */
        public static final String BAD_PROPS        = "Error reading strongback.properties: ";
        public static final String FAILED_MKDIR     = "Failed to create project directory at: ";
        public static final String OVERWRITE_WARN   = "The file already exists, aborting job. To overwrite exisiting files run this"
                                                      + " application with the -o option. ";
        public static final String MISSING_TEMPLATE = "Cannot locate template file. Double check that the strongback folder is in"
                                                      + " the same directory as your wpilib folder. ";
        public static final String IO_EXCEPTION     = "An IO Exception occured. ";
        public static final String MISSING_STRONGBK = "Could not locate the strongback directory. Double check that the strongback"
                                                      + " folder is in the same directory as your wpilib folder.";
        public static final String SUCCESS          = "Successfully created new project at: ";
        
        public static final String HELP =  "usage: strongback newproject [options] -d <directory> -n <project_name>"
                                    + LS + "       strongback newproject [options] -r <project_root>"
                                    + LS + ""
                                    + LS + "Description"
                                    + LS + "  Utility to create a new FRC robot project compatible with the strongback library"
                                    + LS + "  If -d and -n are specified, the project will be created in a new directory inside"
                                    + LS + "  -d with the name -n. If -r is specified, the project will be created directly inside"
                                    + LS + "  -r with the project name being the same as -r. No files will be overwritten unless -o"
                                    + LS + "  is specified."
                                    + LS + ""
                                    + LS + "Options"
                                    + LS + "  -d <parent_directory>"
                                    + LS + "    The directory to create the new project directory in"
                                    + LS + ""
                                    + LS + "  -e"
                                    + LS + "    Adds project metadata for the Eclipse Java IDE"
                                    + LS + ""
                                    + LS + "  -h"
                                    + LS + "    Displays help information"
                                    + LS + ""
                                    + LS + "  -n <project_name>"
                                    + LS + "    The name of the new project"
                                    + LS + ""
                                    + LS + "  -o"
                                    + LS + "    Forces overwriting of exisiting files"
                                    + LS + ""
                                    + LS + "  -p <package_name>"
                                    + LS + "     Specifies a custom initial package for Robot.java"
                                    + LS + ""
                                    + LS + "  -v"
                                    + LS + "     Displays version information"
                                    ;
        
        /* Version Information */
        // TODO Can we update this with ant?
        public static final String VERSION_HEAD = "Strongback New Project Utility";
        public static final String VERSION      = "1.1.0 compiled on 1 Oct 2015";
    }
    
    private static final class ExitCodes {
        public static final int NORMAL       = 0;
        public static final int BAD_PROPS    = 1;
        public static final int BAD_ARGS     = 2;
        public static final int IO_EXCEPT    = 3;
        public static final int FAILED_MKDIR = 4;
        public static final int OVERWRITE    = 5;
        public static final int MISSING_FILE = 6;
        
    }
}
