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

package org.strongback.tools.logdecoder;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Map;
import java.util.StringJoiner;

import org.strongback.tools.utils.FileUtils;
import org.strongback.tools.utils.Parser;
import org.strongback.tools.utils.Printer;
import org.strongback.tools.utils.Printer.Verbosity;
import org.strongback.tools.utils.Version;

/**
 * Utility to convert Strongback Binary Logs into human readable csv format. Usage:
 *
 * The path to the input file is required and is preceded by {@code -f}. The path to the output is optional, if it is given
 * it should be preced by {@code -o}, if it is not, the output will be saved in the current directory, with the same filename
 * as the input, and a .csv extension. The {@code -q} and {@code -v} flags silence and increase output respectively.<br>
 *
 * {@code Usage: LogDecoder -f <input_file> [-o <output_file>] [-q | -v]}.
 *
 * <h1>Strongback Binary Log format:</h1>
 * <p>
 * The first three bytes are the ASCII log ({@code 6C 6F 67}). The next byte is the number of elements that have
 * been logged {@code n}. The next {@code n} bytes are the number of bytes in each data point, followed by
 * {@code n} repetitions of the length of the name of each data point and the name itself in ASCII bytes.
 * <p>
 * Now the data is recorded with respect to the number of bytes in each element.
 * When Logger is stopped it will finish writing the current record followed by the terminator: {@code FF FF FF FF}.
 * If the Logger was interrupted and was unable to finish writing the log, the decoder will recover as many records
 * as possible. (Because the Logger is unbuffered, everything up to the point of the crash should be recovered.)
 *
 * <h1>Example Strongback Binary Log:</h1>
 *
 * <i>Demonstrates the raw binary output of the logger. New lines included only for clarity,
 * and are not part of the file format</i>
 * <pre> [l o g]
 * [3]
 * [4] [2] [2]
 * [4][T i m e] [3][F o o] [3][B a r]
 * [00 00 00 00] [00 52] [00 37]
 * [00 00 00 0A] [04 D5] [23 AF]
 * [00 00 00 14] [3F 00] [12 34]
 * [FF FF FF FF]
 * </pre>
 *
 * <h1>Output CSV format:</h1>
 * <p>
 * The first row contains the names of the elements as encoded into the log file, delimited by a comma. The end
 * of the row is delimited by a newline character. Each following line lists the integer value of the data encoded,
 * to the precision specified in the log file.
 *
 * <h1>Example csv file:</h1>
 * <pre> Time, Foo, Bar
 * 0, 82, 55,
 * 10, 1237, 9135,
 * 20, 16128, 4660
 * </pre>
 *
 * <p>
 * <h1>Exit Codes:</h1>
 * 0 - Operation successful<br>
 * 1 - Invalid arguments<br>
 * 2 - Cannot open input/output file<br>
 * 3 - Input file not recognized<br>
 * 4 - Input/output exception<br>
 * 5 - Unexpected end of file<br>
 *
 * @author Zach Anderson
 */
public class LogDecoder {
    private static Printer printer = new Printer();

