package org.mulgara.tools;

import org.mulgara.sparql.*;
import org.mulgara.connection.*;
import org.mulgara.query.*;
import org.mulgara.query.rdf.Mulgara;

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
 * This is a simple class that demonstrates using the SPARQL interface. This class creates
 * a connection to a default server on the local host, and issues a SPARQL query against
 * it. The output is simple text.
 *
 * Usage: java org.mulgara.tools.Sparql [queryfile ...]
 *
 * The query files given on the command line each contain a SPARQL query. If no query files
 * are given, then the query is parsed from stdin.
 */
public class Sparql {

  /**
   * A URI for the default server on the local host. This is a simple example, so the
   * connection to the host is not configurable.
   */
  private static final URI HOST = URI.create("rmi://localhost/server1");

  /** A URI for the default graph in Mulgara. This one is always empty. */
  private static final URI EMPTY_GRAPH = URI.create(Mulgara.NULL_GRAPH);
  
  private final Iterator<String> queryStrings;
  private final ConnectionFactory factory;
  private Connection connection = null;
  
  private Sparql(Iterator<String> queryStrings) {
    this.queryStrings = queryStrings;
    this.factory = new ConnectionFactory();
  }

  void runQueries() throws Exception {
    connection = factory.newConnection(HOST);

    // iterate over all the query strings
    while (queryStrings.hasNext()) {
      // parse the string into a Query object
      SparqlInterpreter interpreter = new SparqlInterpreter();
      interpreter.setDefaultGraphUri(EMPTY_GRAPH);
      String queryStr = queryStrings.next();
      Query query = interpreter.parseQuery(queryStr);

      System.out.println("Executing query:\n" + queryStr);
      // execute the query, and get back the answer
      Answer a = connection.execute(query);
      // print the results
      System.out.println("Result: " + query.getResultMessage());
      printAnswer(a);
      a.close();

      System.out.println("---");
    }

    // clean up the server connection
    connection.dispose();
    connection = null;
  }
  
  void cancel() throws Exception {
    if (connection != null) {
      System.out.println("Canceling query...");
      connection.cancel();
      connection.dispose();
    }
  }
  
  /**
   * Run a set of SPARQL queries against a local server.
   * @param args A list of filenames containing the queries to run.
   * @throws Exception Any kind of exception is just displayed on stderr, without any handling.
   */
  public static void main(String[] args) throws Exception {
    // set up basic logging
    logConfig();

    // get the list of queries
    Iterator<String> queryStrings;
    if (args.length == 0) queryStrings = getStdIn();
    else queryStrings = getFiles(args);
    
    final Sparql sparql = new Sparql(queryStrings);
    
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      public void run() {
        try {
          sparql.cancel();
        } catch (Exception e) {
          System.err.println("Error canceling query");
          e.printStackTrace();
        }
      }
    }));
    
    sparql.runQueries();
  }

  /**
   * Iterate through an Answer and print each of the lines to STDOUT.
   * @param a The answer to print.
   */
  private static void printAnswer(Answer a) throws Exception {
    GraphAnswer graphAnswer = null;
    if (a instanceof GraphAnswer) {
      graphAnswer = (GraphAnswer)a.clone();
    }
    
    System.out.println("Showing " + a.getRowCount() + " results");
    int width = a.getNumberOfVariables();
    a.beforeFirst();
    while (a.next()) {
      for (int c = 0; c < width; c++) System.out.print(toString(a.getObject(c)) + "    ");
      System.out.println();
    }
    
    if (graphAnswer != null) {
      System.out.println("RDF/XML for GraphAnswer:");
      System.out.println("------------------------");
      RdfXmlEmitter.writeRdfXml(graphAnswer, System.out);
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

