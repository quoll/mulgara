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

// Junit
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.net.*;

// Java
import java.util.*;

// Log4j
import org.apache.log4j.*;

// Ant
import org.apache.tools.ant.Project;
import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.query.Answer;

/**
 * An Ant Task to load RDF into a Mulgara database.
 *
 * @created 2002-11-07
 *
 * @author Ben Warren
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2002 <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ITQLCommandUnitTest extends TestCase {

  /** Log category */
  private final static Logger log = Logger.getLogger(ITQLCommandUnitTest.class);

  /** Description of the Field */
  String RDF_TYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

  /** Description of the Field */
  String MODEL_URI = "http://mulgara.org/mulgara#Model";

  /** Description of the Field */
  String JOURNAL_URI = "urn:medline:Journal";

  /** Description of the Field */
  ItqlInterpreterBean interpreter = null;

  /** Description of the Field */
  String testModel = null;

  /** Description of the Field */
  ITQLCommand command = null;

  /**
   * Public constructor.
   *
   * @param name Name of the test
   * @throws Exception Initialization was not possible.
   */
  public ITQLCommandUnitTest(String name) throws Exception {
    super(name);
    String hostName = InetAddress.getLocalHost().getCanonicalHostName();
    testModel = "rmi://" + hostName + "/server1#itqlcommand-test-model";
  }

  /**
   * Builds a test suite.
   *
   * @return A test suite.
   * @throws Exception Unable to create the suite
   */
  public static TestSuite suite() throws Exception {
    TestSuite suite = new TestSuite();
    suite.addTest(new ITQLCommandUnitTest("testAbortOnErrors"));
    suite.addTest(new ITQLCommandUnitTest("testIgnoreErrors"));
    return suite;
  }

  /**
   * Runs the tests.
   *
   * @param args The args.
   * @throws Exception The tests would not run
   */
  public static void main(String[] args) throws Exception {
    BasicConfigurator.configure();
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Set up for tests.
   *
   */
  public void setUp() throws Exception {
    interpreter = new ItqlInterpreterBean();
    try {
      Project project = new Project();
      project.init();
      command = new ITQLCommand();
      command.init();
      command.setProject(project);
    } catch (Exception e) {
      log.error("Unable to set up the curren test", e);
      e.printStackTrace();
      tearDown();
    }
  }

  /**
   * Test presenting credentials.
   *
   * @throws Exception Generic failed test condition.
   */
  public void testAbortOnErrors() throws Exception {

    // Set auto commit off
    ITQLQuery query1 = new ITQLQuery();
    query1.setQuery("set autocommit off ;");

    // Create a model
    ITQLQuery query2 = new ITQLQuery();
    query2.setQuery("create <" + testModel + "> ;");

    // Insert some data
    ITQLQuery query3 = new ITQLQuery();
    query3.setQuery("insert <http://test> <http://data> 'Hello World' into <" + testModel + "> ;");

    // Query the data
    ITQLQuery query4 = new ITQLQuery();
    query4.setQuery("select $s $p $o from <" + testModel + "> where $s $p $o ;");

    // Commit
    ITQLQuery query5 = new ITQLQuery();
    query5.setQuery("commit ;");

    // Bad query
    ITQLQuery query6 = new ITQLQuery();
    query6.setQuery("select $s $p $o from <" + testModel + "> ;");

    // Insert some more data
    ITQLQuery query7 = new ITQLQuery();
    query7.setQuery("insert <http://more> <http://data> ':-)' " + "into <" + testModel + "> ;");

    // Set auto commit on
    ITQLQuery query8 = new ITQLQuery();
    query8.setQuery("set autocommit on ;");

    command.setIgnoreErrors(false);
    command.addItqlquery(query1);
    command.addItqlquery(query2);
    command.addItqlquery(query3);
    command.addItqlquery(query4);
    command.addItqlquery(query5);
    command.addItqlquery(query6);
    command.addItqlquery(query7);
    command.addItqlquery(query8);
    command.execute();

    // Check number of errors
    assertEquals("Wrong number of errors", 1, command.getNumErrors());

    // Check number executed
    assertEquals("Wrong number of commands executed", 6, command.getNumExecuted());

    // Check for second lot of data
    Answer answer = (Answer)interpreter.executeQueryToList("select $o from <" + testModel + "> where $s $p $o;").get(0);

    if (answer.isUnconstrained()) fail("Result set was null");

    // There should be 1 row of data
    assertEquals("Wrong number of rows", 1, answer.getRowCount());
    answer.close();
  }

  /**
   * Test presenting credentials.
   *
   * @throws Exception Generic failed test condition
   */
  public void testIgnoreErrors() throws Exception {

    // Set auto commit off
    ITQLQuery query1 = new ITQLQuery();
    query1.setQuery("set autocommit off ;");

    // Create a model
    ITQLQuery query2 = new ITQLQuery();
    query2.setQuery("create <" + testModel + "> ;");

    // Insert some data
    ITQLQuery query3 = new ITQLQuery();
    query3.setQuery("insert <http://test> <http://data> 'Hello World' into <" + testModel + "> ;");

    // Query the data
    ITQLQuery query4 = new ITQLQuery();
    query4.setQuery("select $s $p $o from <" + testModel + "> where $s $p $o ;");

    // Commit
    ITQLQuery query5 = new ITQLQuery();
    query5.setQuery("commit ;");

    // Bad query
    ITQLQuery query6 = new ITQLQuery();
    query6.setQuery("select $s $p $o from <" + testModel + "> ;");

    // Insert some more data
    ITQLQuery query7 = new ITQLQuery();
    query7.setQuery("insert <http://more> <http://data> ':-)' " + "into <" + testModel + "> ;");

    // Set auto commit on
    ITQLQuery query8 = new ITQLQuery();
    query8.setQuery("set autocommit on ;");

    command.setIgnoreErrors(true);
    command.addItqlquery(query1);
    command.addItqlquery(query2);
    command.addItqlquery(query3);
    command.addItqlquery(query4);
    command.addItqlquery(query5);
    command.addItqlquery(query6);
    command.addItqlquery(query7);
    command.addItqlquery(query8);
    command.execute();

    // Check number of errors
    assertEquals("Wrong number of errors", 1, command.getNumErrors());

    // Check number executed
    assertEquals("Wrong number of commands executed", 8, command.getNumExecuted());

    // Check for second lot of data
    List<Object> resultList = interpreter.executeQueryToList("select $o from <" + testModel + "> where $s $p $o;");
    Answer answer = (Answer)resultList.get(0);

    if (answer.isUnconstrained()) fail("Result set was null");

    // There should be 2 rows of data
    assertEquals("Wrong number of rows", 2, answer.getRowCount());

    for (int i = 0; i < resultList.size(); i++) {
      Object a = resultList.get(i);
      if (a instanceof Answer) ((Answer)a).close();
    }
  }

  /**
   * Tear down after tests.
   */
  public void tearDown() throws Exception {

    if (interpreter != null) {
      try {
        interpreter.executeQueryToList("drop <" + testModel + ">;");
      } finally {
        try {
          interpreter.close();
        } finally {
          interpreter = null;
        }
      }
    }
  }
}
