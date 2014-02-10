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

package org.mulgara.ant.task.itql;

import java.io.*;
import java.net.*;

// Java
import java.util.*;

// log4j
import org.apache.log4j.*;
import org.apache.log4j.xml.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

// Ant
import org.apache.tools.ant.Task;
import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;

/**
 * An Ant Task to execute ITQL commands. <p>
 *
 * The task has the following configuration options:
 * <ul>
 *   <li> ignoreerrors - If errors should be ignored (default is false). </li>
 *
 *   <li> logconfig - the log4j XML config file (Optional - if none specified,
 *   console logging will be used). </li>
 *   <li> itqlquery- nested ITQL queries to perform. </li>
 * </ul>
 * </p> <p>
 *
 * Example: </p> <p>
 *
 * <pre>
 *&lt;itqlcommand
 *  ignoreerrors="false"
 *  logconfig="${conf.dir}/log4j.xml"&gt;
 *  &lt;itqlquery query="create &amp;lt;rmi://${host.name}/server1#itqlcommand-test&amp;gt; ;"/&gt;
 *  &lt;itqlquery query="insert &lt;http://test&gt; &lt;http://message&gt; 'Hello World' into  &lt;rmi://${host.name}/server1#itqlcommand-test&gt; ;"/&gt;
 *  &lt;itqlquery query="select $$s $$p $$o from &lt;rmi://${host.name}/server1#itqlcommand-test&gt; where $$s $$p $$o ;"/&gt;
 *  &lt;itqlquery query="drop &amp;lt;rmi://${host.name}/server1#itqlcommand-test"&amp;gt; ;"/&gt;
 *&lt;/itqlcommand&gt;
 * </pre> </p>
 *
 * @created 2002-11-07
 *
 * @author Ben Warren
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2002 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ITQLCommand extends Task {

  /** The logging catgory */
  private final static Logger logger = Logger.getLogger(ITQLCommand.class);

  /** Get line separator. */
  private static final String eol = System.getProperty("line.separator");

  /** Ignore errors */
  protected boolean ignoreErrors = false;

  /** The log4j XML config file */
  protected File logConfig = null;

  /** The queries to execute */
  protected List<ITQLQuery> queryList = new ArrayList<ITQLQuery>();

  /** The ITQL interpreter */
  protected ItqlInterpreterBean interpreter = null;

  /** The number of files executed */
  private int numExecuted = 0;

  /** The number of errors */
  private int numErrors = 0;

  /**
   * Set if errors should be ignored (default is false).
   * @param ignore The new IgnoreErrors value
   */
  public void setIgnoreErrors(boolean ignore) {
    ignoreErrors = ignore;
  }


  /**
   * Set the log4j XML config file (Optional - if none specified, console
   * logging will be used).
   * @param config The log4j XML config file.
   */
  public void setLogconfig(File config) {
    logConfig = config;
  }


  /**
   * Get the number of files executed.
   * @return The number of files executed.
   */
  public int getNumExecuted() {
    return numExecuted;
  }


  /**
   * Get the number of errors that occurred.
   * @return The number of errors that occurred.
   */
  public int getNumErrors() {
    return numErrors;
  }


  /**
   * Initialise the task.
   * @throws BuildException on error.
   */
  public void init() throws BuildException {
    super.init();
  }


  /**
   * Add an ITQL query.
   * @param query The query to add.
   */
  public void addItqlquery(ITQLQuery query) {
    queryList.add(query);
  }


  /**
   * Executes the commands.
   * @throws BuildException on error.
   */
  public void execute() throws BuildException {
    try {
      log("Running with this config:" + eol + this.toString(), Project.MSG_DEBUG);
      setupLog();

      if (logger.isDebugEnabled()) logger.debug("Running with this config:" + eol + toString());

      interpreter = new ItqlInterpreterBean();

      executeQueries();

    } finally {
      if (interpreter != null) {
        try {
          interpreter.close();
        } finally {
          interpreter = null;
        }
      }
    }
  }


  /**
   * Overrides Object.toString()
   * @return A string representing this task.
   */
  public String toString() {

    StringBuffer buffer = new StringBuffer();
    buffer.append("[RDFLoad ");
    buffer.append(": ignoreerrors = " + ignoreErrors);
    buffer.append("]");

    return buffer.toString();
  }


  /**
   * Setup the log if required.
   * @throws BuildException on error.
   */
  @SuppressWarnings("deprecation")
  protected void setupLog() throws BuildException {
    // log4j logging
    if (logConfig != null) {
      try {
        DOMConfigurator.configure(logConfig.toURL());
      } catch (MalformedURLException me) {
        throw new BuildException("Could not configure log4j!", me);
      }
    } else {
      BasicConfigurator.configure();
      // Disable debug
      LogManager.getLoggerRepository().setThreshold(Level.OFF);
    }
  }


  /**
   * Get the result of a query as a string;
   * @param query The query to execute.
   * @return The result of the query as a String or Exception or null if there
   *         was no result.
   * @throws BuildException if the result was of an unexpected type.
   */
  protected Object getQueryResult(String query) {
    List<Object> list = interpreter.executeQueryToList(query, true);

    if (list.size() > 0) {

      Object answer = list.get(0);
      try {
        // close the rest of the list
        for (int i = 1; i < list.size(); i++) {
          Object a = list.get(i);
          if (a instanceof Answer) ((Answer)a).close();
        }
      } catch (TuplesException qe) {
        throw new BuildException("Received bad answer from the query: " + query);
      }

      return answer;
    } else {
      throw new BuildException("Did not get an answer for the query: " + query);
    }
  }


  /**
   * Executes the queries.
   *
   * @throws BuildException on error.
   */
  protected void executeQueries() throws BuildException {

    // Loop through the commands
    for (ITQLQuery itqlQuery: queryList) {

      String query = itqlQuery.getQuery();

      // Is there a query?
      if (query != null) {
        if (logger.isInfoEnabled()) logger.info("Executing query: " + query);

        log("Executing query: " + query, Project.MSG_INFO);

        // Do the query
        Object queryResult = getQueryResult(query);

        numExecuted++;

        // If result is a string
        if (queryResult instanceof String) {

          String string = (String)queryResult;

          if (string.startsWith("Successfully") ||
              string.startsWith("Auto commit") ||
              string.startsWith("Credential") ||
              string.startsWith("Completed execution of script")) {

            log("Result message: " + string, Project.MSG_INFO);

            if (logger.isInfoEnabled()) logger.info("Result message: " + string);
          } else {

            numErrors++;

            log("Exception executing query '" + query + "' : " + string, Project.MSG_ERR);
            logger.error("Exception executing query '" + query + "' : " + string);

            if (!ignoreErrors) break;
          }
        } else if (queryResult instanceof Answer) {
          // Result is an Answer

          Answer answer = (Answer) queryResult;

          try {
            if (!answer.isUnconstrained()) {

              log("Result set contained " + answer.getRowCount() + " row(s).", Project.MSG_INFO);

              if (logger.isInfoEnabled()) {
                logger.info("Result set contained " + answer.getRowCount() + "rows.");
              }
            } else {

              log("Query result contained null result set.", Project.MSG_INFO);
              logger.info("Query result contained null result set.");
            }

            answer.close();
          } catch (TuplesException qe) {

            log("Exception getting result set: " + qe, Project.MSG_ERR);

            if (logger.isDebugEnabled()) {

              logger.error("Exception getting result set.", qe);
            }
          }

        } else if (queryResult instanceof Exception) {
          // Result indicates a bad query

          Exception ex = (Exception) queryResult;
          StringWriter swriter = new StringWriter();
          PrintWriter pwriter = new PrintWriter(swriter);
          ex.printStackTrace(pwriter);
          numErrors++;

          log("Exception executing query '" + query + "' : " + swriter, Project.MSG_ERR);
          logger.error("Exception executing query '" + query + "' : " + swriter);

          if (!ignoreErrors) break;
        } else {
          throw new BuildException("Unknown object returned from query: " +
              queryResult.getClass().getName());
        }
      } else {
        // query result was not a string, answer, or exception: therefore unknown
        throw new BuildException("A query was null!");
      }
    }

    log("Executed " + numExecuted + " commands with " + numErrors + " errors.", Project.MSG_INFO);
    logger.info("Executed " + numExecuted + " commands with " + numErrors + " errors.");
  }
}
