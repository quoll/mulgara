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

package org.mulgara.content.mbox;

// Java 2 standard packages
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// Third party packages
import junit.framework.*; // JUnit
import org.apache.log4j.*; // Log4J
import org.jrdf.graph.*;
import org.jrdf.graph.mem.*;
import org.apache.log4j.xml.*; // Log4J
import EDU.oswego.cs.dl.util.concurrent.*;
// import org.jibble.simplewebserver.*;

// Locally written packages
import org.mulgara.content.*;
import org.mulgara.query.*;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.TestResolverSession;

/**
 * Unit testing for the Mulgara MBox Resolver.
 *
 * @created 2004-10-11
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:57:39 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MBoxStatementsUnitTest extends TestCase {

  /** Logger */
  private static Logger log = Logger.getLogger(MBoxStatementsUnitTest.class);

  /** Mutex to prevent concurrent execution of the tests */
  private static Mutex mutex;

  /**
   * Constructor as required by JUnit's TestCase.
   */
  public MBoxStatementsUnitTest(String name) {

    super(name);

    // load the logging configuration
    BasicConfigurator.configure();
    try {

      DOMConfigurator.configure(new URL(System.getProperty(
          "log4j.configuration")));
    } catch (MalformedURLException mue) {

      log.error(
          "Unable to configure logging service from XML configuration " +
          "file", mue);
    }
  }

  /**
   * Setup data which will be used by all the test cases in this class.
   * Overrides TestCase.setUp().
   */
  protected void setUp () {

    if (log.isDebugEnabled()) {

      log.debug("Acquired Mutex");
    }

    try {

      // Acquire the testing semaphore
      mutex.acquire();
    } catch (InterruptedException interruptedException) {

      // An error has occurred so report it
      log.error("Could not acquire semaphore for testing.",
                interruptedException);

      // Fail the test
      fail("Could not acquire semaphore for testing.");
    }
  }

  /**
   * Clean up data set up by setUp().
   * Overrides TestCase.tearDown().
   */
  protected void tearDown () {

    if (log.isDebugEnabled()) {

      log.debug("Released Mutex");
    }

    // Release the testing semaphore
    mutex.release();
  }


  /**
   * Creates a test suite with various different output and compares the output.
   */
  public static TestSuite suite() {

    TestSuite suite = new TestSuite();

    // Create the test semaphore
    mutex = new Mutex();

    // The test cases we want run during testing
    suite.addTest(new MBoxStatementsUnitTest("testConstruction"));
    suite.addTest(new MBoxStatementsUnitTest("testBeforeFirst"));
    suite.addTest(new MBoxStatementsUnitTest("testNext"));
    suite.addTest(new MBoxStatementsUnitTest("testNonMBox"));
    // suite.addTest(new MBoxStatementsUnitTest("testNonFileProtocol"));

    return suite;
  }

  /**
   * Test the constructor by ensuring no exceptions occur for a valid MP3 file.
   */
  public void testConstruction() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting construction test");
    }

    // Create the file handle to our test file
    File file = new File(new File(new File(System.getProperty("cvs.root")),
                                  "data" + File.separator + "mbox"), "Trash");

    // Containers for construction of content object
    Class<?> contentClass = null;
    Constructor<?> constructor = null;
    Content content = null;

    try {

      // Obtain the class for the FileContent object
      contentClass = Class.forName("org.mulgara.resolver.file.FileContent");
    } catch (ClassNotFoundException classNotFoundException) {

      // Log the exception
      log.error("Unable to find class [org.mulgara.resolver.file.FileContent] " +
                "for instantiation.", classNotFoundException);

      // Fail the test
      fail("Unable to find class [org.mulgara.resolver.file.FileContent] " +
           "for instantiation.");
    }

    try {

      // Obtain the Content object constructor
      constructor = contentClass.getConstructor(new Class[] {File.class});
    } catch (NoSuchMethodException noSuchMethodException) {

      // Log the exception
      log.error("Unable to find constructor for FileContent class.",
                noSuchMethodException);

      // Fail the test
      fail("Unable to find constructor for FileContent class.");
    }

    try {

      // Obtain a content handler for the test file
      content = (Content) constructor.newInstance(new Object[] {file});
    } catch (InstantiationException instantiationException) {

      // Log the exception
      log.error("Unable to construct an instance of FileContent class.",
                instantiationException);

      // Fail the test
      fail("Unable to construct an instance of FileContent class.");
    } catch (IllegalAccessException illegalAccessException) {

      // Log the exception
      log.error("Unable to access FileContent class for construction.",
                illegalAccessException);

      // Fail the test
      fail("Unable to access FileContent class for construction.");
    } catch (InvocationTargetException invocationTargetException) {

      // Log the exception
      log.error(
          "Exception occurred during construction of a FileContent object.",
          invocationTargetException);

      // Fail the test
      fail("Exception occurred during construction of a FileContent object.");
    }

    // Check that the content object was instantiated correctly
    assertTrue("Content object was not created properly.", content != null);

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Container for our statements object
    MBoxStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      // the mbox file
      statements = new MBoxStatements(content, resolverSession);
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Failed to parse out [" + content.getURIString() + "] into " +
                "statements.", notModifiedException);

      // Fail the test
      fail("Failed to parse out [" + content.getURIString() + "] into statements.");
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to parse out [" + content.getURIString() + "] into " +
                "statements.", tuplesException);

      // Fail the test
      fail("Failed to parse out [" + content.getURIString() + "] into statements.");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);
  }

  /**
   * Test that the correct number of statements are created by the
   * statements object.
   */
  public void testRowCount() {

    // Create the file handle to our test file
    File file = new File(new File(new File(System.getProperty("cvs.root")),
                                  "data" + File.separator + "mbox"), "Trash");

    // Containers for construction of content object
    Class<?> contentClass = null;
    Constructor<?> constructor = null;
    Content content = null;

    try {

      // Obtain the class for the FileContent object
      contentClass = Class.forName("org.mulgara.resolver.file.FileContent");
    } catch (ClassNotFoundException classNotFoundException) {

      // Log the exception
      log.error("Unable to find class [org.mulgara.resolver.file.FileContent] " +
                "for instantiation.", classNotFoundException);

      // Fail the test
      fail("Unable to find class [org.mulgara.resolver.file.FileContent] " +
           "for instantiation.");
    }

    try {

      // Obtain the Content object constructor
      constructor = contentClass.getConstructor(new Class[] {File.class});
    } catch (NoSuchMethodException noSuchMethodException) {

      // Log the exception
      log.error("Unable to find constructor for FileContent class.",
                noSuchMethodException);

      // Fail the test
      fail("Unable to find constructor for FileContent class.");
    }

    try {

      // Obtain a content handler for the test file
      content = (Content) constructor.newInstance(new Object[] {file});
    } catch (InstantiationException instantiationException) {

      // Log the exception
      log.error("Unable to construct an instance of FileContent class.",
                instantiationException);

      // Fail the test
      fail("Unable to construct an instance of FileContent class.");
    } catch (IllegalAccessException illegalAccessException) {

      // Log the exception
      log.error("Unable to access FileContent class for construction.",
                illegalAccessException);

      // Fail the test
      fail("Unable to access FileContent class for construction.");
    } catch (InvocationTargetException invocationTargetException) {

      // Log the exception
      log.error(
          "Exception occurred during construction of a FileContent object.",
          invocationTargetException);

      // Fail the test
      fail("Exception occurred during construction of a FileContent object.");
    }

    // Check that the content object was instantiated correctly
    assertTrue("Content object was not created properly.", content != null);

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Container for our statements object
    MBoxStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      // the mp3 file
      statements = new MBoxStatements(content, resolverSession);
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Failed to parse out [" + content.getURIString() + "] into " +
                "statements.", notModifiedException);

      // Fail the test
      fail("Failed to parse out [" + content.getURIString() + "] into statements.");
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to parse out [" + content.getURIString() + "] into " +
                "statements.", tuplesException);

      // Fail the test
      fail("Failed to parse out [" + content.getURIString() + "] into statements.");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);

    // Container for row count
    long numRows = 0;

    try {

      // Obtain the number of statements
      numRows = statements.getRowCount();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Unable to retrieve number of statements for [" +
                content.getURI() + "].", tuplesException);

      // Fail the test
      fail("Unable to retrieve number of statements for [" + content.getURIString() +
           "].");

    }

    if (log.isDebugEnabled()) {

      log.debug("Number of rows from mbox is " + numRows);
    }

    // Check that we have retrieved the correct number of statements
    assertTrue("Number of statements was not 358 as expected.  Was " + numRows +
               ".", numRows == 358);
  }

  /**
   * Test that beforeFirst().
   */
  public void testBeforeFirst() {

    // Create the file handle to our test file
    File file = new File(new File(new File(System.getProperty("cvs.root")),
                                  "data" + File.separator + "mbox"), "Trash");

    // Containers for construction of content object
    Class<?> contentClass = null;
    Constructor<?> constructor = null;
    Content content = null;

    try {

      // Obtain the class for the FileContent object
      contentClass = Class.forName("org.mulgara.resolver.file.FileContent");
    } catch (ClassNotFoundException classNotFoundException) {

      // Log the exception
      log.error("Unable to find class [org.mulgara.resolver.file.FileContent] " +
                "for instantiation.", classNotFoundException);

      // Fail the test
      fail("Unable to find class [org.mulgara.resolver.file.FileContent] " +
           "for instantiation.");
    }

    try {

      // Obtain the Content object constructor
      constructor = contentClass.getConstructor(new Class[] {File.class});
    } catch (NoSuchMethodException noSuchMethodException) {

      // Log the exception
      log.error("Unable to find constructor for FileContent class.",
                noSuchMethodException);

      // Fail the test
      fail("Unable to find constructor for FileContent class.");
    }

    try {

      // Obtain a content handler for the test file
      content = (Content) constructor.newInstance(new Object[] {file});
    } catch (InstantiationException instantiationException) {

      // Log the exception
      log.error("Unable to construct an instance of FileContent class.",
                instantiationException);

      // Fail the test
      fail("Unable to construct an instance of FileContent class.");
    } catch (IllegalAccessException illegalAccessException) {

      // Log the exception
      log.error("Unable to access FileContent class for construction.",
                illegalAccessException);

      // Fail the test
      fail("Unable to access FileContent class for construction.");
    } catch (InvocationTargetException invocationTargetException) {

      // Log the exception
      log.error(
          "Exception occurred during construction of a FileContent object.",
          invocationTargetException);

      // Fail the test
      fail("Exception occurred during construction of a FileContent object.");
    }

    // Check that the content object was instantiated correctly
    assertTrue("Content object was not created properly.", content != null);

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Container for our statements object
    MBoxStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      // the mbox file
      statements = new MBoxStatements(content, resolverSession);
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Failed to parse out [" + content.getURIString() + "] into " +
                "statements.", notModifiedException);

      // Fail the test
      fail("Failed to parse out [" + content.getURIString() + "] into statements.");
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to parse out [" + content.getURIString() + "] into " +
                "statements.", tuplesException);

      // Fail the test
      fail("Failed to parse out [" + content.getURIString() + "] into statements.");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);

    try {

      // Set the statements' cursor to be at the beginning
      statements.beforeFirst(new long[0], 0);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to set cursor to the beginning of the statements.",
                tuplesException);

      // Fail the test
      fail("Failed to set cursor to the beginning of the statements.");
    }
  }

  /**
   * Test the constructor by ensuring no exceptions occur for a valid MP3 file.
   */
  public void testNext() {

    // Create the file handle to our test file
    File file = new File(new File(new File(System.getProperty("cvs.root")),
                                  "data" + File.separator + "mbox"), "Trash");

    // Containers for construction of content object
    Class<?> contentClass = null;
    Constructor<?> constructor = null;
    Content content = null;

    try {

      // Obtain the class for the FileContent object
      contentClass = Class.forName("org.mulgara.resolver.file.FileContent");
    } catch (ClassNotFoundException classNotFoundException) {

      // Log the exception
      log.error("Unable to find class [org.mulgara.resolver.file.FileContent] " +
                "for instantiation.", classNotFoundException);

      // Fail the test
      fail("Unable to find class [org.mulgara.resolver.file.FileContent] " +
           "for instantiation.");
    }

    try {

      // Obtain the Content object constructor
      constructor = contentClass.getConstructor(new Class[] {File.class});
    } catch (NoSuchMethodException noSuchMethodException) {

      // Log the exception
      log.error("Unable to find constructor for FileContent class.",
                noSuchMethodException);

      // Fail the test
      fail("Unable to find constructor for FileContent class.");
    }

    try {

      // Obtain a content handler for the test file
      content = (Content) constructor.newInstance(new Object[] {file});
    } catch (InstantiationException instantiationException) {

      // Log the exception
      log.error("Unable to construct an instance of FileContent class.",
                instantiationException);

      // Fail the test
      fail("Unable to construct an instance of FileContent class.");
    } catch (IllegalAccessException illegalAccessException) {

      // Log the exception
      log.error("Unable to access FileContent class for construction.",
                illegalAccessException);

      // Fail the test
      fail("Unable to access FileContent class for construction.");
    } catch (InvocationTargetException invocationTargetException) {

      // Log the exception
      log.error(
          "Exception occurred during construction of a FileContent object.",
          invocationTargetException);

      // Fail the test
      fail("Exception occurred during construction of a FileContent object.");
    }

    // Check that the content object was instantiated correctly
    assertTrue("Content object was not created properly.", content != null);

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Container for our statements object
    MBoxStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      // the mbox file
      statements = new MBoxStatements(content, resolverSession);
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Failed to parse out [" + content.getURIString() + "] into " +
                "statements.", notModifiedException);

      // Fail the test
      fail("Failed to parse out [" + content.getURIString() + "] into statements.");
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to parse out [" + content.getURIString() + "] into " +
                "statements.", tuplesException);

      // Fail the test
      fail("Failed to parse out [" + content.getURIString() + "] into statements.");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);

    try {

      // Set the statements' cursor to be at the beginning
      statements.beforeFirst(new long[0], 0);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to set cursor to the beginning of the statements.",
                tuplesException);

      // Fail the test
      fail("Failed to set cursor to the beginning of the statements.");
    }

    // Assume we do not have any more statements
    boolean hasNext = false;

    try {

      // Retrieve the next statement and store the success value
      hasNext = statements.next();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to retrieve the next statement in the list.",
                tuplesException);

      // Fail the test
      fail("Failed to retrieve the next statement in the list.");
    }

    // Check that there was indeed a next statement
    assertTrue("Statements did not contain any elements as expected.", hasNext);

    if (log.isDebugEnabled()) {

      try {

        log.debug("Globalized values of first entry: [" +
                  resolverSession.globalize(statements.getColumnValue(
            MBoxStatements.SUBJECT)) + ", " +
                  resolverSession.globalize(statements.getColumnValue(
            MBoxStatements.PREDICATE)) + ", " +
                  resolverSession.globalize(statements.getColumnValue(
            MBoxStatements.OBJECT)) + "]");
      } catch (TuplesException tuplesException) {

        // Ignore the exception as we are debugging
      } catch (GlobalizeException globalizeException) {

        // Ignore the exception as we are debugging
      }
    }

    // Containers for our node ids
    long subject = 0;
    long predicate = 0;
    long object = 0;

    try {

      // Get the subject node id
      subject = statements.getColumnValue(MBoxStatements.SUBJECT);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error(
          "Failed to retrieve the subject node for first entry in statements.",
          tuplesException);

      // Fail the test
      fail(
          "Failed to retrieve the subject node for first entry in statements.");
    }

    try {

      // Get the predicate node id
      predicate = statements.getColumnValue(MBoxStatements.PREDICATE);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error(
          "Failed to retrieve the predicate node for first entry in statements.",
          tuplesException);

      // Fail the test
      fail(
          "Failed to retrieve the predicate node for first entry in statements.");
    }

    try {

      // Get the object node id
      object = statements.getColumnValue(MBoxStatements.OBJECT);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error(
          "Failed to retrieve the object node for first entry in statements.",
          tuplesException);

      // Fail the test
      fail(
          "Failed to retrieve the object node for first entry in statements.");
    }

    // Container for our globalized nodes
    Node subjectNode = null;
    Node predicateNode = null;
    Node objectNode = null;

    try {

      // Get the subject node
      subjectNode = resolverSession.globalize(subject);
    } catch (GlobalizeException globalizeException) {

      // Log the exception
      log.error("Failed to globalize subject node.", globalizeException);

      // Fail the test
      fail("Failed to globalize subject node.");
    }

    // Check that the node was globalized correctly
    assertTrue("First statement's subject node was unexpectedly null.",
               subjectNode != null);

    // Check that we have the right value for the node
    assertTrue("First statement's subject node was not the expected value, " +
               "was [" + subjectNode.toString() + "]",
               subjectNode instanceof BlankNodeImpl);

    try {

      // Get the predicate node
      predicateNode = resolverSession.globalize(predicate);
    } catch (GlobalizeException globalizeException) {

      // Log the exception
      log.error("Failed to globalize predicate node.", globalizeException);

      // Fail the test
      fail("Failed to globalize predicate node.");
    }

    // Check that the node was globalized correctly
    assertTrue("First statement's predicate node was unexpectedly null.",
               subjectNode != null);

    // Check that we have the right value for the node
    assertTrue("First statement's predicate node was not the expected value, " +
               "was [" + predicateNode.toString() + "]",
               predicateNode.toString().equals(
               "http://xmlns.filsa.net/emir/0.2/#subject"));

    try {

      // Get the object node
      objectNode = resolverSession.globalize(object);
    } catch (GlobalizeException globalizeException) {

      // Log the exception
      log.error("Failed to globalize object node.", globalizeException);

      // Fail the test
      fail("Failed to globalize object node.");
    }

    // Check that the node was globalized correctly
    assertTrue("First statement's object node was unexpectedly null.",
               subjectNode != null);

    // Check that we have the right value for the node
    assertTrue("First statement's object node was not the expected value, " +
               "was [" + objectNode.toString() + "]",
               objectNode.toString().equals(
               "\"[SPAM] Lose your weight. New weightloss loses up to 19%.\""));
    }

  /**
   * Test the constructor by ensuring an exception occurs for an invalid MBox
   * file.
   */
  public void testNonMBox() {

    // Create the file handle to our test file
    File file = new File(new File(new File(System.getProperty("cvs.root")),
                                  "data"), "camera.owl");

    // Containers for construction of content object
    Class<?> contentClass = null;
    Constructor<?> constructor = null;
    Content content = null;

    try {

      // Obtain the class for the FileContent object
      contentClass = Class.forName("org.mulgara.resolver.file.FileContent");
    } catch (ClassNotFoundException classNotFoundException) {

      // Log the exception
      log.error("Unable to find class [org.mulgara.resolver.file.FileContent] " +
                "for instantiation.", classNotFoundException);

      // Fail the test
      fail("Unable to find class [org.mulgara.resolver.file.FileContent] " +
           "for instantiation.");
    }

    try {

      // Obtain the Content object constructor
      constructor = contentClass.getConstructor(new Class[] {File.class});
    } catch (NoSuchMethodException noSuchMethodException) {

      // Log the exception
      log.error("Unable to find constructor for FileContent class.",
                noSuchMethodException);

      // Fail the test
      fail("Unable to find constructor for FileContent class.");
    }

    try {

      // Obtain a content handler for the test file
      content = (Content) constructor.newInstance(new Object[] {file});
    } catch (InstantiationException instantiationException) {

      // Log the exception
      log.error("Unable to construct an instance of FileContent class.",
                instantiationException);

      // Fail the test
      fail("Unable to construct an instance of FileContent class.");
    } catch (IllegalAccessException illegalAccessException) {

      // Log the exception
      log.error("Unable to access FileContent class for construction.",
                illegalAccessException);

      // Fail the test
      fail("Unable to access FileContent class for construction.");
    } catch (InvocationTargetException invocationTargetException) {

      // Log the exception
      log.error(
          "Exception occurred during construction of a FileContent object.",
          invocationTargetException);

      // Fail the test
      fail("Exception occurred during construction of a FileContent object.");
    }

    // Check that the content object was instantiated correctly
    assertTrue("Content object was not created properly.", content != null);

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Container for our statements object
    @SuppressWarnings("unused")
    MBoxStatements statements = null;

    // Boolean to show we failed to initialise (assumed false)
    boolean failed = false;

    try {

      // Construct a new statements object which will represent the content of
      // the non mbox file
      statements = new MBoxStatements(content, resolverSession);
    } catch (NotModifiedException notModifiedException) {

      // Set that we have failed during construction
      failed = true;
    } catch (TuplesException tuplesException) {

      // Set that we have failed during construction
      failed = true;
    }

    // Check that we have failed properly
    assertTrue("Statements object was created for non-mbox file.", failed);
  }

  /**
   * Test that statements are still generated for http protocol files.
   * TODO: Need a new web server
   */
  /*
  public void testNonFileProtocol() {

    // Container for our webserver
    SimpleWebServer server = null;

    try {

      // Create a webserver for our test data
      server = new SimpleWebServer(new File(new File(System.getProperty(
                                                     "cvs.root")),
                                                     "data" + File.separator +
                                                     "mbox"), 8090);
    } catch (IOException ioException) {

      // Log the error
      log.error("Failed to initialise server for remote file testing.",
                ioException);

      // Fail the test
      fail("Failed to initialise server for remote file testing.");
    }

    // Container for our mbox URL
    URL url = null;

    try {

      // Create the URL for our mbox
      url = new URL("http://localhost:8090/Trash");
    } catch (MalformedURLException malformedURLException) {

      // Log the error
      log.error("Failed to create mbox URL because it was malformed.",
                malformedURLException);

      // Faile the test
      fail("Failed to create mbox URL because it was malformed.");
    }

    // Containers for construction of content object
    Class contentClass = null;
    Constructor constructor = null;
    Content content = null;

    try {

      // Obtain the class for the FileContent object
      contentClass = Class.forName("org.mulgara.resolver.http.HttpContent");
    } catch (ClassNotFoundException classNotFoundException) {

      // Log the exception
      log.error("Unable to find class [org.mulgara.resolver.http.HttpContent] " +
                "for instantiation.", classNotFoundException);

      // Fail the test
      fail("Unable to find class [org.mulgara.resolver.http.HttpContent] " +
           "for instantiation.");
    }

    try {

      // Obtain the Content object constructor
      constructor = contentClass.getConstructor(new Class[] {URL.class});
    } catch (NoSuchMethodException noSuchMethodException) {

      // Log the exception
      log.error("Unable to find constructor for FileContent class.",
                noSuchMethodException);

      // Fail the test
      fail("Unable to find constructor for FileContent class.");
    }

    try {

      // Obtain a content handler for the test file
      content = (Content) constructor.newInstance(new Object[] {url});
    } catch (InstantiationException instantiationException) {

      // Log the exception
      log.error("Unable to construct an instance of HttpContent class.",
                instantiationException);

      // Fail the test
      fail("Unable to construct an instance of HttpContent class.");
    } catch (IllegalAccessException illegalAccessException) {

      // Log the exception
      log.error("Unable to access HttpContent class for construction.",
                illegalAccessException);

      // Fail the test
      fail("Unable to access HttpContent class for construction.");
    } catch (InvocationTargetException invocationTargetException) {

      // Log the exception
      log.error(
          "Exception occurred during construction of a HttpContent object.",
          invocationTargetException);

      // Fail the test
      fail("Exception occurred during construction of a HttpContent object.");
    }

    // Check that the content object was instantiated correctly
    assertTrue("Content object was not created properly.", content != null);

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Container for our statements object
    MBoxStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      // the mbox file
      statements = new MBoxStatements(content, resolverSession);
    } catch (NotModifiedException notModifiedException) {

      // Log the exception
      log.error("Failed to parse out [" + content.getURIString() + "] into " +
                "statements.", notModifiedException);

      // Fail the test
      fail("Failed to parse out [" + content.getURIString() + "] into statements.");
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to parse out [" + content.getURIString() + "] into " +
                "statements.", tuplesException);

      // Fail the test
      fail("Failed to parse out [" + content.getURIString() + "] into statements.");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);

    try {

      // Set the statements' cursor to be at the beginning
      statements.beforeFirst(new long[0], 0);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to set cursor to the beginning of the statements.",
                tuplesException);

      // Fail the test
      fail("Failed to set cursor to the beginning of the statements.");
    }

    // Assume we do not have any more statements
    boolean hasNext = false;

    try {

      // Retrieve the next statement and store the success value
      hasNext = statements.next();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to retrieve the next statement in the list.",
                tuplesException);

      // Fail the test
      fail("Failed to retrieve the next statement in the list.");
    }

    // Check that there was indeed a next statement
    assertTrue("Statements did not contain any elements as expected.", hasNext);

    if (log.isDebugEnabled()) {

      try {

        log.debug("Globalized values of first entry: [" +
                  resolverSession.globalize(statements.getColumnValue(
            statements.SUBJECT)) + ", " +
                  resolverSession.globalize(statements.getColumnValue(
            statements.PREDICATE)) + ", " +
                  resolverSession.globalize(statements.getColumnValue(
            statements.OBJECT)) + "]");
      } catch (TuplesException tuplesException) {

        // Ignore the exception as we are debugging
      } catch (GlobalizeException globalizeException) {

        // Ignore the exception as we are debugging
      }
    }

    // Containers for our node ids
    long subject = 0;
    long predicate = 0;
    long object = 0;

    try {

      // Get the subject node id
      subject = statements.getColumnValue(statements.SUBJECT);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error(
          "Failed to retrieve the subject node for first entry in statements.",
          tuplesException);

      // Fail the test
      fail(
          "Failed to retrieve the subject node for first entry in statements.");
    }

    try {

      // Get the predicate node id
      predicate = statements.getColumnValue(statements.PREDICATE);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error(
          "Failed to retrieve the predicate node for first entry in statements.",
          tuplesException);

      // Fail the test
      fail(
          "Failed to retrieve the predicate node for first entry in statements.");
    }

    try {

      // Get the object node id
      object = statements.getColumnValue(statements.OBJECT);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error(
          "Failed to retrieve the object node for first entry in statements.",
          tuplesException);

      // Fail the test
      fail(
          "Failed to retrieve the object node for first entry in statements.");
    }

    // Container for our globalized nodes
    Node subjectNode = null;
    Node predicateNode = null;
    Node objectNode = null;

    try {

      // Get the subject node
      subjectNode = resolverSession.globalize(subject);
    } catch (GlobalizeException globalizeException) {

      // Log the exception
      log.error("Failed to globalize subject node.", globalizeException);

      // Fail the test
      fail("Failed to globalize subject node.");
    }

    // Check that the node was globalized correctly
    assertTrue("First statement's subject node was unexpectedly null.",
               subjectNode != null);

    // Create a temporary cache directory handle where the file should be located
    //File cache = new File(System.getProperty("java.io.tmpdir") + File.separator +
    //                    "resolvercache" + File.separator +
    //                    "OC Remix - Chrono_Trigger_600_AD_in_Piano.mp3");

    try {

       // Get the subject node
       subjectNode = resolverSession.globalize(subject);
     } catch (GlobalizeException globalizeException) {

       // Log the exception
       log.error("Failed to globalize subject node.", globalizeException);

       // Fail the test
       fail("Failed to globalize subject node.");
     }

     // Check that the node was globalized correctly
     assertTrue("First statement's subject node was unexpectedly null.",
                subjectNode != null);

     // Check that we have the right value for the node
     assertTrue("First statement's subject node was not the expected value, " +
                "was [" + subjectNode.toString() + "]",
                subjectNode instanceof BlankNodeImpl);

     try {

       // Get the predicate node
       predicateNode = resolverSession.globalize(predicate);
     } catch (GlobalizeException globalizeException) {

       // Log the exception
       log.error("Failed to globalize predicate node.", globalizeException);

       // Fail the test
       fail("Failed to globalize predicate node.");
     }

     // Check that the node was globalized correctly
     assertTrue("First statement's predicate node was unexpectedly null.",
                subjectNode != null);

     try {

       // Get the object node
       objectNode = resolverSession.globalize(object);
     } catch (GlobalizeException globalizeException) {

       // Log the exception
       log.error("Failed to globalize object node.", globalizeException);

       // Fail the test
       fail("Failed to globalize object node.");
     }

     // Check that the node was globalized correctly
     assertTrue("First statement's object node was unexpectedly null.",
                subjectNode != null);
  }
  */


  /**
   * Default text runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {

    junit.textui.TestRunner.run(suite());
  }
}