    /* Overhead for command line parsing and file resolution */
    public static final void main(String[] args) {
        // Try to parse command line arguments
        Map<String, String> opts = null;
        try {
            opts = Parser.parse(args, "fo", "h|f");
        } catch (InvalidParameterException e) {
            printer.error(e.getLocalizedMessage());
            printer.print(Strings.HELP, Verbosity.ALWAYS);
            System.exit(ExitCodes.INVALID_ARGUMENT);
        }
        assert opts != null;

        if(opts.containsKey("h")) {
            printer.print(Strings.HELP, Verbosity.ALWAYS);
            System.exit(ExitCodes.NORMAL);
        }

        if(opts.containsKey("n")) {
            printer.print(Strings.VERSION_HEAD, Printer.Verbosity.ALWAYS);
            printer.print(Strings.VERSION, Printer.Verbosity.ALWAYS);
            System.exit(ExitCodes.NORMAL);
        }

        printer.setVerbosity(opts.containsKey("q"), opts.containsKey("v"));

        // Try to open file streams
        File inPath;
        File outPath;
        try {
            inPath = FileUtils.resolvePath(opts.get("f"));
            printer.print(Strings.IN_PATH + inPath.getCanonicalPath(), Printer.Verbosity.VERBOSE);
            if(!opts.containsKey("o")) {
                String out = inPath.getName();

                // Strips file extension
                int extensionStart = out.contains(".") ? out.lastIndexOf('.') : out.length();
                out = out.substring(0, extensionStart) + ".csv";

                outPath = FileUtils.resolvePath(out);
            } else {
                outPath = FileUtils.resolvePath(opts.get("o"));
            }
            printer.print(Strings.OUT_PATH + outPath.getCanonicalPath(), Printer.Verbosity.VERBOSE);

            // Input stream must be buffered to use mark
            DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(inPath)));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outPath));

            // Try to convert log
            int exitCode = ExitCodes.NORMAL;
            try {
                decode(stream, writer);
                printer.print(Strings.SUCCESS, Printer.Verbosity.ALWAYS);
            } catch (BadFileFormatException e) {
                printer.error(Strings.BAD_LOG);
                exitCode = ExitCodes.BAD_LOG;
                // The log is junk, delete the CSV
                outPath.deleteOnExit();
            } catch (EOFException e) {
                printer.error(Strings.UNEXPECTED_EOF);
                exitCode = ExitCodes.UNEXPECTED_EOF;
            } catch (IOException e) {
                printer.error(Strings.FAILED_READ);
                exitCode = ExitCodes.IO_EXCEPTION;
            } finally {
                writer.close();
                stream.close();

                printer.print(Strings.OUTPUT_SAVED + outPath.getCanonicalPath(), Printer.Verbosity.ALWAYS);
                System.exit(exitCode);
            }

        } catch (InvalidParameterException e) {
            printer.error(Strings.BAD_FILEPATH);
            System.exit(ExitCodes.INVALID_ARGUMENT);
        } catch (FileNotFoundException e) {
            printer.error(Strings.CANNOT_OPEN_FILE + e.getLocalizedMessage());
            System.exit(ExitCodes.CANNOT_OPEN_FILE);
        } catch (IOException e) {
            printer.error(Strings.UNKNOWN_IO + e.getLocalizedMessage());
            System.exit(ExitCodes.IO_EXCEPTION);
        }
    }

    private static String readString(DataInputStream in) throws EOFException, IOException{
        int len = in.readInt();
        byte[] value = new byte[len];
        in.read(value);
        return new String(value,StandardCharsets.UTF_8);
    }

    /* Actually converts the log */
    private static final void decode(DataInputStream in, BufferedWriter writer)
            throws BadFileFormatException, EOFException, IOException {
        // Verify Header
        printer.print(Strings.CHECK_LOG, Printer.Verbosity.VERBOSE);
        String header = readString(in);
        printer.print("Found header = " + header, Printer.Verbosity.VERBOSE);
        if(!"data-record".equals(header)) throw new BadFileFormatException();
        printer.print(Strings.SUCCESS, Printer.Verbosity.VERBOSE);

        // Get the number of channels
        int numElements = in.readInt();
        printer.print(Strings.ELEMENT_COUNT + numElements, Printer.Verbosity.VERBOSE);

        // Get the size of each channel sample
        int[] elementSizes = new int[numElements];
        for(int i = 0; i< elementSizes.length; i++) {
            elementSizes[i] = in.readInt();
            printer.print("read channel " + i + " size = " + elementSizes[i], Printer.Verbosity.VERBOSE);
        }

        // Read and write the name of each channel
        StringJoiner joiner = new StringJoiner(",");
        for(int i = 0; i< numElements; i++) {
            joiner.add(readString(in));
        }
        writer.write(joiner.toString());
        printer.print("channel names = " + joiner, Printer.Verbosity.VERBOSE);
        writer.newLine();

        printer.print(Strings.READING_LOG, Printer.Verbosity.VERBOSE);
        int lineCount = 0;
        // Read each record
        try {
            in.mark(4);
            while(in.readInt()!=0xFFFFFFFF) {
                printer.print(Strings.READ_LINE + lineCount, Printer.Verbosity.VERBOSE);
                in.reset();
                for(int i = 0; i < numElements; i++) {
                    if (i!=0) writer.write(",");
                    if(elementSizes[i]==4){
                        int value = in.readInt();
                        writer.write(Integer.toString(value));
                    } else if(elementSizes[i]==2) {
                        short value = in.readShort();
                        writer.write(Short.toString(value));
                    } else {
                        throw new IOException("Unexpected size of data: " + elementSizes[i]);
                    }
                }
                writer.newLine();
                lineCount++;
                in.mark(4);
            }
            printer.print(Strings.SUCCESS, Printer.Verbosity.VERBOSE);
        } finally {
            // Always flush what we were able to decode
            printer.print(Strings.FLUSH_FILES, Printer.Verbosity.VERBOSE);
            writer.flush();
        }
    }

    /* UI strings */
    private static final class Strings {
        private static final String LS = System.lineSeparator();

        /* Version */
        public static final String VERSION_HEAD = "Strongback Binary Log Decoder Utility";
        public static final String VERSION      = Version.versionNumber() + " compiled on " + Version.buildDate();

        public static final String HELP =  "usage: strongback newproject [options] -f <input_file> [-o <output_file>]"
                                    + LS + ""
                                    + LS + "Description"
                                    + LS + "  Utility to convert Strongback Binary Log files into human readable CSV. If -o is not"
                                    + LS + "  specified the converted log will be saved in the current directory with the same name"
                                    + LS + "  as the binary log."
                                    + LS + ""
                                    + LS + "Options"
                                    + LS + "  -f <input_file>"
                                    + LS + "    The strongback binary log to convert"
                                    + LS + ""
                                    + LS + "  -h"
                                    + LS + "    Displays help information"
                                    + LS + ""
                                    + LS + "  -n <project_name>"
                                    + LS + "    The name of the new project"
                                    + LS + ""
                                    + LS + "  -o"
                                    + LS + "    The file to write the human readable csv log to. If not"
                                    + LS + "    specified the output will be written to the current directory"
                                    + LS + ""
                                    + LS + "  -p <package_name>"
                                    + LS + "     Specifies a custom initial package for Robot.java"
                                    + LS + ""
                                    + LS + "  -b"
                                    + LS + "     Displays version information"
                                    + LS + ""
                                    + LS + "  -v"
                                    + LS + "     Displays more verbose output"
                                    + LS + ""
                                    + LS + "  -q"
                                    + LS + "     Suppressess all output"
                                    + LS + ""
                                    + LS + "Report issues at http://github.com/strongback/strongback-java"
                                    ;

        /* Exit messages */
        public static final String BAD_FILEPATH     = "Invalid file path specified";
        public static final String CANNOT_OPEN_FILE = "Can not open file: ";
        public static final String BAD_LOG          = "File format not recognized";
        public static final String UNEXPECTED_EOF   = "Unexpected end of file, did robot crash?";
        public static final String UNKNOWN_IO       = "An IO exception occured: ";
        public static final String FAILED_READ      = "The log failed to read";
        public static final String OUTPUT_SAVED     = "Output saved to: ";

        /* Verbose messages */
        public static final String IN_PATH       = "Reading log at: ";
        public static final String OUT_PATH      = "Writing csv to: ";
        public static final String CHECK_LOG     = "Validating log header...";
        public static final String ELEMENT_COUNT = "Found elements: ";
        public static final String READING_LOG   = "Reading log...";
        public static final String READ_LINE     = "Reading line: ";
        public static final String FLUSH_FILES   = "Closing resources...";
        public static final String SUCCESS       = "Success";
    }

    private static final class ExitCodes {
        public static final int NORMAL           = 0;
        public static final int INVALID_ARGUMENT = 1;
        public static final int CANNOT_OPEN_FILE = 2;
        public static final int BAD_LOG          = 3;
        public static final int IO_EXCEPTION     = 4;
        public static final int UNEXPECTED_EOF   = 5;
    }

    @SuppressWarnings("serial")
    private static final class BadFileFormatException extends IOException { }
}
