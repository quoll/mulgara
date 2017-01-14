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
import org.mulgara.query.QueryException;
import org.mulgara.server.SessionFactory;
import org.mulgara.server.driver.SessionFactoryFinder;
import org.mulgara.util.Rmi;
import org.mulgara.util.StackTrace;
import org.mulgara.util.TempDir;

// third party packages
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Java 2 standard packages
import java.net.URL;
import java.util.Vector;
import java.net.*;
import java.io.File;
import java.io.FileOutputStream;

// third party packages
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator; // Soap packages

// Soap packages
import org.apache.soap.Constants;
import org.apache.soap.Fault;
import org.apache.soap.rpc.*;

// emory util package
import edu.emory.mathcs.util.remote.io.*;
import edu.emory.mathcs.util.remote.io.server.impl.*;

/**
 * Unit test for {@link ItqlInterpreterBeanUnitTest}.
 *
 * @created 2001-11-19
 *
 * @author Tate Jones
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 05:13:59 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ItqlInterpreterBeanUnitTest extends TestCase {

  //
  // Members
  //

  /** The category to send logging info to */
  private static final Logger log = Logger.getLogger(ItqlInterpreterBeanUnitTest.class);

  /** Description of the Field */
  private ItqlInterpreterBean bean = null;

  /** host name of server */
  private static String hostName = System.getProperty("host.name", "localhost");

  /** an example model */
  private static String testModel = "rmi://" + hostName + "/server1#itqlmodel";

  /** Data directory for test files */
  private static String dataDirectory = System.getProperty("cvs.root") + "/data";

  static {
    if ( System.getProperty("os.name").indexOf("Windows") >= 0 ) {
      dataDirectory = "/"+dataDirectory.replace('\\','/');
    }
  }

  /** a temp directory location */
  private static File tmpDirectory = TempDir.getTempDir();

  //
  // Public API
  //

  /**
   * Constructs a new ItqlInterpreter unit test.
   *
   * @param name the name of the test
   */
  public ItqlInterpreterBeanUnitTest(String name) {

    // delegate to super class constructor
    super(name);

    // load the logging configuration
    try {
      DOMConfigurator.configure(System.getProperty("cvs.root") + "/log4j-conf.xml");
    } catch (FactoryConfigurationError fce) {
      log.error("Unable to configure logging service from XML configuration file");
    }

  }

  /**
   * Returns a test suite containing the tests to be run.
   *
   * @return the test suite
   */
  public static TestSuite suite() {

    TestSuite suite = new TestSuite();

    suite.addTest(new ItqlInterpreterBeanUnitTest("testQuery1"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testQuery2"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testQuery3"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testAnswerIteration"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testCreateModel"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testLoadApi5"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testLoadApi6"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testLoadApi7"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testLoadApi8"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testLoadApi9"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testBackupApi1"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testBackupApi2"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testExportApi1"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testExportApi2"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testRestoreApi1"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testRoundTrip1"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testMultipleBeanTest"));
    suite.addTest(new ItqlInterpreterBeanUnitTest("testExplicitSession"));

    return suite;
  }

  /**
   * The main program for the ItqlInterpreterBeanUnitTest class
   *
   * @param args The command line arguments
   * @throws Exception General catch-all for exceptions thrown during the entire test suite
   */
  public static void main(String[] args) throws Exception {

    junit.textui.TestRunner.run(suite());
  }
  
  /**
   * Convert Windows line endings...
   */
  private static String convertLineEndings(String str) {
    String converted = str;
    if (System.getProperty("os.name").toLowerCase().indexOf("win") > -1) {
      converted = converted.replaceAll("\r\n", "\n");
    }
    return converted;
  }

  /**
   * Test the interpreter via a direct call and a SOAP call
   *
   * @throws Exception if the test fails
   */
  public void testQuery1() throws Exception {

    String queryString =
        "select $s $p $o from <rmi://" + hostName +
        "/server1#> where $s $p $o ;";

    String directAnswer = bean.executeQueryToString(queryString);
    directAnswer = convertLineEndings(directAnswer);
    String soapAnswer = this.executeSoapCall(queryString);

    assertEquals(
        "A basic SELECT SOAP iTQL result is not the same as a direct call",
        soapAnswer, directAnswer);
  }

  /**
   * A unit test for JUnit
   *
   * @throws Exception General catch-all for exceptions thrown during the test
   */
  public void testQuery2() throws Exception {

    String queryString = "create <rmi://" + hostName + "/server1#model> ;";
    String directAnswer = bean.executeQueryToString(queryString);
    directAnswer = convertLineEndings(directAnswer);
    String soapAnswer = this.executeSoapCall(queryString);

    assertEquals("A CREATE SOAP iTQL result is not the same as a direct call",
        soapAnswer, directAnswer);
  }

  /**
   * A unit test for JUnit
   *
   * @throws Exception General catch-all for exceptions thrown during the test
   */
  public void testQuery3() throws Exception {

    String queryString =
        "insert <http://google.blogspace.com/archives/000999836> <http://purl.org/rss/1.0/description> 'Google needs to stop sending it\\'s cookie and promise to only store aggregate data, with no connection between users and search terms. ; This issue was publically raised almost a year ago that Google still hasnt dealt with its inexcusable....' into <rmi://" +
        hostName + "/server1#model>;";

    String result =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<answer xmlns=\"http://mulgara.org/tql#\"><query><message>Successfully inserted statements into rmi://" +
        hostName + "/server1#model</message></query></answer>";

    String directAnswer = bean.executeQueryToString(queryString);
    directAnswer = convertLineEndings(directAnswer);
    String soapAnswer = this.executeSoapCall(queryString);

    if (log.isDebugEnabled()) {
      log.error("\nDIRECT:'" + directAnswer + "'");
      log.error("\nSOAP  :'" + soapAnswer + "'");
      log.error("\nEQUALS  :'" + soapAnswer.equals(directAnswer) + "'");
      log.error("\nSSTARTS  :'" + soapAnswer.startsWith(directAnswer) + "'");
      log.error("\nDSTARTS  :'" + directAnswer.startsWith(soapAnswer) + "'");
    }

    assertEquals("A insert SOAP iTQL result is not the same as a direct call",
        soapAnswer, directAnswer);
    assertEquals("Incorrect iTQL result found", soapAnswer, result);
  }

  /**
   * A unit test for JUnit. Tests that an Answer can be obtained and
   * iterated over and reset multiple times.
   *
   * @throws Exception General catch-all for exceptions thrown during the test
   */
  public void testAnswerIteration() throws Exception {

    //create model
    String model = "rmi://" + hostName + "/server1#answerModel";
    String query = "create <" + model + "> ;";
    bean.executeQueryToString(query);

    //load data
    String dataFile = "file:" + dataDirectory + "/numbers.rdf.gz";
    query = "load <" + dataFile + "> into <" + model + "> ;";
    //number of statements must be more than 1 page size (1000 statements)
    System.setProperty("mulgara.rmi.prefetchsize", "10");
    bean.executeQueryToString(query);
    bean.executeQueryToString(query);

    //get Answer (everything)
    query = "select $s $p $o from <" + model + "> where $s $p $o;";
    Answer result = bean.executeQuery(query);
    result.close();

    //iterate and re-iterate - ensuring rowCount is preserved
    try {
      result = bean.executeQuery(query);
      //determine the number of statements in the Answer
      long rowCount = result.getRowCount();
      long statementCount = 0;

      //number of times to iterate
      int NUM_ITERATIONS = 10;
      for (int i = 0; i < NUM_ITERATIONS; i++) {
        //count to the end of Answer
        while (result.next()) {
          statementCount++;
        }
        //ensure all statements were preserved
        assertEquals("Number of Statements in Answer has changed after " +
            "iteration: " + i + ". Answer: " + result.getClass().getName(),
            rowCount, statementCount);

        //reset
        result.beforeFirst();
        statementCount = 0;
      }
    } finally {
      try {
        result.close();
      } finally {
        //drop the model
        bean.executeQueryToString("drop <" + model + "> ;");
      }
    }
  }

  /**
   * Performs a query over the SOAP interface
   *
   * @param query The TQL query to execute over the interface.
   * @return The XML response to the call, as a string.
   * @throws Exception General catch-all for exceptions thrown during the call
   */
  public String executeSoapCall(String query) throws Exception {

    //URL url = new URL("http://" + hostName + ":8080/soap/servlet/rpcrouter");
    URL url = new URL("http://" + hostName + ":8080/webservices/services/ItqlBeanService");

    // Build the call.
    Call call = new Call();
    call.setTargetObjectURI("urn:Query");
    call.setMethodName("executeQueryToString");

    //call.setEncodingStyleURI(Constants.NS_URI_LITERAL_XML);
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);

    Vector<Parameter> params = new Vector<Parameter>();
    params.addElement(new Parameter("queryString", String.class, query,
        Constants.NS_URI_SOAP_ENC));
    call.setParams(params);

    // make the call: note that the action URI is empty because the
    // XML-SOAP rpc router does not need this. This may change in the
    // future.
    Response resp = call.invoke( /* router URL */
        url, /* actionURI */
        "");

    // Check the response.
    if (resp.generatedFault()) {

      Fault fault = resp.getFault();
      fail("Soap call has failed : Fault Code   = " + fault.getFaultCode() +
          " Fault String = " + fault.getFaultString());

      return fault.getFaultCode() + " - " + fault.getFaultString();
    } else {

      Parameter result = resp.getReturnValue();

      return result.getValue().toString();
    }
  }

  /**
   * Test the interpreter using a create statement. Executes the following
   * query: <pre>
   *   create &lt;mulgara://localhost/database&gt; ;
   * </pre> Expects results: ParserException
   *
   * @throws Exception if the test fails
   */
  public void testCreateModel() throws Exception {

    // log that we're executing the test
    log.debug("Starting create test 3");

    // create the statement
    String statement = "create <" + testModel + "> ;";

    // log the query we'll be sending
    log.debug("Executing statement : " + statement);

    String results = "";

    // execute the query
    bean.executeQuery(statement);
    results = bean.getLastMessage();

    // log the results
    log.debug("Received results : " + results);

    // log that we've completed the test
    log.debug("Completed create test 3");
  }

  /**
   * Test the interpreter using a load API locally
   *
   * @throws Exception if the test fails
   */
  public void testLoadApi5() throws Exception {

    // log that we're executing the test
    log.debug("Starting load API test 5");

    File source = new File(dataDirectory + "/ical.rdf");
    URI modelURI = new URI(testModel);

    // execute the load locally
    long statements = bean.load(source, modelURI);

    assertEquals("Incorrect number of statements inserted", 1482, statements);

  }

  /**
   * Test the interpreter using a load API locally
   *
   * @throws Exception if the test fails
   */
  public void testLoadApi6() throws Exception {

    // log that we're executing the test
    log.debug("Starting load API test 6");

    File source = new File(dataDirectory + "/camera.owl");
    URI modelURI = new URI(testModel);

    // execute the load locally
    long statements = bean.load(source, modelURI);

    assertEquals("Incorrect number of statements inserted", 103, statements);

  }

  /**
   * Test the interpreter using a load API locally
   *
   * @throws Exception if the test fails
   */
  public void testLoadApi7() throws Exception {

    boolean badFile = false;

    // log that we're executing the test
    log.debug("Starting load API test 7");

    File source = new File(dataDirectory + "/camera.owl.bad");
    URI modelURI = new URI(testModel);

    // execute the load locally
    try {
      bean.load(source, modelURI);
    } catch (QueryException ex) {
      badFile = true;
    }

    assertTrue("Excepting a bad file", badFile);

  }

  /**
   * Test the interpreter using a load API locally
   *
   * @throws Exception if the test fails
   */
  public void testLoadApi8() throws Exception {

    // log that we're executing the test
    log.debug("Starting load API test 8");

    File source = new File(dataDirectory + "/camera.n3");
    URI modelURI = new URI(testModel);

    // execute the load locally
    long statements = bean.load(source, modelURI);

    assertEquals("Incorrect number of statements inserted", 99, statements);

  }

  /**
   * Test the interpreter using a load API locally with a GZip file.
   *
   * @throws Exception if the test fails
   */
  public void testLoadApi9() throws Exception {

    try {

      // log that we're executing the test
      log.debug("Starting load API test 9");

      File source = new File(dataDirectory + "/numbers.rdf.gz");
      URI modelURI = new URI(testModel);

      // execute the load locally
      long statements = bean.load(source, modelURI);

      assertEquals("Incorrect number of statements inserted", 512, statements);
    } catch (Exception exception) {

      exception.printStackTrace();
      throw exception;
    }
  }

  /**
   * Test the interpreter using a backup API locally.
   * Expects the server to exist
   *
   * @throws Exception if the test fails
   */
  public void testBackupApi1() throws Exception {

    // log that we're executing the test
    log.debug("Starting backup API test 2");

    try {
      File file = new File(tmpDirectory, "server.gz");
      file.delete();
  
      URI serverURI = new URI("rmi://localhost/server1");
  
      bean.backup(serverURI, file);
  
      assertTrue("Excepting a backup file", file.exists());
    } catch (QueryException e) {
      System.err.println("Error processing query" + e);
      Throwable t = e.getCause();
      while (t != null) {
        System.err.println("Caused by: " + t + StackTrace.throwableToString(t));
        t = t.getCause();
      }
      throw e;
    }

  }

  /**
   * Test the interpreter using a backup API locally.
   * Expects the server to exist
   *
   * @throws Exception if the test fails
   */
  public void testBackupApi2() throws Exception {

    // log that we're executing the test
    log.debug("Starting backup API test 4");

    try {
      File file = new File(tmpDirectory, "server2.gz");
      file.delete();
  
      URI serverURI = new URI("rmi://localhost/server1");
  
      bean.backup(serverURI, new FileOutputStream(file));
  
      assertTrue("Excepting a backup file", file.exists());
    } catch (QueryException e) {
      System.err.println("Error processing query" + e);
      Throwable t = e.getCause();
      while (t != null) {
        System.err.println("Caused by: " + t + StackTrace.throwableToString(t));
        t = t.getCause();
      }
      throw e;
    }
  
  }

  /**
   * Test the interpreter using an export API locally.
   * Expects the test model to exist
   *
   * @throws Exception if the test fails
   */
  public void testExportApi1() throws Exception {

    // log that we're executing the test
    log.debug("Starting backup API test 1");

    File file = new File(tmpDirectory, "camera.rdf");
    file.delete();

    URI modelURI = new URI(testModel);

    bean.export(modelURI, file);

    assertTrue("Excepting a backup file", file.exists());

  }

  /**
   * Test the interpreter using an export API locally.
   * Expects the test model to exist
   *
   * @throws Exception if the test fails
   */
  public void testExportApi2() throws Exception {

    // log that we're executing the test
    log.debug("Starting backup API test 3");

    File file = new File(tmpDirectory, "camera2.rdf");
    file.delete();

    URI modelURI = new URI(testModel);

    bean.export(modelURI, new FileOutputStream(file));

    assertTrue("Excepting a backup file", file.exists());

  }

  /**
   * Test the interpreter using a restore API locally.
   * Expects the server to exist
   *
   * @throws Exception if the test fails
   */
  public void testRestoreApi1() throws Exception {

    // log that we're executing the test
    log.debug("Starting restore API test 1");

    try {
      File file = new File(tmpDirectory, "server2.gz");
  
      URI serverURI = new URI("rmi://localhost/server1");
  
      bean.restore(file.toURI().toURL().openStream(), serverURI);
    } catch (QueryException e) {
      System.err.println("Error processing query" + e);
      Throwable t = e.getCause();
      while (t != null) {
        System.err.println("Caused by: " + t + StackTrace.throwableToString(t));
        t = t.getCause();
      }
      throw e;
    }

  }

  /**
   * Test a round trip of backup and restore
   *
   * This will test relative and absolute URIs to the server host name.
   *
   * @throws Exception if the test fails
   */
  public void testRoundTrip1() throws Exception {

    try {
      // log that we're executing the test
      log.debug("Starting round trip test 1");
  
      URI serverURI = new URI("rmi://" + hostName + "/server1");
  
      // test the output
      String select = "select $o from <" + testModel + "> " +
          "where <rmi://" + hostName +
          "/server1> <http://purl.org/dc/elements/1.1/creator> $o or " +
          " <rmi://" + hostName +
          "/foobar> <http://purl.org/dc/elements/1.1/creator> $o or " +
          " <rmi://" + hostName +
          "/server1/foobar> <http://purl.org/dc/elements/1.1/creator> $o ;";
  
      // insert statements with a subject the same as the
      // server name
      String insert = "insert " +
          "<rmi://" + hostName +
          "/server1> <http://purl.org/dc/elements/1.1/creator> 'foo' " +
          "<rmi://" + hostName +
          "/foobar> <http://purl.org/dc/elements/1.1/creator> 'foobar' " +
          "<rmi://" + hostName +
          "/server1/foobar> <http://purl.org/dc/elements/1.1/creator> 'server1/foobar' " +
          " into <" + testModel + ">;";
  
      // insert the statement
      bean.executeQuery(insert);
  
      // select the statement
      Answer answer = bean.executeQuery(select);
      assertTrue("Excepting a answer before restore", answer != null);
      assertTrue("Excepting a single result and found :" +
          answer.getRowCount(), (answer.getRowCount() == 3));
  
      //backup the server
      File file = new File(tmpDirectory, "roundtrip.gz");
      file.delete();
      bean.backup(serverURI, new FileOutputStream(file));
      assertTrue("Excepting a backup file", file.exists());
  
      // restore the server
      bean.restore(file.toURI().toURL().openStream(), serverURI);
  
      // select the statement
      answer = bean.executeQuery(select);
      assertTrue("Excepting a answer after restore", answer != null);
      assertTrue("Excepting a single result and found :" +
          answer.getRowCount(), (answer.getRowCount() == 3));
    } catch (QueryException e) {
      System.err.println("Error processing query" + e);
      Throwable t = e.getCause();
      while (t != null) {
        System.err.println("Caused by: " + t + StackTrace.throwableToString(t));
        t = t.getCause();
      }
      throw e;
    }

  }

  /**
   * Test the multiple creations of the interpreterbean
   * Ensure the number of open files are not exceeded on OS.
   *
   */
  public void testMultipleBeanTest() {

    /*
         // keep a track of the number of session directories
         String sessionDir = testDir + File.separator +
                             "server1" + File.separator +
                             "sessions" + File.separator;

         System.out.println("Session dir is "+ sessionDir );

         // log that we're executing the test
         log.debug("Starting multiple bean test");


         // get the initial number of directories
         int initNumberOfSessionDirs = (new File(sessionDir)).list().length;
     log.debug("Initial number of session directories are "+initNumberOfSessionDirs);
     */

    String query = "select $s $p $o from <rmi://" + hostName +
        "/server1#> where $s $p $o;";

    // create a 100 beans
    // TODO: change back to 1000
    for (int count = 1; count < 100; count++) {
      ItqlInterpreterBean bean = null;
      Answer answer = null;
      try {
        // create a new bean.
        bean = new ItqlInterpreterBean();

        log.warn("Starting bean number " + count);

        // execute a basic query
        answer = bean.executeQuery(query);
        assertTrue("Failed to query system model", answer.getRowCount() > 0);
        answer.close();

      } catch (Exception ex) {
        log.error("Failed to create/query the " + count + " ItqlInterpreterBean", ex);
        System.err.println("Failed to create/query the " + count + " ItqlInterpreterBean");
        System.err.println("Exception: " + ex.getMessage());
        ex.printStackTrace();
        assertTrue("Failed to create/query the " + count + " ItqlInterpreterBean", false);
        break;
      } finally {
        bean.close();
      }
    }

    /*
         // get the end number of directories
         int endNumberOfSessionDirs = (new File(sessionDir)).list().length;
     log.debug("The end number of session directories are "+endNumberOfSessionDirs);

         // the init and end number of directories should be the same
     assertTrue("Expecting "+initNumberOfSessionDirs+" session to exist, but "+
               "found "+endNumberOfSessionDirs+" session directories",
               endNumberOfSessionDirs == initNumberOfSessionDirs);
     */
  }

  /**
   * Test giving ItqlInterpreterBean an explicit session.
   *
   * @throws Exception if the test fails
   */
  @SuppressWarnings("deprecation")
  public void testExplicitSession() throws Exception {

    // log that we're executing the test
    log.debug("Starting explicit session test");

    URI serverURI = new URI("rmi://" + hostName + "/server1");
    SessionFactory sessionFactory =
                      SessionFactoryFinder.newSessionFactory(serverURI, true);

    bean.close();
    bean = new ItqlInterpreterBean(sessionFactory.newSession(),
                                   sessionFactory.getSecurityDomain());

    // auto-commit = true
    bean.executeQuery(
        "insert <es:foo1> <es:bar1> 'foo' into <" + testModel + ">;");

    Answer answer = bean.executeQuery(
        "select $p $o from <" + testModel + "> " + "where <es:foo1> $p $o;");
    assertEquals("Expecting a single result", 1, answer.getRowCount());

    bean.executeQuery(
        "delete <es:foo1> <es:bar1> 'foo' from <" + testModel + ">;");

    answer = bean.executeQuery(
        "select $p $o from <" + testModel + "> " + "where <es:foo1> $p $o;");
    assertEquals("Expecting no results", 0, answer.getRowCount());

    // explicit tx with commit
    bean.beginTransaction("explicit-session-test-commit");

    bean.executeQuery(
        "insert <es:foo1> <es:bar1> 'foo' into <" + testModel + ">;");

    answer = bean.executeQuery(
        "select $p $o from <" + testModel + "> " + "where <es:foo1> $p $o;");
    assertEquals("Expecting a single result", 1, answer.getRowCount());

    bean.executeQuery(
        "delete <es:foo1> <es:bar1> 'foo' from <" + testModel + ">;");

    bean.commit("explicit-session-test-commit");

    answer = bean.executeQuery(
        "select $p $o from <" + testModel + "> " + "where <es:foo1> $p $o;");
    assertEquals("Expecting no results", 0, answer.getRowCount());

    // explicit tx with rollback
    bean.beginTransaction("explicit-session-test-rollback");

    bean.executeQuery(
        "insert <es:foo1> <es:bar1> 'foo' into <" + testModel + ">;");

    answer = bean.executeQuery(
        "select $p $o from <" + testModel + "> " + "where <es:foo1> $p $o;");
    assertEquals("Expecting a single result", 1, answer.getRowCount());

    bean.rollback("explicit-session-test-rollback");

    answer = bean.executeQuery(
        "select $p $o from <" + testModel + "> " + "where <es:foo1> $p $o;");
    assertEquals("Expecting no results", 0, answer.getRowCount());
  }


  // ItqlInt

  /**
   * Initialise members.
   *
   * @throws Exception if something goes wrong
   */
  protected void setUp() throws Exception {

    bean = new ItqlInterpreterBean();
  }

  protected void tearDown() throws Exception {
    if (bean != null) {
      try {
        bean.close();
      } finally {
        bean = null;
      }
    }
  }
}
