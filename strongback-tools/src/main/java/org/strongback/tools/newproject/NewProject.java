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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.strongback.tools.utils.FileUtils;
import org.strongback.tools.utils.Parser;
import org.strongback.tools.utils.PropertiesUtils;
import org.strongback.tools.utils.PropertiesUtils.InvalidPropertiesException;
import org.strongback.tools.utils.Version;

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

    private static boolean debug;

    private static void debug( Object msg ) {
        if ( debug ) System.out.println("DEBUG: " + msg);
    }

    public static void main(String[] args) {
        // Parse the parameters ...
        Map<String, String> params = null;
        try {
            params = Parser.parse(args, "npr", "D|v|h|i|nd!r|r!n!d");
        } catch(InvalidParameterException e) {
            System.err.println(e.getLocalizedMessage());
            System.out.println(Strings.HELP);
            exit("", ExitCodes.BAD_ARGS);
        }
        assert params != null;

        debug = params.containsKey("D");
        if(params.containsKey("h")) {
            System.out.println(Strings.HELP);
            exit();
        }
        if(params.containsKey("v")) {
            System.out.println(Strings.VERSION_HEAD);
            System.out.println(Strings.VERSION);
            exit();
        }

        // Resolve the Strongback installation directory ...
        debug("Resolving '~/strongback'");
        File strongbackDirectory = FileUtils.resolvePath("~/strongback");
        if ( strongbackDirectory == null || !strongbackDirectory.exists()) {
            exit("Unable to find the 'strongback' installation directory. Check the Strongback installation.", ExitCodes.BAD_ENV);
        }
        strongbackDirectory = strongbackDirectory.getAbsoluteFile();
        if ( !strongbackDirectory.isDirectory() ) {
            exit("Expecting '" + strongbackDirectory + "' to be a directory. Check the Strongback installation.",ExitCodes.BAD_ENV);
        }
        if ( !strongbackDirectory.canRead() ) {
            exit("Unable to read the 'strongback' installation directory at " + strongbackDirectory,ExitCodes.BAD_ENV);
        }
        // replace the windows backslashes with forward slashes so i) string.replaceAll works, and ii) Eclipse paths are correct
        final String strongbackPath = strongbackDirectory.getAbsolutePath().replaceAll("\\\\", "/");
        debug("Resolved '~/strongback' to '" + strongbackPath + "'");

        // Load the strongback properties ...
        debug("Checking '~/strongback/strongback.properties' file");
        File strongbackPropsFile = new File(strongbackDirectory,"/strongback.properties");
        if ( !strongbackPropsFile.exists() ) {
            exit("Unable to find the 'strongback.properties' file in the installation directory. Check the Strongback installation.",ExitCodes.BAD_ENV);
        }
        strongbackPropsFile = strongbackPropsFile.getAbsoluteFile();
        if ( !strongbackPropsFile.isFile() ) {
            exit("Expecting '" + strongbackPropsFile + "' to be a file but was a directory. Check the Strongback installation.",ExitCodes.BAD_ENV);
        }
        if ( !strongbackPropsFile.canRead() ) {
            exit("Unable to read the '" + strongbackPropsFile + "' file. Check the Strongback installation.",ExitCodes.BAD_ENV);
        }
        Properties strongbackProperties = null;
        try {
            debug("Loading '" + strongbackPropsFile + "' file");
            strongbackProperties = PropertiesUtils.load(strongbackPropsFile);
            PropertiesUtils.antify(strongbackProperties);
            debug("Loaded '" + strongbackPropsFile.getAbsoluteFile() + "' file");
        } catch (IOException e) {
            exit("Unable to load the '" + strongbackPropsFile + "' file: " + e.getMessage(),ExitCodes.BAD_ENV);
        } catch (InvalidPropertiesException e) {
            exit("Invalid property field in '" + strongbackPropsFile + "' file: " + e.getMessage(),ExitCodes.BAD_ENV);
        }

        // Resolve the WPILib installation directory ...
        String wpiPath = strongbackProperties.getProperty("wpilib.home");
        debug("Checking for the WPILib installation directory " + wpiPath);
        if ( wpiPath == null ) {
            exit("Strongback properties file '" + strongbackPropsFile + "' must specify the WPILIb directory in 'wpilib.home'",ExitCodes.BAD_ENV);
        }
        File wpiLibDir = new File(wpiPath).getAbsoluteFile();
        if ( !wpiLibDir.exists() ) {
            exit("Unable to find the '" + wpiLibDir.getName() + "' installation directory.  Make sure the 'wpilib.home' property in '" + strongbackPropsFile + "' points to a valid version of WPILib installation.",ExitCodes.BAD_ENV);
        }
        if ( !wpiLibDir.isDirectory() ) {
            exit("Expecting '" + wpiLibDir + "' to be a directory but was a file. Make sure the 'wpilib.home' property in '" + strongbackPropsFile + "' points to a valid version of WPILib installation.",ExitCodes.BAD_ENV);
        }
        if ( !wpiLibDir.canRead() ) {
            exit("Unable to read the '" + wpiLibDir + "' file. Check the WPILib version and file permissions.",ExitCodes.BAD_ENV);
        }
        debug("Found valid WPILib installation directory: " + wpiLibDir);

        // Load the WPILib properties (which may not exist anymore) ...
        debug("Looking for WPILib properties file");
        String wpiLibPropsPath = strongbackProperties.getProperty("wpilib.props", new File(wpiLibDir,"wpilib.properties").getAbsolutePath());
        debug("Checking '" + wpiLibPropsPath + "' file");
        File wpiLibPropsFile = new File(wpiLibPropsPath);
        Properties wpi = new Properties();
        if ( wpiLibPropsFile.exists() && wpiLibPropsFile.isFile() && wpiLibPropsFile.canRead() ) {
            wpiLibPropsFile = wpiLibPropsFile.getAbsoluteFile();
            try {
                debug("Loading '" + wpiLibPropsFile + "' file");
                wpi = PropertiesUtils.load(wpiLibPropsFile);
                PropertiesUtils.antify(wpi);
                debug("Loaded '" + wpiLibPropsFile.getAbsoluteFile() + "' file");
            } catch (IOException e) {
                exit("Unable to load the '" + wpiLibPropsFile + "' file: " + e.getMessage(),ExitCodes.BAD_PROPS);
            } catch (InvalidPropertiesException e) {
                exit("Invalid property field in '" + wpiLibPropsFile + "' file: " + e.getMessage(),ExitCodes.BAD_PROPS);
            }
        } else {
            debug("WPILib installation does not contain a properties file, so skipping this step");
        }

        final Properties strongback = PropertiesUtils.concat(strongbackProperties, wpi);
        debug("The Strongback properties are: " + strongback);

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
        debug("The project root will be: " + projectRoot);
        debug("The project name will be: " + projectName);

        if(params.containsKey("p")) {
            mainPackage = params.get("p");
        } else {
            mainPackage = "org.usfirst.frc.team"+ strongback.getProperty("team-number") +".robot";
        }
        debug("The main package for the robot will be '" + mainPackage + "'");

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
        File userlibTemplate  = new File(strongback.getProperty("strongback.templates.dir"), "Strongback.userlibraries.template");
        File userlibImportTemplate  = new File(strongback.getProperty("strongback.templates.dir"), "Strongback.userlibraries.import.template");

        // Verify templates exist
        if(!buildTemplate.exists()) exit(Strings.MISSING_TEMPLATE + buildTemplate.getPath(), ExitCodes.MISSING_FILE);
        if(!propsTemplate.exists()) exit(Strings.MISSING_TEMPLATE + propsTemplate.getPath(), ExitCodes.MISSING_FILE);
        if(!robotTemplate.exists()) exit(Strings.MISSING_TEMPLATE + robotTemplate.getPath(), ExitCodes.MISSING_FILE);
        if(!testTemplate.exists())  exit(Strings.MISSING_TEMPLATE + testTemplate.getPath(),  ExitCodes.MISSING_FILE);

        // Destination files
        File buildProps = new File(projectRoot, "build.properties");
        File buildXML   = new File(projectRoot, "build.xml");
        File robotJava  = new File(src,         "Robot.java");
        File testJava   = new File(testsrc,     "TestRobot.java");

        // Eclipse specific
        File project   = new File(projectRoot, ".project");
        File classpath = new File(projectRoot, ".classpath");
        File metadataDir = new File(projectRoot.getParentFile(), ".metadata");

        // Be sure to always generate the Eclipse files that are part of the installation ...
        try {
            File eclipseDir = FileUtils.resolvePath("~/strongback/java/eclipse");
            eclipseDir.mkdirs();
            debug("Created the '" + eclipseDir + "' directory to hold generated files.");
            // user libraries importable file ...
            File userlibraries = new File(eclipseDir, "Strongback.userlibraries");
            if(!userlibraries.exists()) {   // don't overwrite
                copyTo(userlibImportTemplate, userlibraries, (line) -> line.replaceAll("STRONGBACKHOME", strongbackPath));
            }
            debug("Created the '" + userlibraries + "' file for manually importing the Strongback user libraries.");
        } catch (IOException e) {
            exit(Strings.IO_EXCEPTION + e.getLocalizedMessage(), ExitCodes.IO_EXCEPT);
        }

        // --------------------------
        // PROJECT-SPECIFIC FILES ...
        // --------------------------

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
        boolean eclipseProject = false;
        boolean foundMetadata = false;
        boolean updatedMetadata = false;
        try {
            copyTo(buildTemplate, buildXML,   (line) -> line.replace("PROJECT_NAME", projectName));
            copyTo(propsTemplate, buildProps, (line) -> line.replace("PACKAGE", mainPackage));
            copyTo(robotTemplate, robotJava,  (line) -> line.replace("PACKAGE", mainPackage));
            copyTo(testTemplate,  testJava,   (line) -> line.replace("PACKAGE", mainPackage));

            // Eclipse specific
            if(params.containsKey("e")) {
                copyTo(projectTemplate,   project,   (line) -> line.replace("PROJECT_NAME", projectName));
                copyTo(classpathTemplate, classpath, (line) -> line);

                // See if the `Strongback` user library is in the workspace ...
                if (metadataDir.exists()) {
                    foundMetadata = true;
                    File jdtPrefsFile = new File(metadataDir,".plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs");
                    if (jdtPrefsFile.exists()) {
                        Properties jdtPrefs = new Properties();
                        try ( InputStream is = new FileInputStream(jdtPrefsFile) ) {
                            jdtPrefs.load(is);
                        }
                        if (!jdtPrefs.isEmpty() && !jdtPrefs.containsKey("org.eclipse.jdt.core.userLibrary.Strongback")) {
                            debug("Adding the Strongback user library to the Eclipse workspace at " + metadataDir.getParent());
                            // Make a backup of the original preferences file ...
                            File jdtPrefsFileCopy = new File(jdtPrefsFile.getParentFile(),"org.eclipse.jdt.core.prefs.backup");
                            copyTo(jdtPrefsFile, jdtPrefsFileCopy, (line) -> line);
                            debug("Created backup of " + jdtPrefsFile);

                            // Read in the userlibrary file and escape all the required characters ...
                            List<String> lines = Files.readAllLines(userlibTemplate.toPath(), StandardCharsets.UTF_8);
                            String escapedContents = combineAndEscape(lines, strongbackPath);
                            debug("Escaped contents of the preference file:" + System.lineSeparator() + escapedContents);

                            // Set the property and output the file ...
                            jdtPrefs.setProperty("org.eclipse.jdt.core.userLibrary.Strongback", escapedContents);
                            try ( OutputStream os = new FileOutputStream(jdtPrefsFile) ) {
                                jdtPrefs.store(os,"");
                                debug("Updated preference file");
                                updatedMetadata = true;
                            }
                        }
                    }
                }
                eclipseProject = true;
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
        if ( foundMetadata && updatedMetadata ) {
            System.out.print(Strings.UPDATED_WORKSPACE);
            System.out.println(metadataDir.getParentFile().getAbsolutePath());
            System.out.print(Strings.RESTART_ECLIPSE);
        } else if ( eclipseProject ) {
            System.out.println(Strings.IMPORT_ECLIPSE);
            if ( !foundMetadata ) {
                System.out.print(Strings.IMPORT_USERLIB);
                System.out.println(strongbackDirectory.getAbsolutePath() + "/java/eclipse/Strongback.userlibraries");
            }
        }
    }

    protected static String combineAndEscape( List<String> lines, String strongbackHome ) throws IOException {
        StringBuilder sb = new StringBuilder();
        lines.forEach(str->{
            debug("Pre-escaped line: " + str);
            String replaced = str.replaceAll("STRONGBACKHOME",strongbackHome).replaceAll("    ", "\t");
            sb.append(replaced).append("\n");
            debug("Escaped line:     " + replaced);
        });
        return sb.toString();
    }

    protected static void copyTo(File input, File output, Function<String, String> each) throws IOException {
        BufferedReader testReader = new BufferedReader(new FileReader(input));
        BufferedWriter testWriter = new BufferedWriter(new FileWriter(output));
        String now = new Date().toString();
        try {
            while(testReader.ready()) {
                testWriter.write(each.apply(testReader.readLine().replace("DATE", now)));
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
        public static final String FAILED_MKDIR     = "Failed to create project directory at: ";
        public static final String OVERWRITE_WARN   = "The file already exists, aborting job. To overwrite exisiting files run this"
                                                      + " application with the -o option. ";
        public static final String MISSING_TEMPLATE = "Cannot locate template file. Double check that the strongback folder is in"
                                                      + " the same directory as your wpilib folder. ";
        public static final String IO_EXCEPTION     = "An IO Exception occured. ";
        public static final String SUCCESS          = "Successfully created new project at: ";
        public static final String UPDATED_WORKSPACE= "\nAdded the Strongback user library to your Eclipse workspace at ";
        public static final String RESTART_ECLIPSE  = "Restart this Eclipse workspace and import the project.\n";
        public static final String IMPORT_ECLIPSE   = "\nProject ready for importing into Eclipse workspace.";
        public static final String IMPORT_USERLIB   = "\nEnsure that the Strongback user library is defined in workspace; if not then import from ";

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
                                    + LS + "    Adds project metadata for the Eclipse Java IDE, and looks for the Eclipse workspace"
                                    + LS + "    .metadata folder to add the Strongback user library if not already defined."
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
        public static final String VERSION      = Version.versionNumber() + " compiled on " + Version.buildDate();
    }

    private static final class ExitCodes {
        public static final int NORMAL       = 0;
        public static final int BAD_PROPS    = 1;
        public static final int BAD_ARGS     = 2;
        public static final int IO_EXCEPT    = 3;
        public static final int FAILED_MKDIR = 4;
        public static final int OVERWRITE    = 5;
        public static final int MISSING_FILE = 6;
        public static final int BAD_ENV      = 7;
    }
}
