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

package org.mulgara.itql;

import org.mulgara.query.Answer;

// Java 2 standard packages
// third party packages
// third party packages
import java.io.*;
import java.util.Random;
import java.util.Vector;

/**
 * Stress test the system by creating robot itql beans and
 * randomly execute tql queries and record the results
 *
 * Unit test for {@link ItqlInterpreterBean}.
 *
 * @created 2002-06-03
 *
 * @author Tate Jones
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:15 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ItqlInterpreterStressTest {

  /**
   * Description of the Field
   */
  @SuppressWarnings("unused")
  private boolean verbose = false;

  /**
   * Number of repetiions each robot executes the set of tql queries.
   */
  private int repetitions = 1000;

  /**
   * The number of itql bean robots to be created
   */
  private int stress = 50;

  /**
   * The maximum delay between tql executions
   */
  private int delay = 7000;

  /**
   * The minimum delay between tql executions
   */
  private int minDelay = 2000;

  /**
   * Period to wait until another robot is added
   */
  private int rampTime = 250;

  /**
   * The current number of robots active
   */
  private int robots = 100;

  /**
   * The file location of the tql commands
   */
  private String itqlLog = "itqlcmd.log";

  /**
   * The file location of the outputed results for queries
   */
  private String queryOutputFile = "query.csv";

  /**
   * The total time of the stress test
   */
  private long totalTime = 0;

  /**
   * The total number of tql executions across all robots
   */
  private long executions = 0;

  /**
   * Start time of the stress test
   */
  @SuppressWarnings("unused")
  private long startTime = 0;

  /**
   * Used to create random delay between tql executions
   */
  private Random rand = new Random();

  /**
   * Contains the list of queries to be executed
   */
  private Vector<String> queries = new Vector<String>();

  /**
   * Stream to read tql commands
   */
  private PrintWriter queryLog = null;

  /**
   * Stress test the system by creating robot itql beans and
   * randomly execute tql queries and record the results
   */
  public ItqlInterpreterStressTest() {

  }

  /**
   * METHOD TO DO
   *
   * @param args PARAMETER TO DO
   */
  public static void main(String[] args) {

    if ( args.length > 0 && (args[0].equals("-?") || args[0].equalsIgnoreCase("-h"))) {

      String lineSeparator = System.getProperty("line.separator");
      String usuage = "Stress test " + lineSeparator + "Usage: "+ lineSeparator +
          " [-s <number>] [-r <number>] [-w <milliseconds>] "+
          " [-wm <milliseconds>] [-ra <milliseconds>] [-i <file>] "+
          " [-o <file>]" + lineSeparator + lineSeparator +
          " -h display this help screen "+lineSeparator+
          " -s number of robots to ramp up to (stress level) (default 100)"+lineSeparator+
          " -r number of query repetitions for each robot to execute (default 1000)"+lineSeparator+
          " -w the maximum wait time between query executions per robot (default 7000 millisec)"+lineSeparator+
          " -wm the minimum wait time between query executions per robot (default 2000 millisec)"+lineSeparator+
          " -ra the ramp time for each new robot to be created (default 250 millisec)"+lineSeparator+
          " -i the input file containing the itql commands (default itqlcmd.log)"+lineSeparator+
          " -o output file containing the query performance results (default query.csv)"+lineSeparator;
      System.out.println( usuage );
    } else {
      try {
        new ItqlInterpreterStressTest().start(args);
        System.exit(0);
      } catch (Exception e) {
        System.out.println(e.getMessage());
        System.exit(1);
      }
    }
  }

  /**
   * METHOD TO DO
   *
   * @param args PARAMETER TO DO
   * @throws Exception EXCEPTION TO DO
   */
  protected void start(String[] args) throws Exception {

    for (int i = 0; i < args.length; i++) {

      if (args[i].equals("-v")) {
        verbose = true;
      } else if (args[i].equals("-r")) {
        repetitions = Integer.parseInt(args[i + 1]);
      } else if (args[i].equals("-s")) {
        stress = Integer.parseInt(args[i + 1]);
      } else if (args[i].equals("-w")) {
        delay = Integer.parseInt(args[i + 1]);
      } else if (args[i].equals("-wm")) {
        minDelay = Integer.parseInt(args[i + 1]);
      } else if (args[i].equals("-i")) {
        itqlLog = args[i + 1];
      } else if (args[i].equals("-ra")) {
        rampTime = Integer.parseInt(args[i + 1]);
      } else if (args[i].equals("-o")) {
        queryOutputFile = args[i + 1];
      }

    }

    // open the itql command log
    LineNumberReader itqlCommands = new LineNumberReader(new FileReader(itqlLog));

    // read the contents
    String query = itqlCommands.readLine();

    while (query != null) {
      queries.add(query);
      query = itqlCommands.readLine();
    }

    System.out.println("Settings");
    System.out.println("\tRepetitions :" + repetitions);
    System.out.println("\tStress (number of robots) :" + stress);
    System.out.println("\tRandom Delay up-to :" + delay);
    System.out.println("\tMin. Delay up-to :" + minDelay);
    System.out.println("\tNumber of queries :" + queries.size());
    System.out.println("\tRamp robot every :" + rampTime);
    System.out.println("\tQuery OutputFile :" + queryOutputFile);

    if (queries.size() == 0) {
      System.out.println("No queries to be tested - exiting");
    }

    if (queryOutputFile == null) {
      System.out.println("The output file has been supplied.  Use -o");
    }

    queryLog = new PrintWriter(new FileWriter(queryOutputFile), true);
    queryLog.println("Time\tRobot\tQuery Number\tQuery Time\tAverage Query Time ms" );

    ItqlRobot[] threads = new ItqlRobot[stress];

    for (int i = 0; i < stress; i++) {
      String name = "Robot" + String.valueOf(i);
      threads[i] = new ItqlRobot(name, queries, repetitions, delay, minDelay);
    }

    startTime = System.currentTimeMillis();

    for (int i = 0; i < stress; i++) {

      // start a robot
      System.out.println("Starting " + threads[i].getRobotName());
      threads[i].start();
      synchronized (queryLog) {
        robots++;
      }

      // wait a period time before adding a new robot
      try {
        Thread.sleep(rampTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    System.out.println("All robots have started....");

    for (int i = 0; i < stress; i++) {
      threads[i].join();
    }

    queryLog.close();
  }

  private class ItqlRobot extends Thread {

    private ItqlInterpreterBean itql = null;
    private String name = null;
    private Vector<String> queries = null;
    private int repetitions = 0;
    private int delay = 0;
    private int minDelay = 0;
    private int noOfQueries = 0;

    /**
     * TQL bean robot that will execute a collection of TQL queries
     *
     * @param name robot name identifier
     * @param queries a list of queries to execute
     * @param repetitions number of repeatitions
     * @param delay the maximum delay between executes
     * @param minDelay the minimum delay between executes
     */
    public ItqlRobot(String name, Vector<String> queries, int repetitions, int delay,
        int minDelay) {

      this.itql = new ItqlInterpreterBean();
      this.name = name;
      this.queries = queries;
      this.repetitions = repetitions;
      this.delay = delay;
      this.minDelay = minDelay;
      this.noOfQueries = queries.size();
    }

    /**
     * Obtain the current robot name
     *
     * @return String Current robot name
     */

    public String getRobotName() {

      return this.name;
    }

    private void executeQuery() {

      Answer answer = null;

      try {

        long start = System.currentTimeMillis();
        int queryNumber = rand.nextInt(noOfQueries);
        String query = queries.elementAt(queryNumber);
        answer = itql.executeQuery(query);
        if ( answer != null ) {
          System.out.print(".");
        } else {
          System.out.println();
          System.out.println(query );
        }

        long queryTime = (System.currentTimeMillis() - start);

        totalTime = totalTime + queryTime;
        executions++;

        synchronized (queryLog) {
          queryLog.println(System.currentTimeMillis()+"\t"+
                           this.getRobotName()+"\t"+
                           queryNumber + "\t" + queryTime + "\t"+
                           (totalTime / executions) );
        }
      } catch (Exception ex) {

        ex.printStackTrace();
      } finally {
        try {
          answer.close();
        } catch (Exception ex) {
          // do nothing
        }
      }
    }

    public void run() {

      try {
        for (int calls = 0; calls < repetitions; calls++) {

          try {

            sleep(rand.nextInt(delay - minDelay) + minDelay);
          } catch (InterruptedException e) {

            e.printStackTrace();
          }

          try {

            this.executeQuery();
          } catch (Exception ex) {

            ex.printStackTrace();
          }
        }

        System.out.println(this.name + " has completed");
      } finally {
        synchronized (queryLog) {
          robots--;
        }
        itql.close();
      }
    }
  }
}
