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

package org.mulgara.resolver.filesystem;

// Java 2 standard packages
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;

// Third party packages
import junit.framework.*; // JUnit
import org.apache.log4j.*; // Log4J
import org.jrdf.graph.Node;
import org.apache.log4j.xml.*; // Log4J

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.GlobalizeException;
import org.mulgara.resolver.spi.LocalizeException;
import org.mulgara.resolver.spi.ResolverSession;
import org.mulgara.resolver.spi.TestResolverSession;
import org.mulgara.store.tuples.*;

/**
 * Unit testing for the FileSystemStatements object.
 *
 * @created 2004-11-22
 *
 * @author Mark Ludlow
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:27 $ by $Author: newmana $
 *
 * @company <a href="mailto:info@PIsoftware.com">Plugged In Software</a>
 *
 * @copyright &copy; 2004 <a href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class FileSystemStatementsUnitTest extends TestCase {

  /** Logger */
  private static Logger log = Logger.getLogger(FileSystemStatementsUnitTest.class);

  /**
   * Constructor as required by JUnit's TestCase.
   */
  public FileSystemStatementsUnitTest(String name) {

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
  protected void setUp() {

  }

  /**
   * Clean up data set up by setUp().
   * Overrides TestCase.tearDown().
   */
  protected void tearDown() {

  }

  /**
   * Creates a test suite with various different output and compares the output.
   */
  public static TestSuite suite() {

    TestSuite suite = new TestSuite();

    // The test cases we want run during testing
    suite.addTest(new FileSystemStatementsUnitTest("testConstruction"));
    suite.addTest(new FileSystemStatementsUnitTest("testBeforeFirst"));
    suite.addTest(new FileSystemStatementsUnitTest("testNext"));
    suite.addTest(new FileSystemStatementsUnitTest("testEnd"));
    suite.addTest(new FileSystemStatementsUnitTest("testBadSystem"));
    suite.addTest(new FileSystemStatementsUnitTest("testLiteralSystem"));
    suite.addTest(new FileSystemStatementsUnitTest("testNonExistantSystem"));
    suite.addTest(new FileSystemStatementsUnitTest("testFileInclude"));
    suite.addTest(new FileSystemStatementsUnitTest("testFileExclude"));

    return suite;
  }

  /**
   * Test the constructor.
   */
  public void testConstruction() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting construction test");
    }

    // Obtain our tuples and session
    Object[] statementData = createSessionAndTuples();

    // Container for our statements object
    FileSystemStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      //  filesystem
      statements = new FileSystemStatements((Tuples) statementData[0],
                                            (Tuples) statementData[1],
                                            (ResolverSession) statementData[2]);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to create a set of statements about the filesystem",
                tuplesException);

      // Fail the test
      fail("Failed to create a set of statements about the filesystem");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);
  }

  /**
   * Test that the beforeFirst() method works correctly.
   */
  public void testBeforeFirst() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting before first test");
    }

    // Obtain our tuples and session
    Object[] statementData = createSessionAndTuples();

    // Container for our statements object
    FileSystemStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      //  filesystem
      statements = new FileSystemStatements((Tuples) statementData[0],
                                            (Tuples) statementData[1],
                                            (ResolverSession) statementData[2]);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to create a set of statements about the filesystem",
                tuplesException);

      // Fail the test
      fail("Failed to create a set of statements about the filesystem");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);

    try {

      // Reset the statements' pointer
      statements.beforeFirst();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to reset statements' cursor to before first entry",
                tuplesException);

      // Fail the test
      fail("Failed to reset statements' cursor to before first entry");
    }
  }

  /**
   * Test that the cursor advancement methods work correctly.
   */
  public void testNext() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting next test");
    }

    // Obtain our tuples and session
    Object[] statementData = createSessionAndTuples();

    // Container for our statements object
    FileSystemStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      //  filesystem
      statements = new FileSystemStatements((Tuples) statementData[0],
                                            (Tuples) statementData[1],
                                            (ResolverSession) statementData[2]);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to create a set of statements about the filesystem",
                tuplesException);

      // Fail the test
      fail("Failed to create a set of statements about the filesystem");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);

    try {

      // Reset the statements' pointer
      statements.beforeFirst();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to reset statements' cursor to before first entry",
                tuplesException);

      // Fail the test
      fail("Failed to reset statements' cursor to before first entry");
    }

    // Flag for whether the resolved statements had a next statement
    // (Assumed true)
    boolean hasNext = true;

    try {

      // Reset the statements' pointer
      hasNext = statements.next();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to retrieve first entry in resolved statements",
                tuplesException);

      // Fail the test
      fail("Failed to retrieve first entry in resolved statements");
    }

    assertTrue("Statements unexpectedly had no statements", hasNext);

    // Convert the third statement initialiser data item to a resolver session
    ResolverSession resolverSession = (ResolverSession) statementData[2];

    if (log.isDebugEnabled()) {

      try {

        log.debug("Globalized values of first entry: [" +
                  resolverSession.globalize(statements.getColumnValue(
            FileSystemStatements.SUBJECT)) + ", " +
                  resolverSession.globalize(statements.getColumnValue(
            FileSystemStatements.PREDICATE)) + ", " +
                  resolverSession.globalize(statements.getColumnValue(
            FileSystemStatements.OBJECT)) + "]");
      } catch (TuplesException tuplesException) {

        // Ignore the exception as we are debugging
      } catch (GlobalizeException globalizeException) {

        // Ignore the exception as we are debugging
      }
    }

    try {

      statements.getColumnValue(FileSystemStatements.SUBJECT);
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

      statements.getColumnValue(FileSystemStatements.PREDICATE);
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

      statements.getColumnValue(FileSystemStatements.OBJECT);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error(
          "Failed to retrieve the object node for first entry in statements.",
          tuplesException);

      // Fail the test
      fail(
          "Failed to retrieve the object node for first entry in statements.");
    }

    // Since we don't know what the first entry is going to be, we can only go
    // on the fact that there is a triple and that it is of the correct form
  }

  /**
   * Test the effects of including a badly formed filesystem uri.
   */
  public void testBadSystem() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting bad system test");
    }

    // Node id containers
    long dataDirNode = 0;
    long includeNode = 0;
    long subjectNode = 0;

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    try {

      // Allocate the filesystem node
      dataDirNode = resolverSession.localize(new URIReferenceImpl(new URI(
          "http://fileserver.com/mulgara/filesystem")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise bad inclusion node " +
                "<http://fileserver.com/mulgara/filesystem>", localiseException);

      // Fail the test
      fail("Failed to localise bad inclusion node " +
           "<http://fileserver.com/mulgara/filesystem>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create bad inclusion system uri " +
                "<http://fileserver.com/mulgara/filesystem>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create bad inclusion system uri " +
           "<http://fileserver.com/mulgara/filesystem>");
    }

    try {

      resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise filesystem node " +
                "<http://mulgara.org/mulgara/filesystem>", localiseException);

      // Fail the test
      fail("Failed to localise filesystem node " +
           "<http://mulgara.org/mulgara/filesystem>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create filesystem node uri " +
                "<http://mulgara.org/mulgara/filesystem>", uriSyntaxException);

      // Fail the test
      fail("Failed to create filesystem node uri " +
           "<http://mulgara.org/mulgara/filesystem>");
    }

    try {

      // Define the inclusion node
      includeNode = resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Include")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise inclusion node " +
                "<http://mulgara.org/mulgara/filesystem#Include>",
                localiseException);

      // Fail the test
      fail("Failed to localise inclusion node " +
           "<http://mulgara.org/mulgara/filesystem#Include>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create inclusion node uri " +
                "<http://mulgara.org/mulgara/filesystem#Include>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create inclusion node uri" +
           "<http://mulgara.org/mulgara/filesystem#Include>");
    }

    try {

      resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Exclude")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise exclusion node " +
                "<http://mulgara.org/mulgara/filesystem#Exclude>",
                localiseException);

      // Fail the test
      fail("Failed to localise exclusion node " +
           "<http://mulgara.org/mulgara/filesystem#Exclude>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create exclusion node uri " +
                "<http://mulgara.org/mulgara/filesystem#Exclude>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create exclusion node uri " +
           "<http://mulgara.org/mulgara/filesystem#Exclude>");
    }

    try {

      // Define the exclusion node
      subjectNode = resolverSession.localize(new BlankNodeImpl());
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise subject node", localiseException);

      // Fail the test
      fail("Failed to localise subject node");
    }

    // Create the variable column headers
    String[] var1 = new String[] {"fileSystemModel", "y", "fileSystemRef"};

    // Create the inclusion tuples
    Tuples includeTuples = new LiteralTuples(var1);

    // Create the tuple sets
    long[][] subjectTuples = new long[][] {new long[] {subjectNode, includeNode,
        dataDirNode}
    };

    // Iterate through the set of tuple nodes and append them to the tuples
    // container
    for (int i = 0; i < subjectTuples.length; i++) {

      try {

        // Append the tuples set to the container
        ((LiteralTuples) includeTuples).appendTuple(subjectTuples[i]);
      } catch (TuplesException tuplesException) {

        // Log the error
        log.error("Failed to append tuples set to inclusion list [" +
                  subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
                  subjectTuples[i][2] + "]", tuplesException);

        // Fail the test
        fail("Failed to append tuples set to inclusion list [" +
             subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
             subjectTuples[i][2] + "]");
      }
    }

    // Container for our statements object
    FileSystemStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      //  filesystem
      statements = new FileSystemStatements(includeTuples, null,
                                            resolverSession);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to create a set of statements about the filesystem",
                tuplesException);

      // Fail the test
      fail("Failed to create a set of statements about the filesystem");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);

    try {

      // Reset the statements' pointer
      statements.beforeFirst();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to reset statements' cursor to before first entry",
                tuplesException);

      // Fail the test
      fail("Failed to reset statements' cursor to before first entry");
    }

    // Flag for whether the resolved statements had a next statement
    // (Assumed true)
    boolean hasNext = true;

    try {

      // Reset the statements' pointer
      hasNext = statements.next();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to retrieve first entry in resolved statements",
                tuplesException);

      // Fail the test
      fail("Failed to retrieve first entry in resolved statements");
    }

    assertTrue("Statements unexpectedly had no statements", hasNext);

    if (log.isDebugEnabled()) {

      try {

        log.debug("Globalized values of first entry: [" +
                  resolverSession.globalize(statements.getColumnValue(
            FileSystemStatements.SUBJECT)) + ", " +
                  resolverSession.globalize(statements.getColumnValue(
            FileSystemStatements.PREDICATE)) + ", " +
                  resolverSession.globalize(statements.getColumnValue(
            FileSystemStatements.OBJECT)) + "]");
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
      subject = statements.getColumnValue(FileSystemStatements.SUBJECT);
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
      predicate = statements.getColumnValue(FileSystemStatements.PREDICATE);
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
      object = statements.getColumnValue(FileSystemStatements.OBJECT);
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
    Node subjectsNode = null;
    Node predicateNode = null;
    Node objectNode = null;

    try {

      // Get the subject node
      subjectsNode = resolverSession.globalize(subject);
    } catch (GlobalizeException globalizeException) {

      // Log the exception
      log.error("Failed to globalize subject node.", globalizeException);

      // Fail the test
      fail("Failed to globalize subject node.");
    }

    // Check that the node was globalized correctly
    assertTrue("First statement's subject node was unexpectedly null.",
               subjectsNode != null);

    // Check that we have the right value for the subject node
    assertTrue("First statement's subject node was not the expected value, " +
               "was [" + subjectsNode.toString() + "]",
               subjectsNode.toString().equals(
        "http://fileserver.com/mulgara/filesystem"));

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
               predicateNode != null);

    // Check that we have the right value for the node
    assertTrue("First statement's predicate node was not the expected value, " +
               "was [" + predicateNode.toString() + "]",
               predicateNode.toString().equals(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));

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
               objectNode != null);

    // Check that we have the right value for the node
    assertTrue("First statement's object node was not the expected value, " +
               "was [" + objectNode.toString() + "]",
               objectNode.toString().equals(
        "http://mulgara.org/mulgara#InvalidFileSystem"));

  }

  /**
   * Test the effects of including a non-resource filesystem uri.
   */
  public void testLiteralSystem() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting literal system test");
    }

    // Node id containers
    long dataDirNode = 0;
    long includeNode = 0;
    long subjectNode = 0;

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Create a file which represents our data filesystem
    File fileSystem = new File(System.getProperty("cvs.root") + File.separator +
                               "data" + File.separator + "filesystem");

    try {

      // Allocate the filesystem node as a literal
      dataDirNode = resolverSession.localize(new LiteralImpl(
          fileSystem.toURI().toString()));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise bad inclusion node " + fileSystem.toURI(),
                localiseException);

      // Fail the test
      fail("Failed to localise bad inclusion node " + fileSystem.toURI());
    }

    try {

      resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise filesystem node " +
                "<http://mulgara.org/mulgara/filesystem>", localiseException);

      // Fail the test
      fail("Failed to localise filesystem node " +
           "<http://mulgara.org/mulgara/filesystem>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create filesystem node uri " +
                "<http://mulgara.org/mulgara/filesystem>", uriSyntaxException);

      // Fail the test
      fail("Failed to create filesystem node uri " +
           "<http://mulgara.org/mulgara/filesystem>");
    }

    try {

      // Define the inclusion node
      includeNode = resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Include")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise inclusion node " +
                "<http://mulgara.org/mulgara/filesystem#Include>",
                localiseException);

      // Fail the test
      fail("Failed to localise inclusion node " +
           "<http://mulgara.org/mulgara/filesystem#Include>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create inclusion node uri " +
                "<http://mulgara.org/mulgara/filesystem#Include>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create inclusion node uri" +
           "<http://mulgara.org/mulgara/filesystem#Include>");
    }

    try {

      resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Exclude")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise exclusion node " +
                "<http://mulgara.org/mulgara/filesystem#Exclude>",
                localiseException);

      // Fail the test
      fail("Failed to localise exclusion node " +
           "<http://mulgara.org/mulgara/filesystem#Exclude>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create exclusion node uri " +
                "<http://mulgara.org/mulgara/filesystem#Exclude>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create exclusion node uri " +
           "<http://mulgara.org/mulgara/filesystem#Exclude>");
    }

    try {

      // Define the exclusion node
      subjectNode = resolverSession.localize(new BlankNodeImpl());
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise subject node", localiseException);

      // Fail the test
      fail("Failed to localise subject node");
    }

    // Create the variable column headers
    String[] var1 = new String[] {"fileSystemModel", "y", "fileSystemRef"};

    // Create the inclusion tuples
    Tuples includeTuples = new LiteralTuples(var1);

    // Create the tuple sets
    long[][] subjectTuples = new long[][] {new long[] {subjectNode, includeNode,
        dataDirNode}
    };

    // Iterate through the set of tuple nodes and append them to the tuples
    // container
    for (int i = 0; i < subjectTuples.length; i++) {

      try {

        // Append the tuples set to the container
        ((LiteralTuples) includeTuples).appendTuple(subjectTuples[i]);
      } catch (TuplesException tuplesException) {

        // Log the error
        log.error("Failed to append tuples set to inclusion list [" +
                  subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
                  subjectTuples[i][2] + "]", tuplesException);

        // Fail the test
        fail("Failed to append tuples set to inclusion list [" +
             subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
             subjectTuples[i][2] + "]");
      }
    }

    // Container for our statements object
    FileSystemStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      //  filesystem
      statements = new FileSystemStatements(includeTuples, null,
                                            resolverSession);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to create a set of statements about the filesystem",
                tuplesException);

      // Fail the test
      fail("Failed to create a set of statements about the filesystem");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);
  }

  /**
   * Test the effects of including a filesystem uri which does not exist.
   */
  public void testNonExistantSystem() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting non-existant system test");
    }

    // Node id containers
    long dataDirNode = 0;
    long includeNode = 0;
    long subjectNode = 0;

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Create a file which represents our data filesystem
    File fileSystem = new File(System.getProperty("cvs.root") + File.separator +
                               "datax");

    try {

      // Allocate the filesystem node
      dataDirNode = resolverSession.localize(
          new URIReferenceImpl(fileSystem.toURI()));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise non-existant inclusion node " +
                fileSystem.toURI(), localiseException);

      // Fail the test
      fail("Failed to localise non-existant inclusion node " +
           fileSystem.toURI());
    }

    try {

      resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise filesystem node " +
                "<http://mulgara.org/mulgara/filesystem>", localiseException);

      // Fail the test
      fail("Failed to localise filesystem node " +
           "<http://mulgara.org/mulgara/filesystem>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create filesystem node uri " +
                "<http://mulgara.org/mulgara/filesystem>", uriSyntaxException);

      // Fail the test
      fail("Failed to create filesystem node uri " +
           "<http://mulgara.org/mulgara/filesystem>");
    }

    try {

      // Define the inclusion node
      includeNode = resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Include")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise inclusion node " +
                "<http://mulgara.org/mulgara/filesystem#Include>",
                localiseException);

      // Fail the test
      fail("Failed to localise inclusion node " +
           "<http://mulgara.org/mulgara/filesystem#Include>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create inclusion node uri " +
                "<http://mulgara.org/mulgara/filesystem#Include>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create inclusion node uri" +
           "<http://mulgara.org/mulgara/filesystem#Include>");
    }

    try {

      resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Exclude")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise exclusion node " +
                "<http://mulgara.org/mulgara/filesystem#Exclude>",
                localiseException);

      // Fail the test
      fail("Failed to localise exclusion node " +
           "<http://mulgara.org/mulgara/filesystem#Exclude>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create exclusion node uri " +
                "<http://mulgara.org/mulgara/filesystem#Exclude>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create exclusion node uri " +
           "<http://mulgara.org/mulgara/filesystem#Exclude>");
    }

    try {

      // Define the exclusion node
      subjectNode = resolverSession.localize(new BlankNodeImpl());
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise subject node", localiseException);

      // Fail the test
      fail("Failed to localise subject node");
    }

    // Create the variable column headers
    String[] var1 = new String[] {"fileSystemModel", "y", "fileSystemRef"};

    // Create the inclusion tuples
    Tuples includeTuples = new LiteralTuples(var1);

    // Create the tuple sets
    long[][] subjectTuples = new long[][] {new long[] {subjectNode, includeNode,
        dataDirNode}
    };

    // Iterate through the set of tuple nodes and append them to the tuples
    // container
    for (int i = 0; i < subjectTuples.length; i++) {

      try {

        // Append the tuples set to the container
        ((LiteralTuples) includeTuples).appendTuple(subjectTuples[i]);
      } catch (TuplesException tuplesException) {

        // Log the error
        log.error("Failed to append tuples set to inclusion list [" +
                  subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
                  subjectTuples[i][2] + "]", tuplesException);

        // Fail the test
        fail("Failed to append tuples set to inclusion list [" +
             subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
             subjectTuples[i][2] + "]");
      }
    }

    // Container for our statements object
    FileSystemStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      //  filesystem
      statements = new FileSystemStatements(includeTuples, null,
                                            resolverSession);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to create a set of statements about the filesystem",
                tuplesException);

      // Fail the test
      fail("Failed to create a set of statements about the filesystem");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);

    try {

      // Reset the statements' pointer
      statements.beforeFirst();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to reset statements' cursor to before first entry",
                tuplesException);

      // Fail the test
      fail("Failed to reset statements' cursor to before first entry");
    }

    // Flag for whether the resolved statements had a next statement
    // (Assumed true)
    boolean hasNext = true;

    try {

      // Reset the statements' pointer
      hasNext = statements.next();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to retrieve first entry in resolved statements",
                tuplesException);

      // Fail the test
      fail("Failed to retrieve first entry in resolved statements");
    }

    assertTrue("Statements unexpectedly had no statements", hasNext);

    if (log.isDebugEnabled()) {

      try {

        log.debug("Globalized values of first entry: [" +
                  resolverSession.globalize(statements.getColumnValue(
            FileSystemStatements.SUBJECT)) + ", " +
                  resolverSession.globalize(statements.getColumnValue(
            FileSystemStatements.PREDICATE)) + ", " +
                  resolverSession.globalize(statements.getColumnValue(
            FileSystemStatements.OBJECT)) + "]");
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
      subject = statements.getColumnValue(FileSystemStatements.SUBJECT);
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
      predicate = statements.getColumnValue(FileSystemStatements.PREDICATE);
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
      object = statements.getColumnValue(FileSystemStatements.OBJECT);
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
    Node subjectsNode = null;
    Node predicateNode = null;
    Node objectNode = null;

    try {

      // Get the subject node
      subjectsNode = resolverSession.globalize(subject);
    } catch (GlobalizeException globalizeException) {

      // Log the exception
      log.error("Failed to globalize subject node.", globalizeException);

      // Fail the test
      fail("Failed to globalize subject node.");
    }

    // Check that the node was globalized correctly
    assertTrue("First statement's subject node was unexpectedly null.",
               subjectsNode != null);

    // Check that we have the right value for the subject node
    assertTrue("First statement's subject node was not the expected value, " +
               "was [" + subjectsNode.toString() + "]",
               subjectsNode.toString().equals(
        fileSystem.toURI().toString()));

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
               predicateNode != null);

    // Check that we have the right value for the node
    assertTrue("First statement's predicate node was not the expected value, " +
               "was [" + predicateNode.toString() + "]",
               predicateNode.toString().equals(
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));

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
               objectNode != null);

    // Check that we have the right value for the node
    assertTrue("First statement's object node was not the expected value, " +
               "was [" + objectNode.toString() + "]",
               objectNode.toString().equals(
        "http://mulgara.org/mulgara#NonExistantFileSystem"));
  }

  /**
   * Test that the inclusion of files works.
   */
  public void testFileInclude() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting file inclusion test");
    }

    // Node id containers
    long dataDirNode = 0;
    long includeNode = 0;
    long subjectNode = 0;

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Create a file which represents our sample file
    File file = new File(System.getProperty("cvs.root") + File.separator +
                         "data" + File.separator + "ical.rdf");

    try {

      // Allocate the filesystem node as a literal
      dataDirNode = resolverSession.localize(new URIReferenceImpl(file.toURI()));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise bad inclusion node " + file.toURI(),
                localiseException);

      // Fail the test
      fail("Failed to localise bad inclusion node " + file.toURI());
    }

    try {

      resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise filesystem node " +
                "<http://mulgara.org/mulgara/filesystem>", localiseException);

      // Fail the test
      fail("Failed to localise filesystem node " +
           "<http://mulgara.org/mulgara/filesystem>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create filesystem node uri " +
                "<http://mulgara.org/mulgara/filesystem>", uriSyntaxException);

      // Fail the test
      fail("Failed to create filesystem node uri " +
           "<http://mulgara.org/mulgara/filesystem>");
    }

    try {

      // Define the inclusion node
      includeNode = resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Include")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise inclusion node " +
                "<http://mulgara.org/mulgara/filesystem#Include>",
                localiseException);

      // Fail the test
      fail("Failed to localise inclusion node " +
           "<http://mulgara.org/mulgara/filesystem#Include>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create inclusion node uri " +
                "<http://mulgara.org/mulgara/filesystem#Include>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create inclusion node uri" +
           "<http://mulgara.org/mulgara/filesystem#Include>");
    }

    try {

      resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Exclude")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise exclusion node " +
                "<http://mulgara.org/mulgara/filesystem#Exclude>",
                localiseException);

      // Fail the test
      fail("Failed to localise exclusion node " +
           "<http://mulgara.org/mulgara/filesystem#Exclude>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create exclusion node uri " +
                "<http://mulgara.org/mulgara/filesystem#Exclude>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create exclusion node uri " +
           "<http://mulgara.org/mulgara/filesystem#Exclude>");
    }

    try {

      // Define the exclusion node
      subjectNode = resolverSession.localize(new BlankNodeImpl());
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise subject node", localiseException);

      // Fail the test
      fail("Failed to localise subject node");
    }

    // Create the variable column headers
    String[] var1 = new String[] {"fileSystemModel", "y", "fileSystemRef"};

    // Create the inclusion tuples
    Tuples includeTuples = new LiteralTuples(var1);

    // Create the tuple sets
    long[][] subjectTuples = new long[][] {new long[] {subjectNode, includeNode,
        dataDirNode}
    };

    // Iterate through the set of tuple nodes and append them to the tuples
    // container
    for (int i = 0; i < subjectTuples.length; i++) {

      try {

        // Append the tuples set to the container
        ((LiteralTuples) includeTuples).appendTuple(subjectTuples[i]);
      } catch (TuplesException tuplesException) {

        // Log the error
        log.error("Failed to append tuples set to inclusion list [" +
                  subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
                  subjectTuples[i][2] + "]", tuplesException);

        // Fail the test
        fail("Failed to append tuples set to inclusion list [" +
             subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
             subjectTuples[i][2] + "]");
      }
    }

    // Container for our statements object
    FileSystemStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      //  filesystem
      statements = new FileSystemStatements(includeTuples, null,
                                            resolverSession);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to create a set of statements about the filesystem",
                tuplesException);

      // Fail the test
      fail("Failed to create a set of statements about the filesystem");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);

    try {

      // Reset the statements' pointer
      statements.beforeFirst();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to reset statements' cursor to before first entry",
                tuplesException);

      // Fail the test
      fail("Failed to reset statements' cursor to before first entry");
    }

    // Flag for whether the resolved statements had a next statement
    // (Assumed true)
    boolean hasNext = true;

    try {

      // Reset the statements' pointer
      hasNext = statements.next();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to retrieve first entry in resolved statements",
                tuplesException);

      // Fail the test
      fail("Failed to retrieve first entry in resolved statements");
    }

    assertTrue("Statements unexpectedly had no statements", hasNext);

    if (log.isDebugEnabled()) {

      try {

        log.debug("Globalized values of first entry: [" +
                  resolverSession.globalize(statements.getColumnValue(
            FileSystemStatements.SUBJECT)) + ", " +
                  resolverSession.globalize(statements.getColumnValue(
            FileSystemStatements.PREDICATE)) + ", " +
                  resolverSession.globalize(statements.getColumnValue(
            FileSystemStatements.OBJECT)) + "]");
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
      subject = statements.getColumnValue(FileSystemStatements.SUBJECT);
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
      predicate = statements.getColumnValue(FileSystemStatements.PREDICATE);
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
      object = statements.getColumnValue(FileSystemStatements.OBJECT);
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
    Node subjectsNode = null;
    Node predicateNode = null;
    Node objectNode = null;

    try {

      // Get the subject node
      subjectsNode = resolverSession.globalize(subject);
    } catch (GlobalizeException globalizeException) {

      // Log the exception
      log.error("Failed to globalize subject node.", globalizeException);

      // Fail the test
      fail("Failed to globalize subject node.");
    }

    // Check that the node was globalized correctly
    assertTrue("First statement's subject node was unexpectedly null.",
               subjectsNode != null);

    // Check that we have the right value for the subject node
    assertTrue("First statement's subject node was not the expected value, " +
               "was [" + subjectsNode.toString() + "]",
               subjectsNode.toString().equals(file.toURI().toString()));

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
               predicateNode != null);

    // Check that we have the right value for the node
    assertTrue("First statement's predicate node was not the expected value, " +
               "was [" + predicateNode.toString() + "]",
               predicateNode.toString().equals(
        "http://mulgara.org/mulgara#canWrite"));

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
               objectNode != null);

    // Check that we have the right value for the node
    assertTrue("First statement's object node was not the expected value, " +
               "was [" + objectNode.toString() + "]",
               objectNode.toString().equals("\"true\""));

  }

  /**
   * Test that the exclusion of files works.
   */
  public void testFileExclude() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting file exclusion test");
    }
    // Node id containers
    long dataDirNode = 0;
    long includeNode = 0;
    long excludeNode = 0;
    long subjectNode = 0;

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Create a file which represents our sample file
    File file = new File(System.getProperty("cvs.root") + File.separator +
                         "data" + File.separator + "ical.rdf");

    try {

      // Allocate the filesystem node as a literal
      dataDirNode = resolverSession.localize(new URIReferenceImpl(file.toURI()));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise bad inclusion node " + file.toURI(),
                localiseException);

      // Fail the test
      fail("Failed to localise bad inclusion node " + file.toURI());
    }

    try {

      resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise filesystem node " +
                "<http://mulgara.org/mulgara/filesystem>", localiseException);

      // Fail the test
      fail("Failed to localise filesystem node " +
           "<http://mulgara.org/mulgara/filesystem>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create filesystem node uri " +
                "<http://mulgara.org/mulgara/filesystem>", uriSyntaxException);

      // Fail the test
      fail("Failed to create filesystem node uri " +
           "<http://mulgara.org/mulgara/filesystem>");
    }

    try {

      // Define the inclusion node
      includeNode = resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Include")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise inclusion node " +
                "<http://mulgara.org/mulgara/filesystem#Include>",
                localiseException);

      // Fail the test
      fail("Failed to localise inclusion node " +
           "<http://mulgara.org/mulgara/filesystem#Include>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create inclusion node uri " +
                "<http://mulgara.org/mulgara/filesystem#Include>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create inclusion node uri" +
           "<http://mulgara.org/mulgara/filesystem#Include>");
    }

    try {

      // Define the exclusion node
      excludeNode = resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Exclude")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise exclusion node " +
                "<http://mulgara.org/mulgara/filesystem#Exclude>",
                localiseException);

      // Fail the test
      fail("Failed to localise exclusion node " +
           "<http://mulgara.org/mulgara/filesystem#Exclude>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create exclusion node uri " +
                "<http://mulgara.org/mulgara/filesystem#Exclude>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create exclusion node uri " +
           "<http://mulgara.org/mulgara/filesystem#Exclude>");
    }

    try {

      // Define the exclusion node
      subjectNode = resolverSession.localize(new BlankNodeImpl());
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise subject node", localiseException);

      // Fail the test
      fail("Failed to localise subject node");
    }

    // Create the variable column headers
    String[] var1 = new String[] {"fileSystemModel", "y", "fileSystemRef"};

    // Create the inclusion tuples
    Tuples includeTuples = new LiteralTuples(var1);

    // Create the tuple sets
    long[][] subjectTuples = new long[][] {new long[] {subjectNode, includeNode,
        dataDirNode}
    };

    // Iterate through the set of tuple nodes and append them to the tuples
    // container
    for (int i = 0; i < subjectTuples.length; i++) {

      try {

        // Append the tuples set to the container
        ((LiteralTuples) includeTuples).appendTuple(subjectTuples[i]);
      } catch (TuplesException tuplesException) {

        // Log the error
        log.error("Failed to append tuples set to inclusion list [" +
                  subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
                  subjectTuples[i][2] + "]", tuplesException);

        // Fail the test
        fail("Failed to append tuples set to inclusion list [" +
             subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
             subjectTuples[i][2] + "]");
      }
    }

    // Create the exclusion tuples
    Tuples excludeTuples = new LiteralTuples(var1);

    // Create the tuple sets
    subjectTuples = new long[][] {new long[] {subjectNode, excludeNode,
        dataDirNode}
    };

    // Iterate through the set of tuple nodes and append them to the tuples
    // container
    for (int i = 0; i < subjectTuples.length; i++) {

      try {

        // Append the tuples set to the container
        ((LiteralTuples) excludeTuples).appendTuple(subjectTuples[i]);
      } catch (TuplesException tuplesException) {

        // Log the error
        log.error("Failed to append tuples set to exclusion list [" +
                  subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
                  subjectTuples[i][2] + "]", tuplesException);

        // Fail the test
        fail("Failed to append tuples set to exclusion list [" +
             subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
             subjectTuples[i][2] + "]");
      }
    }

    // Container for our statements object
    FileSystemStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      //  filesystem
      statements = new FileSystemStatements(includeTuples, excludeTuples,
                                            resolverSession);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to create a set of statements about the filesystem",
                tuplesException);

      // Fail the test
      fail("Failed to create a set of statements about the filesystem");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);

    try {

      // Reset the statements' pointer
      statements.beforeFirst();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to reset statements' cursor to before first entry",
                tuplesException);

      // Fail the test
      fail("Failed to reset statements' cursor to before first entry");
    }

    // Flag for whether the resolved statements had a next statement
    // (Assumed true)
    boolean hasNext = true;

    try {

      // Reset the statements' pointer
      hasNext = statements.next();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to retrieve first entry in resolved statements",
                tuplesException);

      // Fail the test
      fail("Failed to retrieve first entry in resolved statements");
    }

    assertTrue("Statements unexpectedly contained statements", !hasNext);
  }

  /**
   * Test that statements can get to the end.
   */
  public void testEnd() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting end test");
    }

    // Obtain our tuples and session
    Object[] statementData = createSessionAndTuples();

    // Container for our statements object
    FileSystemStatements statements = null;

    try {

      // Construct a new statements object which will represent the content of
      //  filesystem
      statements = new FileSystemStatements((Tuples) statementData[0],
                                            (Tuples) statementData[1],
                                            (ResolverSession) statementData[2]);
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to create a set of statements about the filesystem",
                tuplesException);

      // Fail the test
      fail("Failed to create a set of statements about the filesystem");
    }

    // Check the statements objects was really created
    assertTrue("Statements object was not created as expected.",
               statements != null);

    try {

      // Reset the statements' pointer
      statements.beforeFirst();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to reset statements' cursor to before first entry",
                tuplesException);

      // Fail the test
      fail("Failed to reset statements' cursor to before first entry");
    }

    // Flag for whether the resolved statements had a next statement
    // (Assumed true)
    boolean hasNext = true;

    try {

      // Reset the statements' pointer
      hasNext = statements.next();
    } catch (TuplesException tuplesException) {

      // Log the exception
      log.error("Failed to retrieve first entry in resolved statements",
                tuplesException);

      // Fail the test
      fail("Failed to retrieve first entry in resolved statements");
    }

    assertTrue("Statements unexpectedly had no statements", hasNext);

    // Idle through the statements generated to test that they will end
    // correctly
    while (hasNext) {

      try {

        // Reset the statements' pointer
        hasNext = statements.next();
      } catch (TuplesException tuplesException) {

        // Log the exception
        log.error("Failed to navigate statements to end", tuplesException);

        // Fail the test
        fail("Failed to navigate statements to end");
      }
    }
  }

  /**
   * Creates a session and the inclusion and exclusion tuples.
   *
   * @return The array of created session and tuples details in the order:
   *         Included tuples, Excluded tuples, and session
   */
  private Object[] createSessionAndTuples() {

    // Node id containers
    long dataDirNode = 0;
    long mboxDirNode = 0;
    long includeNode = 0;
    long excludeNode = 0;
    long subjectNode = 0;

    // Obtain a resolver session
    ResolverSession resolverSession = new TestResolverSession();

    // Create a file which represents our data filesystem
    File fileSystem = new File(System.getProperty("cvs.root") + File.separator +
                               "data" + File.separator + "filesystem");

    try {

      // Allocate the filesystem node
      dataDirNode = resolverSession.localize(
          new URIReferenceImpl(fileSystem.toURI()));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise data directory node: " + fileSystem.toURI(),
                localiseException);

      // Fail the test
      fail("Failed to localise data directory node: " + fileSystem.toURI());
    }

    // Create a file which represents our mbox filesystem
    fileSystem = new File(System.getProperty("cvs.root") + File.separator +
                          "data" + File.separator + "filesystem" +
                          File.separator +
                          "mbox");

    try {

      // Allocate the filesystem node
      mboxDirNode = resolverSession.localize(
          new URIReferenceImpl(fileSystem.toURI()));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise mbox directory node: " + fileSystem.toURI(),
                localiseException);

      // Fail the test
      fail("Failed to localise mbox directory node: " + fileSystem.toURI());
    }

    try {

      resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise filesystem node " +
                "<http://mulgara.org/mulgara/filesystem>", localiseException);

      // Fail the test
      fail("Failed to localise filesystem node " +
           "<http://mulgara.org/mulgara/filesystem>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create filesystem node uri " +
                "<http://mulgara.org/mulgara/filesystem>", uriSyntaxException);

      // Fail the test
      fail("Failed to create filesystem node uri " +
           "<http://mulgara.org/mulgara/filesystem>");
    }

    try {

      // Define the inclusion node
      includeNode = resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Include")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise inclusion node " +
                "<http://mulgara.org/mulgara/filesystem#Include>",
                localiseException);

      // Fail the test
      fail("Failed to localise inclusion node " +
           "<http://mulgara.org/mulgara/filesystem#Include>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create inclusion node uri " +
                "<http://mulgara.org/mulgara/filesystem#Include>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create inclusion node uri" +
           "<http://mulgara.org/mulgara/filesystem#Include>");
    }

    try {

      // Define the exclusion node
      excludeNode = resolverSession.localize(new URIReferenceImpl(new URI(
          "http://mulgara.org/mulgara/filesystem#Exclude")));
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise exclusion node " +
                "<http://mulgara.org/mulgara/filesystem#Exclude>",
                localiseException);

      // Fail the test
      fail("Failed to localise exclusion node " +
           "<http://mulgara.org/mulgara/filesystem#Exclude>");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the error
      log.error("Failed to create exclusion node uri " +
                "<http://mulgara.org/mulgara/filesystem#Exclude>",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create exclusion node uri " +
           "<http://mulgara.org/mulgara/filesystem#Exclude>");
    }

    try {

      // Define the exclusion node
      subjectNode = resolverSession.localize(new BlankNodeImpl());
    } catch (LocalizeException localiseException) {

      // Log the error
      log.error("Failed to localise subject node", localiseException);

      // Fail the test
      fail("Failed to localise subject node");
    }

    // Create the variable column headers
    String[] var1 = new String[] {"fileSystemModel", "y", "fileSystemRef"};

    // Create the inclusion tuples
    Tuples includeTuples = new LiteralTuples(var1);

    // Create the tuple sets
    long[][] subjectTuples = new long[][] {new long[] {subjectNode, includeNode,
        dataDirNode}
    };

    // Iterate through the set of tuple nodes and append them to the tuples
    // container
    for (int i = 0; i < subjectTuples.length; i++) {

      try {

        // Append the tuples set to the container
        ((LiteralTuples) includeTuples).appendTuple(subjectTuples[i]);
      } catch (TuplesException tuplesException) {

        // Log the error
        log.error("Failed to append tuples set to inclusion list [" +
                  subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
                  subjectTuples[i][2] + "]", tuplesException);

        // Fail the test
        fail("Failed to append tuples set to inclusion list [" +
             subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
             subjectTuples[i][2] + "]");
      }
    }

    // Create the exclusion tuples
    Tuples excludeTuples = new LiteralTuples(var1);

    // Create the tuple sets
    subjectTuples = new long[][] {new long[] {subjectNode, excludeNode,
        mboxDirNode}
    };

    // Iterate through the set of tuple nodes and append them to the tuples
    // container
    for (int i = 0; i < subjectTuples.length; i++) {

      try {

        // Append the tuples set to the container
        ((LiteralTuples) excludeTuples).appendTuple(subjectTuples[i]);
      } catch (TuplesException tuplesException) {

        // Log the error
        log.error("Failed to append tuples set to exclusion list [" +
                  subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
                  subjectTuples[i][2] + "]", tuplesException);

        // Fail the test
        fail("Failed to append tuples set to exclusion list [" +
             subjectTuples[i][0] + ", " + subjectTuples[i][1] + ", " +
             subjectTuples[i][2] + "]");
      }
    }

    return new Object[] {includeTuples, excludeTuples, resolverSession};
  }

  /**
   * Default text runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {

    junit.textui.TestRunner.run(suite());
  }
}
