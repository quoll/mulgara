package org.mulgara.tools;

import org.mulgara.itql.*;
import org.mulgara.connection.*;
import org.mulgara.query.*;
import org.mulgara.query.operation.Command;

import org.apache.log4j.*;

import java.net.URI;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * This is a simple class that demonstrates using the TQL interface. This class creates
 * a connection to a default server on the local host, and issues a TQL query against
 * it. The output is simple text.
 *
 * Usage: java org.mulgara.tools.Tql [queryfile ...]
 *
 * The query files given on the command line each contain a TQL query. If no query files
 * are given, then the query is parsed from stdin.
 */
public class Tql {

  /**
   * A URI for the default server on the local host. This is a simple example, so the
   * connection to the host is not configurable.
   */
  private static final URI HOST = URI.create("rmi://localhost/server1");

  /**
   * Run a set of TQL queries against a local server.
   * @param args A list of filenames containing the queries to run.
   * @throws Exception Any kind of exception is just displayed on stderr, without any handling.
   */
  public static void main(String[] args) throws Exception {
    // set up basic logging
    logConfig();

    // Connect to the server
    ConnectionFactory factory = new ConnectionFactory();
    Connection conn = factory.newConnection(HOST);

    // get the list of queries
    Iterator<String> queryStrings;
    if (args.length == 0) queryStrings = getStdIn();
    else queryStrings = getFiles(args);

    // iterate over all the query strings
    while (queryStrings.hasNext()) {
      // parse the string into a Command object
      Command cmd = new TqlInterpreter().parseCommand(queryStrings.next());

      if (cmd instanceof Query) {
        // execute the query, and get back the answer
        Answer a = (Answer)conn.execute((Query)cmd);
        System.out.println("Result: " + cmd.getResultMessage());
        printAnswer(a);
        a.close();
      } else {
        // other command to be executed. Print the result object.
        Object result = conn.execute(cmd);
        System.out.println("Result: " + cmd.getResultMessage() + " <" + result + ">");
      }

      System.out.println("---");
    }

    // clean up the server connection
    conn.close();
  }

  /**
   * Iterate through an Answer and print each of the lines to STDOUT.
   * @param a The answer to print.
   */
  private static void printAnswer(Answer a) throws Exception {
    System.out.println("Showing " + a.getRowCount() + " results");
    int width = a.getNumberOfVariables();
    a.beforeFirst();
    while (a.next()) {
      for (int c = 0; c < width; c++) {
        Object o = a.getObject(c);
        if (o instanceof Answer) {
          System.out.println("[");
          printAnswer((Answer)o);
          System.out.print("] ");
        } else {
          System.out.print(toString(a.getObject(c)) + "    ");
        }
      }
      System.out.println();
    }
  }

  /**
   * Convert any parameter to a string, or "&lt;null&gt;" if no parameter is provided.
   * @param The object to print. May be <code>null</code>.
   * @return A string representation of o, or "&lt;null&gt;" if o is <code>null</code>.
   */
  private static String toString(Object o) {
    return (o == null) ? "<null>" : o.toString();
  }

  /**
   * Get a String iterator that returns the single string formed from STDIN.
   * @return An iterator for a singleton list that contains the contents of STDIN.
   */
  private static Iterator<String> getStdIn() throws IOException {
    return Collections.singletonList(loadToString(new InputStreamReader(System.in))).iterator();
  }

  /**
   * Get a String iterator that returns the contents of a list of files.
   * @param args A list of file names (absolute or relative) that contains text to read.
   * @return An iterator for strings that contain the context of the files from args.
   */
  private static Iterator<String> getFiles(String[] args) throws IOException {
    LinkedList<String> fileData = new LinkedList<String>();
    for (String f: args) fileData.add(loadToString(new FileReader(f)));
    return fileData.iterator();
  }
  
  /**
   * Reads the contents of a {@link java.io.Reader} into a string.
   * @param reader A Reader to be read to completion.
   * @return A string containing everything that could be retrieved from reader.
   */
  private static String loadToString(Reader reader) throws IOException {
    BufferedReader br = new BufferedReader(reader);
    StringBuffer data = new StringBuffer();
    String line;
    while (null != (line = br.readLine())) data.append(line).append("\n");
    return data.toString();
  }

  /**
   * Sets up simple logging of WARN level and above to go to the console.
   */
  private static void logConfig() {
    Logger rootLogger = Logger.getRootLogger();
    rootLogger.setLevel(Level.WARN);
    rootLogger.addAppender(new ConsoleAppender());
  }

}

