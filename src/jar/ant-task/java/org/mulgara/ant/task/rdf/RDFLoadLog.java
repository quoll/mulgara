/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is the Kowari Metadata Store.
 *
 * The Initial Developer of the Original Code is Plugged In Software Pty
 * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
 * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
 * Plugged In Software Pty Ltd. All Rights Reserved.
 *
 * Contributor(s): N/A.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text
 * of the notices in the Source Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.ant.task.rdf;

// Java
// Ant
// Ant
import java.io.*; // Java

// Java
import java.util.*;

/**
 * Handles reading from and writing to the RDF load log file.
 *
 * @created 2002-11-07
 *
 * @author Ben Warren
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:32 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2002 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class RDFLoadLog {

  /** Get line separator. */
  private static final String eol = System.getProperty("line.separator");

  /** The file to log to. */
  private File logFile = null;

  /** Writer for writing to the log file */
  private PrintWriter logWriter = null;

  /** Set of files from the log that are already loaded */
  private Set<String> loadedFileSet = new HashSet<String>();

  /** A buffer to log errors to */
  private StringBuffer errorBuffer = new StringBuffer();

  /** The number of files loaded in all logged runs */
  private int numLoaded = 0;

  /** The number of errors that occurred */
  private int numErrors = 0;

  /**
   * Public constructor that initialises the log. The method {@link #close()}
   * should be called when logging is complete.
   *
   * @param file The log file.
   * @param read If true, Read in previously loaded files.
   * @throws IOException EXCEPTION TO DO
   */
  public RDFLoadLog(File file, boolean read) throws IOException {

    logFile = file;

    List<String> loadedList = null;

    if (read) loadedList = readLog();

    setupLogWriter();

    if ((loadedList != null) && (loadedList.size() > 0)) {
      writeExisting(loadedList);
    }
  }

  /**
   * Is the file already loaded.
   *
   * @param file The name of the file to check.
   * @return true if the file is already loaded.
   */
  public boolean isLoaded(String file) {
    return loadedFileSet.contains(file);
  }

  /**
   * Get the number of files loaded from the current and previous logged runs.
   *
   * @return The number of files loaded from the current and previous logged runs.
   */
  public int getNumLoaded() {
    return numLoaded;
  }

  /**
   * Get the number of errors that occurred.
   *
   * @return The the number of errors that occurred.
   */
  public int getNumErrors() {
    return numErrors;
  }

  /**
   * Log that a file was loaded.
   *
   * @param file The file to log as loaded.
   */
  public void logLoadedFile(File file) {
    logWriter.println(file.getAbsolutePath());
    logWriter.flush();
    numLoaded++;
  }

  /**
   * Log that a list of files was loaded.
   *
   * @param list The list of files (File objects) to log as loaded.
   */
  public void logLoadedFiles(List<File> list) {
    for (File file: list) {
      logWriter.println(file.getAbsolutePath());
      numLoaded++;
    }

    logWriter.flush();
  }

  /**
   * Log that load error occurred.
   *
   * @param file The file the error occurred on.
   * @param message An error message.
   */
  public void logLoadError(File file, String message) {
    errorBuffer.append("Could not load file '" + file.getAbsolutePath() +
                       "'." + eol + message + eol + eol);
    numErrors++;
  }

  /**
   * Log that load error occurred.
   *
   * @param message An error message.
   */
  public void logLoadError(String message) {
    errorBuffer.append(message + eol + eol);
    numErrors++;
  }

  /**
   * Close the log writer, should be called when logging is complete.
   *
   */
  public void close() {
    // Write out the errors
    logWriter.println();
    logWriter.println("Total files loaded: " + numLoaded);

    if (numErrors > 0) {

      logWriter.println();
      logWriter.println("Errors during load");
      logWriter.println("==================");
      logWriter.print(errorBuffer);
    }

    logWriter.println("Total errors: " + numErrors);

    // Close the writer
    logWriter.flush();
    logWriter.close();
  }

  /**
   * Setup the log writer and write any files already loaded.
   *
   * @throws IOException on error.
   */
  private void setupLogWriter() throws IOException {

    if (logFile != null) {
      // Make the log file and any required directories
      if (!logFile.exists()) logFile.getParentFile().mkdirs();

      logWriter = new PrintWriter(new FileWriter(logFile));
    }
  }

  /**
   * Reads in the files already loaded from the log file.
   *
   * @return A list of all the lines from the log file.
   * @throws IOException on error.
   */
  private List<String> readLog() throws IOException {

    List<String> loadedList = new ArrayList<String>();

    if (logFile.exists()) {

      BufferedReader inReader = new BufferedReader(new FileReader(logFile));
      try {
        String line = inReader.readLine();

        // Read in the files previously loaded.
        while ((line != null) && (line.trim().length() != 0)) {

          loadedFileSet.add(line);

          // Use list as well to keep files in same log order when written out again.
          loadedList.add(line);
          line = inReader.readLine();
        }
      } finally {
        inReader.close();
      }
    }

    return loadedList;
  }

  /**
   * Write out the files loaded last time.
   *
   * @param loadedList The list of file loaded last time.
   */
  private void writeExisting(List<String> loadedList) {

    for (String file: loadedList) {
      logWriter.println(file);
      numLoaded++;
    }

    logWriter.flush();
  }
}
