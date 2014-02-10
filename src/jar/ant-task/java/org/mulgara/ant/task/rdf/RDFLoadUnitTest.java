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

// Junit
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.net.*;

// Java
import java.util.*;

// Log4j
import org.apache.log4j.*;
import org.apache.tools.ant.BuildException;

// Ant
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.query.Answer;
import org.mulgara.util.TempDir;

/**
 * An Ant Task to load RDF into a Mulgara database.
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
public class RDFLoadUnitTest extends TestCase {

  /** Log category */
  @SuppressWarnings("unused")
  private final static Logger log = Logger.getLogger(RDFLoadUnitTest.class);

  /** URI string for rdf:type */
  String RDF_TYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

  /** URI string for the Mulgara model/graph type */
  String MODEL_URI = "http://mulgara.org/mulgara#Model";

  /** URI string for journals */
  String JOURNAL_URI = "urn:medline:Journal";

  /** The query object */
  ItqlInterpreterBean interpreter = null;

  /** The name of the server */
  String hostName = null;

  /** The name of the graph for testing */
  String testModel = null;

  /** The load object */
  RDFLoad load = null;

  /** The directory to work under */
  String baseDir = System.getProperty("basedir");

  /** A directory to work with for RDF files */
  File goodRDFDir =
      new File(baseDir + File.separator + "jxdata" + File.separator +
               "ant-tasks" + File.separator + "rdf-good");

  /** An erroneous directory to work with for RDF files */
  File badRDFDir =
      new File(baseDir + File.separator + "jxdata" + File.separator +
               "ant-tasks" + File.separator + "rdf-bad");

  /** An erroneous directory to work with for RDF files */
  File badRDFDir2 =
      new File(baseDir + File.separator + "jxdata" + File.separator +
               "ant-tasks" + File.separator + "rdf-bad2");

  /** File to write log info into */
  File logFile = new File(TempDir.getTempDir(), "rdfload-log.txt");

  /**
   * Public constructor.
   *
   * @param name The name of the test
   * @throws Exception Thrown if the test cannot be set up
   */
  public RDFLoadUnitTest(String name) throws Exception {
    super(name);
    hostName = InetAddress.getLocalHost().getCanonicalHostName();
    testModel = "rmi://" + hostName + "/server1#rdfload-test-model";
  }

  /**
   * Builds a test suite.
   *
   * @return A test suite.
   * @throws Exception Thrown if any of the tests cannot be set up
   */
  public static TestSuite suite() throws Exception {

    TestSuite suite = new TestSuite();
    suite.addTest(new RDFLoadUnitTest("testPresentCredentials"));  // no failure

    /* PHLOGISTON -- this test should be reenabled
         suite.addTest(new RDFLoadUnitTest("testCreateDropModel"));
     */
    
    suite.addTest(new RDFLoadUnitTest("testDirLoadNoLog"));  // failed
    suite.addTest(new RDFLoadUnitTest("testPathLoadWithLog"));  // failed
    suite.addTest(new RDFLoadUnitTest("testReadLog"));  // failed
    suite.addTest(new RDFLoadUnitTest("testDropModelWithLog"));  // failed
    suite.addTest(new RDFLoadUnitTest("test1ErrorNoLog"));  // failed
    suite.addTest(new RDFLoadUnitTest("test1ErrorWithLog"));  // failed
    suite.addTest(new RDFLoadUnitTest("test2ErrorsNoLog"));  // failed
    suite.addTest(new RDFLoadUnitTest("test2ErrorsWithLog"));  // failed
    suite.addTest(new RDFLoadUnitTest("test3ErrorsWithLog"));  // failed
    suite.addTest(new RDFLoadUnitTest("test1PerTrans"));  // failed
    suite.addTest(new RDFLoadUnitTest("test5PerTrans"));  // failed
    suite.addTest(new RDFLoadUnitTest("test2PerTransWith1Error"));  // failed  
    suite.addTest(new RDFLoadUnitTest("test2PerTransWith3Errors"));  // failed
    
    //suite.addTest(new RDFLoadUnitTest("test10PerTransIgnoreErrorsIn2Dirs"));  // failed
    
    return suite;
  }

  /**
   * Runs the tests.
   *
   * @param args The args.
   * @throws Exception Thrown if the tests cannot be run
   */
  public static void main(String[] args) throws Exception {

    String baseDir = System.getProperty("basedir");

    if (baseDir == null) {
      throw new RuntimeException("Could not get the 'basedir' system property");
    }

    //    String logConfigFile =
    //          baseDir+File.separator+"testdata"+File.separator+"log4j-test.xml";
    //
    //    DOMConfigurator.configure(logConfigFile);
    BasicConfigurator.configure();
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Set up for tests.
   */
  public void setUp() {
    interpreter = new ItqlInterpreterBean();
    boolean exceptionOccurred = true;
    try {
      Project project = new Project();
      project.init();
      load = new RDFLoad();
      load.init();
      load.setProject(project);

      exceptionOccurred = false;
    } finally {
      if (exceptionOccurred) tearDown();
    }
  }

  /**
   * Test presenting credentials.
   */
  public void testPresentCredentials() {

    // Good credentials
    load.setModeluri(URI.create("rmi://my.place.com/server1"));
    load.setDomainuri(URI.create("rmi://my.place.com/server1"));
    load.setDir(new File("/spare"));
    load.setUsername("user");
    load.setPassword("password");
    load.interpreter = interpreter;

    try {
      load.checkParams();
      load.presentCredentials();
    } catch (BuildException be) {
      fail("Unexpected exception whilst presenting credentials: " + be);
    }

    // Bad credentials
    load.setUsername("");

    try {
      load.checkParams();
      load.presentCredentials();
      fail("Credential presentation should have failed");
    } catch (BuildException be) {

    }

    // Missing credentials
    load.setDomainuri(null);

    try {
      load.checkParams();
      fail("Credential presentation should have failed");
    } catch (BuildException be) {

    }
  }

  /**
   * Test creating the model.
   *
   * @throws Exception General declaration for failed tests.
   */
  public void testCreateDropModel() throws Exception {

    // Good model
    load.setModeluri(URI.create(testModel));
    load.interpreter = interpreter;

    try {
      load.createModel();
    } catch (BuildException be) {
      fail("Unexpected exception whilst creating model: " + be);
    }

    // Check for the model
    List<Object> list =
        interpreter.executeQueryToList("select 'text' from <rmi://" + hostName +
                                       "/server1#> where " + " <" + testModel +
                                       "> <" + RDF_TYPE_URI + "> <" +
                                       MODEL_URI + ">;");

    if (list.get(0)instanceof String) {
      fail("Got exception instead of answer: " + list.get(0));
    }

    Answer answer = (Answer) list.get(0);
    answer.beforeFirst();

    if (answer.isUnconstrained() || !answer.next()) {
      fail("Graph was not created!");
    }
    assertEquals("Query should not return multiple answers", list.size(), 1);
    answer.close();

    // Drop the model
    load.dropModel();

    // Check for the model
    list =
        interpreter.executeQueryToList("select 'text' from <rmi://" + hostName +
                                       "/server1#> where " + " <" + testModel +
                                       "> <" + RDF_TYPE_URI + "> <" +
                                       MODEL_URI + ">;");

    answer = (Answer)list.get(0);
    answer.beforeFirst();

    if (answer.next()) {
      fail("Graph was not dropped!");
    }
    assertEquals("Query should not return multiple answers", list.size(), 1);
    answer.close();

    // Bad model
    load.setModeluri(URI.create("rmi://blah"));

    try {
      load.createModel();
      fail("Graph creation should have failed");
    } catch (BuildException be) {
    }

    // Missing model
    load.setModeluri(null);

    try {
      load.interpreter = null;
      load.execute();
      fail("Graph creation should have failed");
    } catch (BuildException be) {
    }
  }

  /**
   * Test a normal load with no logging using a directory.
   *
   * @throws Exception General declaration for failed tests.
   */
  public void testDirLoadNoLog() throws Exception {

    load.setModeluri(URI.create(testModel));
    load.setDir(goodRDFDir);
    load.setDropmodel(true);
    load.execute();

    Answer answer =
        (Answer)interpreter.executeQueryToList("select $s from <" + testModel +
                                               "> where $s <" + RDF_TYPE_URI +
                                               "> <" + JOURNAL_URI +
        ">;").get(0);

    if (answer.isUnconstrained()) {
      fail("The data did not load");
    }

    // Should be 5 loaded
    assertEquals("Not enough documents loaded!", 5, answer.getRowCount());
    answer.close();

    // Should be 0 errors
    assertEquals("Wrong number of errors!", 0, load.getNumErrors());
  }

  /**
   * Test a normal load with no logging using an rdf path.
   *
   * @throws Exception General declaration for failed tests.
   */
  public void testPathLoadWithLog() throws Exception {

    load.setModeluri(URI.create(testModel));
    load.setLogfile(logFile);

    Path path = load.createRdfpath();
    FileSet fileSet = new FileSet();
    fileSet.setDir(goodRDFDir);
    path.addFileset(fileSet);
    load.execute();

    Answer answer =
        (Answer)interpreter.executeQueryToList("select $s from <" + testModel +
                                                "> where $s <" + RDF_TYPE_URI +
                                                "> <" + JOURNAL_URI +
        ">;").get(0);

    if (answer.isUnconstrained()) {
      fail("The data did not load");
    }

    // Should be 5 loaded
    assertEquals("Not enough documents loaded!", 5, answer.getRowCount());
    answer.close();

    // Check log
    RDFLoadLog loadLog = new RDFLoadLog(logFile, true);
    loadLog.close();
    assertEquals("Not enough documents logged!", 5, loadLog.getNumLoaded());

    // Should be 0 errors
    assertEquals("Wrong number of errors!", 0, load.getNumErrors());
  }

  /**
   * Test reading of the log - no files should be loaded as they have been
   * logged as loaded. <p>
   *
   * This test relies on the test {@link #testPathLoadWithLog()} running first.
   * </p>
   *
   * @throws Exception General declaration for failed tests.
   */
  public void testReadLog() throws Exception {

    load.setModeluri(URI.create(testModel));
    load.setLogfile(logFile);

    Path path = load.createRdfpath();
    FileSet fileSet = new FileSet();
    fileSet.setDir(goodRDFDir);
    path.addFileset(fileSet);
    load.execute();

    assertEquals("No documents should have been loaded!", 0, load.getNumLoaded());
  }

  /**
   * Drop model is specified so the log should be ignored and all files should
   * be re-loaded. <p>
   *
   * This test relies on the test {@link #testReadLog()} running first. </p>
   *
   * @throws Exception General declaration for failed tests.
   */
  public void testDropModelWithLog() throws Exception {

    try {

      load.setModeluri(URI.create(testModel));
      load.setLogfile(logFile);
      load.setDropmodel(true);

      Path path = load.createRdfpath();
      FileSet fileSet = new FileSet();
      fileSet.setDir(goodRDFDir);
      path.addFileset(fileSet);
      load.execute();

      Answer answer =
          (Answer)interpreter.executeQueryToList("select $s from <" +
                                                 testModel +
                                                 "> where $s <" + RDF_TYPE_URI +
                                                 "> <" + JOURNAL_URI + ">;").get(0);

      if (answer.isUnconstrained()) {
        fail("The data did not load");
      }

      // Mulgara - should be 5 loaded
      assertEquals("Wrong number of documents loaded!", 5,
                   answer.getRowCount());
      answer.close();

      // Check loader
      assertEquals("Five documents should have been loaded!", 5,
                   load.getNumLoaded());
    } finally {
      // Get rid of the log file
      if (logFile.exists()) logFile.delete();
    }
  }

  /**
   * The load should stop after the first error.
   *
   * @throws Exception General declaration for failed tests.
   */
  public void test1ErrorNoLog() throws Exception {

    load.setModeluri(URI.create(testModel));
    load.setDir(badRDFDir);
    load.execute();

    Answer answer =
        (Answer)interpreter.executeQueryToList("select $s from <" + testModel +
                                               "> where $s <" + RDF_TYPE_URI +
                                               "> <" + JOURNAL_URI +
        ">;").get(0);

    if (answer.isUnconstrained()) {
      fail("The data did not load");
    }

    // Mulgara - should be 1 loaded
    assertEquals("Wrong number of documents loaded!", 1,
                 answer.getRowCount());
    answer.close();

    // Should be 1 error
    assertEquals("Wrong number of errors!", 1, load.getNumErrors());
  }

  /**
   * The load should stop after the first error.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void test1ErrorWithLog() throws Exception {

    try {

      load.setModeluri(URI.create(testModel));
      load.setLogfile(logFile);
      load.setDropmodel(true);

      Path path = load.createRdfpath();
      FileSet fileSet = new FileSet();
      fileSet.setDir(badRDFDir);
      path.addFileset(fileSet);
      load.execute();

      Answer answer =
          (Answer)interpreter.executeQueryToList("select $s from <" +
                                                 testModel +
                                                 "> where $s <" + RDF_TYPE_URI +
                                                 "> <" + JOURNAL_URI + ">;").get(0);

      if (answer.isUnconstrained()) {
        fail("The data did not load");
      }

      // Mulgara - should be 1 loaded
      assertEquals("Wrong number of documents loaded!", 1,
                   answer.getRowCount());
      answer.close();

      // Should be 1 error
      assertEquals("Wrong number of errors!", 1, load.getNumErrors());

      // Check log
      RDFLoadLog loadLog = new RDFLoadLog(logFile, true);
      assertEquals("Wrong number of documents in log!", 1,
                   loadLog.getNumLoaded());
    } finally {
      // Get rid of the log file
      if (logFile.exists()) logFile.delete();
    }
  }

  /**
   * The load should stop after the first 2 errors.
   *
   * @throws Exception General declaration for failed tests.
   */
  public void test2ErrorsNoLog() throws Exception {

    load.setModeluri(URI.create(testModel));
    load.setDir(badRDFDir);
    load.setMaxerrors(1);
    load.execute();

    Answer answer =
        (Answer)interpreter.executeQueryToList("select $s from <" + testModel +
                                               "> where $s <" + RDF_TYPE_URI +
                                               "> <" + JOURNAL_URI +
        ">;").get(0);

    if (answer.isUnconstrained()) {
      fail("The data did not load");
    }

    // Mulgara - should be 1 loaded
    assertEquals("Wrong number of documents loaded!", 1, answer.getRowCount());
    answer.close();

    // Should be 2 errors
    assertEquals("Wrong number of errors!", 2, load.getNumErrors());
  }

  /**
   * The load should stop after the first 2 errors.
   *
   * @throws Exception General declaration for failed tests.
   */
  public void test2ErrorsWithLog() throws Exception {

    // try {

      load.setModeluri(URI.create(testModel));
      load.setLogfile(logFile);
      load.setDropmodel(true);
      load.setMaxerrors(1);

      Path path = load.createRdfpath();
      FileSet fileSet = new FileSet();
      fileSet.setDir(badRDFDir);
      path.addFileset(fileSet);
      load.execute();

      Answer answer =
          (Answer)interpreter.executeQueryToList("select $s from <" +
                                                 testModel +
                                                 "> where $s <" + RDF_TYPE_URI +
                                                 "> <" + JOURNAL_URI + ">;").get(0);

      if (answer.isUnconstrained()) {
        fail("The data did not load");
      }

      // Mulgara - should be 1 loaded
      assertEquals("Wrong number of documents loaded!", 1, answer.getRowCount());
      answer.close();

      // Should be 2 errors
      assertEquals("Wrong number of errors!", 2, load.getNumErrors());

      //      // Check log
      //      RDFLoadLog loadLog = new RDFLoadLog(logFile, true);
      //      assertEquals("Wrong number of documents in log!", 1, loadLog.getNumLoaded());
    // } finally {
      //      if (logFile.exists()) logFile.delete();
    // }
  }

  /**
   * The load should skip the first file and then load the fifth file after the
   * 3 bad files. <p>
   *
   * This test depends on {@link #test2ErrorsWithLog()}. </p>
   *
   * @throws Exception General declaration for failed tests.
   */
  public void test3ErrorsWithLog() throws Exception {

    try {

      load.setModeluri(URI.create(testModel));
      load.setLogfile(logFile);
      load.setMaxerrors(3);

      Path path = load.createRdfpath();
      FileSet fileSet = new FileSet();
      fileSet.setDir(badRDFDir);
      path.addFileset(fileSet);
      load.execute();

      Answer answer =
          (Answer)interpreter.executeQueryToList("select $s from <" +
                                                 testModel +
                                                 "> where $s <" + RDF_TYPE_URI +
                                                 "> <" + JOURNAL_URI + ">;").get(0);

      if (answer.isUnconstrained()) {
        fail("The data did not load");
      }

      // Mulgara - should be 1 loaded as tear down would have dropped the model...
      assertEquals("Wrong number of documents loaded!", 1, answer.getRowCount());
      answer.close();

      // Should be 3 errors
      assertEquals("Wrong number of errors!", 3, load.getNumErrors());

      // Should be l loaded
      assertEquals("Wrong number of documents loaded!", 1, load.getNumLoaded());

      // Check log - 2 files in the log, 1 from the previous run
      RDFLoadLog loadLog = new RDFLoadLog(logFile, true);
      assertEquals("Wrong number of documents in log!", 2,
                   loadLog.getNumLoaded());
    } finally {
      // Get rid of the log file
      if (logFile.exists()) logFile.delete();
    }
  }

  /**
   * Test a normal load with 1 files per transaction.
   *
   * @throws Exception General declaration for failed tests.
   */
  public void test1PerTrans() throws Exception {

    try {

      load.setModeluri(URI.create(testModel));
      load.setDropmodel(true);
      load.setLogfile(logFile);
      load.setTransactionsize(1);

      Path path = load.createRdfpath();
      FileSet fileSet = new FileSet();
      fileSet.setDir(goodRDFDir);
      path.addFileset(fileSet);
      load.execute();

      Answer answer =
          (Answer)interpreter.executeQueryToList("select $s from <" +
                                                 testModel +
                                                 "> where $s <" + RDF_TYPE_URI +
                                                 "> <" + JOURNAL_URI + ">;").get(0);

      if (answer.isUnconstrained()) {
        fail("The data did not load");
      }

      // Should be 5 loaded
      assertEquals("Not enough documents loaded!", 5, answer.getRowCount());
      answer.close();

      // Check log
      RDFLoadLog loadLog = new RDFLoadLog(logFile, true);
      loadLog.close();
      assertEquals("Not enough documents logged!", 5, loadLog.getNumLoaded());

      // Should be 0 errors
      assertEquals("Wrong number of errors!", 0, load.getNumErrors());
    } finally {

      if (logFile.exists()) logFile.delete();
    }
  }

  /**
   * Test a normal load with 5 file per transaction.
   *
   * @throws Exception General declaration for failed tests.
   */
  public void test5PerTrans() throws Exception {

    try {

      load.setModeluri(URI.create(testModel));
      load.setLogfile(logFile);
      load.setTransactionsize(5);

      Path path = load.createRdfpath();
      FileSet fileSet = new FileSet();
      fileSet.setDir(goodRDFDir);
      path.addFileset(fileSet);
      load.execute();

      Answer answer =
          (Answer)interpreter.executeQueryToList("select $s from <" +
                                                 testModel +
                                                 "> where $s <" + RDF_TYPE_URI +
                                                 "> <" + JOURNAL_URI + ">;").get(0);

      if (answer.isUnconstrained()) {
        fail("The data did not load");
      }

      // Should be 5 loaded
      assertEquals("Not enough documents loaded!", 5, answer.getRowCount());
      answer.close();

      // Check log
      RDFLoadLog loadLog = new RDFLoadLog(logFile, true);
      loadLog.close();
      assertEquals("Not enough documents logged!", 5, loadLog.getNumLoaded());

      // Should be 0 errors
      assertEquals("Wrong number of errors!", 0, load.getNumErrors());
    } finally {
      if (logFile.exists()) logFile.delete();
    }
  }

  /**
   * Test a bad load with 2 files per transaction and 1 allowed error. No files
   * should be loaded.
   *
   * create <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * set autocommit off;
   * load <file:/spare/test/mulgara/jxdata/ant-tasks/rdf-bad/1.rdf> into <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * load <file:/spare/test/mulgara/jxdata/ant-tasks/rdf-bad/2.rdf> into <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * set autocommit off;
   * load <file:/spare/test/mulgara/jxdata/ant-tasks/rdf-bad/1.rdf> into <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * load <file:/spare/test/mulgara/jxdata/ant-tasks/rdf-bad/3.rdf> into <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * set autocommit on;
   * select $s from <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> where $s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <urn:medline:Journal>;
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void test2PerTransWith1Error() throws Exception {

    // try {

      load.setModeluri(URI.create(testModel));
      load.setLogfile(logFile);      
      load.setMaxerrors(1);
      load.setTransactionsize(2);

      Path path = load.createRdfpath();
      FileSet fileSet = new FileSet();
      fileSet.setDir(badRDFDir);
      path.addFileset(fileSet);
      load.execute();

      Answer answer =
          (Answer)interpreter.executeQueryToList("select $s from <" +
                                                 testModel +
                                                 "> where $s <" + RDF_TYPE_URI +
                                                 "> <" + JOURNAL_URI + ">;").get(0);

      if (answer.isUnconstrained()) {
        fail("Null result set");
      }

      // One should be loaded
      answer.beforeFirst();  
      assertTrue("No documents should be loaded, found "+answer.getRowCount(), !answer.next());
      answer.close();

      assertEquals("No documents should be loaded!", 0, load.getNumLoaded());

      //      // Check log
      //      RDFLoadLog loadLog = new RDFLoadLog(logFile, true);
      //      loadLog.close();
      //      assertEquals("No documents should be logged!", 0, loadLog.getNumLoaded());
      // Should be 2 errors
      assertEquals("Wrong number of errors!", 2, load.getNumErrors());
    // } finally {
      //      if (logFile.exists()) {
      //        logFile.delete();
      //      }
    // }
  }

  /**
   * Test a bad load with 2 files per transaction and 3 allowed errors. The
   * first and fifth files should be loaded in a transaction.
   *
   * create <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * set autocommit off;
   * load <file:/spare/test/mulgara/jxdata/ant-tasks/rdf-bad/1.rdf> into <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * load <file:/spare/test/mulgara/jxdata/ant-tasks/rdf-bad/2.rdf> into <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * set autocommit off;
   * load <file:/spare/test/mulgara/jxdata/ant-tasks/rdf-bad/1.rdf> into <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * load <file:/spare/test/mulgara/jxdata/ant-tasks/rdf-bad/3.rdf> into <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * set autocommit off;
   * load <file:/spare/test/mulgara/jxdata/ant-tasks/rdf-bad/1.rdf> into <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * load <file:/spare/test/mulgara/jxdata/ant-tasks/rdf-bad/4.rdf> into <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * set autocommit off;
   * load <file:/spare/test/mulgara/jxdata/ant-tasks/rdf-bad/1.rdf> into <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * load <file:/spare/test/mulgara/jxdata/ant-tasks/rdf-bad/5.rdf> into <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> ;
   * commit;
   * set autocommit on;
   * select $s from <rmi://kildall.bne.pisoftware.com/server1#rdfload-test-model> where $s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <urn:medline:Journal>;
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void test2PerTransWith3Errors() throws Exception {

    try {

      load.setModeluri(URI.create(testModel));
      load.setLogfile(logFile);
      load.setMaxerrors(3);
      load.setTransactionsize(2);

      Path path = load.createRdfpath();
      FileSet fileSet = new FileSet();
      fileSet.setDir(badRDFDir);
      path.addFileset(fileSet);
      load.execute();

      Answer answer =
          (Answer)interpreter.executeQueryToList("select $s from <" +
                                                 testModel +
                                                 "> where $s <" + RDF_TYPE_URI +
                                                 "> <" + JOURNAL_URI + ">;").get(0);

      if (answer.isUnconstrained()) {
        fail("The data did not load");
      }

      assertEquals("No documents should be loaded!", 2,
                   answer.getRowCount());
      answer.close();

      assertEquals("One document should be loaded this run!", 2,
                   load.getNumLoaded());

      // Check log
      RDFLoadLog loadLog = new RDFLoadLog(logFile, true);
      loadLog.close();
      assertEquals("Two documents should be logged!", 2, loadLog.getNumLoaded());

      // Should be 3 errors
      assertEquals("Wrong number of errors!", 3, load.getNumErrors());
    } finally {
      if (logFile.exists()) logFile.delete();
    }
  }

  /**
   * Test a bad load with 2 files per transaction and ignore errors. The files
   * are spread over 2 dirs. The first, fifth, sixth and 10 files should be
   * loaded in 2 seperate transactions.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void test10PerTransIgnoreErrorsIn2Dirs() throws Exception {

    try {

      load.setModeluri(URI.create(testModel));
      load.setLogfile(logFile);
      load.setIgnoreErrors(true);
      load.setTransactionsize(2);

      Path path = load.createRdfpath();
      FileSet fileSet1 = new FileSet();
      fileSet1.setDir(badRDFDir);

      FileSet fileSet2 = new FileSet();
      fileSet2.setDir(badRDFDir2);
      path.addFileset(fileSet1);
      path.addFileset(fileSet2);
      load.execute();

      Answer answer =
          (Answer)interpreter.executeQueryToList("select $s from <" +
                                                 testModel +
                                                 "> where $s <" + RDF_TYPE_URI +
                                                 "> <" + JOURNAL_URI + ">;").get(0);

      if (answer.isUnconstrained()) {
        fail("The data did not load");
      }

      // 4 should be loaded
      assertEquals("Not enough documents loaded!", 4, answer.getRowCount());
      answer.close();

      assertEquals("Four documents should be loaded!", 4, load.getNumLoaded());

      // Check log
      RDFLoadLog loadLog = new RDFLoadLog(logFile, true);
      loadLog.close();
      assertEquals("Four documents should be logged!", 4, loadLog.getNumLoaded());

      // Should be 3 errors
      assertEquals("Wrong number of errors!", 6, load.getNumErrors());
    } finally {
      if (logFile.exists()) logFile.delete();
    }
  }

  /**
   * Tear down after tests.
   */
  public void tearDown() {

    load = null;
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
