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
import java.util.*;

// Third party packages
import junit.framework.*; // JUnit
import org.jrdf.graph.*;
import org.apache.log4j.*; // Log4J
import org.apache.log4j.xml.*; // Log4J

import org.mulgara.resolver.filesystem.exception.*;

/**
 * Unit testing for the MetaFileManager object.
 *
 * @created 2004-11-30
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
public class FileManagerUnitTest extends TestCase {

  /** Logger */
  private static Logger log = Logger.getLogger(FileManagerUnitTest.class);

  /**
   * Constructor as required by JUnit's TestCase.
   */
  public FileManagerUnitTest(String name) {

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
    suite.addTest(new FileManagerUnitTest("testConstruction"));
    suite.addTest(new FileManagerUnitTest("testAddFileSystem"));
    suite.addTest(new FileManagerUnitTest("testPrepare"));
    suite.addTest(new FileManagerUnitTest("testNextTriple"));
    suite.addTest(new FileManagerUnitTest("testBadSystem"));
    suite.addTest(new FileManagerUnitTest("testNonExistantSystem"));

    return suite;
  }

  /**
   * Test the constructor.
   */
  public void testConstruction() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting construction test");
    }

    // Create a meta file manager
    MetaFileManager manager = new MetaFileManager(createExclusionList());

    // Check the meta file manager was created properly
    assertTrue("MetaFileManager object was not created as expected.",
               manager != null);
  }

  /**
   * Test that the the prepare method works correctly.
   */
  public void testPrepare() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting prepare test");
    }

    // Create a meta file manager
    MetaFileManager manager = new MetaFileManager(createExclusionList());

    // Check the meta file manager was created properly
    assertTrue("MetaFileManager object was not created as expected.",
               manager != null);

    // Populate the list of inclusions
    populateInclusionList(manager);

    try {

      // Prepare the meta file manager for navigation
      manager.prepare();
    } catch (FileManagerException fileManagerException) {

      // Log the exception
      log.error("Failed to prepare file manager for retrieval of filesystem " +
                "metadata", fileManagerException);

      // Fail the test
      fail("Failed to prepare file manager for retrieval of filesystem " +
           "metadata");
    }
  }

  /**
   * Test that the included system addition works correctly.
   */
  public void testAddFileSystem() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting add file system test");
    }

    // Create a meta file manager
    MetaFileManager manager = new MetaFileManager(createExclusionList());

    // Check the meta file manager was created properly
    assertTrue("MetaFileManager object was not created as expected.",
               manager != null);

    // Populate the list of inclusions
    populateInclusionList(manager);

    // No exceptions or failures should really occur, however, future versions
    // may do so and these should be accounted for in the testing
  }

  /**
   * Test the effects of including a badly formed filesystem uri.
   */
  public void testBadSystem() {

    if (log.isDebugEnabled()) {

      log.debug("// Starting bad system test");
    }

    // Create a meta file manager
    MetaFileManager manager = new MetaFileManager(createExclusionList());

    // Check the meta file manager was created properly
    assertTrue("MetaFileManager object was not created as expected.",
               manager != null);

    // Container for our bad filesystem URI
    URI fileSystemURI = null;

    try {

      // Create an URI which is not of the file protocol
      fileSystemURI = new URI("http://myserver.com/myFile.ext");
    } catch (URISyntaxException uriSyntaxException) {

      // Log the exception
      log.error("Failed to create non-file URI 'http://myserver.com/myFile.ext'",
                uriSyntaxException);

      // Fail the test
      fail("Failed to create non-file URI 'http://myserver.com/myFile.ext'");
    }

    // Add the file's URI to the inclusion list
    manager.addFileSystem(fileSystemURI);

    try {

      // Prepare the meta file manager for navigation
      manager.prepare();
    } catch (FileManagerException fileManagerException) {

      // Log the exception
      log.error("Failed to prepare file manager for retrieval of filesystem " +
                "metadata", fileManagerException);

      // Fail the test
      fail("Failed to prepare file manager for retrieval of filesystem " +
           "metadata");
    }

    // Container for our first triple
    Triple nextTriple = null;

    try {

      // Obtain the next triple in the meta file manager
      nextTriple = manager.nextTriple();
    } catch (FileManagerException fileManagerException) {

      // Log the exception
      log.error("Failed to prepare file manager for retrieval of filesystem " +
                "metadata", fileManagerException);

      // Fail the test
      fail("Failed to prepare file manager for retrieval of filesystem " +
           "metadata");
    }

    // Check that we obtained a triple
    assertTrue("Next triple in manager was unexpectedly null",
               nextTriple != null);

    if (log.isDebugEnabled()) {

        log.debug("Globalized values of first entry: [" +
                  nextTriple.getSubject() + ", " + nextTriple.getPredicate() +
                  ", " + nextTriple.getObject() + "]");
      }

      // Container for our triple node values
      Node subjectNode = nextTriple.getSubject();
      Node predicateNode = nextTriple.getPredicate();
      Node objectNode = nextTriple.getObject();

      // Check that the node was globalized correctly
      assertTrue("First statement's subject node was unexpectedly null.",
                 subjectNode != null);

      // Check that we have the right value for the subject node
      assertTrue("First statement's subject node was not the expected value, " +
                 "was [" + subjectNode.toString() + "]",
                 subjectNode.toString().equals(
                 "http://myserver.com/myFile.ext"));

      // Check that the node was globalized correctly
      assertTrue("First statement's predicate node was unexpectedly null.",
                 predicateNode != null);

      // Check that we have the right value for the node
      assertTrue(
          "First statement's predicate node was not the expected value, " +
          "was [" + predicateNode.toString() + "]",
          predicateNode.toString().equals(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));

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
     * Test the effects of obtaining the next triple.
     */
    public void testNextTriple() {

      if (log.isDebugEnabled()) {

        log.debug("// Starting next triple test");
      }
      // Create a meta file manager
      MetaFileManager manager = new MetaFileManager(createExclusionList());

      // Check the meta file manager was created properly
      assertTrue("MetaFileManager object was not created as expected.",
                 manager != null);

      // Populate the list of inclusions
      populateInclusionList(manager);

      try {

        // Prepare the meta file manager for navigation
        manager.prepare();
      } catch (FileManagerException fileManagerException) {

        // Log the exception
        log.error("Failed to prepare file manager for retrieval of filesystem " +
                  "metadata", fileManagerException);

        // Fail the test
        fail("Failed to prepare file manager for retrieval of filesystem " +
             "metadata");
      }

      // Container for our first triple
      Triple nextTriple = null;

      try {

        // Obtain the next triple in the meta file manager
        nextTriple = manager.nextTriple();
      } catch (FileManagerException fileManagerException) {

        // Log the exception
        log.error("Failed to prepare file manager for retrieval of filesystem " +
                  "metadata", fileManagerException);

        // Fail the test
        fail("Failed to prepare file manager for retrieval of filesystem " +
             "metadata");
      }

      // Check that we obtained a triple
      assertTrue("Next triple in manager was unexpectedly null",
                 nextTriple != null);
    }

    /**
     * Test the effects of including a filesystem uri which does not exist.
     */
    public void testNonExistantSystem() {

      if (log.isDebugEnabled()) {

        log.debug("// Starting non-existant system test");
      }

      // Create a meta file manager
      MetaFileManager manager = new MetaFileManager(createExclusionList());

      // Check the meta file manager was created properly
      assertTrue("MetaFileManager object was not created as expected.",
                 manager != null);

      // Create a file which represents a non-existant filesystem
      File fileSystem = new File(System.getProperty("java.io.tmpdir") +
                                 File.separator + "filesstem");

      // Add the file's URI to the inclusion list
      manager.addFileSystem(fileSystem.toURI());

      try {

        // Prepare the meta file manager for navigation
        manager.prepare();
      } catch (FileManagerException fileManagerException) {

        // Log the exception
        log.error("Failed to prepare file manager for retrieval of filesystem " +
                  "metadata", fileManagerException);

        // Fail the test
        fail("Failed to prepare file manager for retrieval of filesystem " +
             "metadata");
      }

      // Container for our first triple
      Triple nextTriple = null;

      try {

        // Obtain the next triple in the meta file manager
        nextTriple = manager.nextTriple();
      } catch (FileManagerException fileManagerException) {

        // Log the exception
        log.error("Failed to prepare file manager for retrieval of filesystem " +
                  "metadata", fileManagerException);

        // Fail the test
        fail("Failed to prepare file manager for retrieval of filesystem " +
             "metadata");
      }

      // Check that we obtained a triple
      assertTrue("Next triple in manager was unexpectedly null",
                 nextTriple != null);

      if (log.isDebugEnabled()) {

          log.debug("Globalized values of first entry: [" +
                    nextTriple.getSubject() + ", " + nextTriple.getPredicate() +
                    ", " + nextTriple.getObject() + "]");
        }

        // Container for our triple node values
        Node subjectNode = nextTriple.getSubject();
        Node predicateNode = nextTriple.getPredicate();
        Node objectNode = nextTriple.getObject();

        // Check that the node was globalized correctly
        assertTrue("First statement's subject node was unexpectedly null.",
                   subjectNode != null);

        // Check that we have the right value for the subject node
        assertTrue("First statement's subject node was not the expected value, " +
                   "was [" + subjectNode.toString() + "]",
                   subjectNode.toString().equals(fileSystem.toURI().toString()));

        // Check that the node was globalized correctly
        assertTrue("First statement's predicate node was unexpectedly null.",
                   predicateNode != null);

        // Check that we have the right value for the node
        assertTrue(
            "First statement's predicate node was not the expected value, " +
            "was [" + predicateNode.toString() + "]",
            predicateNode.toString().equals(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));

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
     * Creates a list of files to exclude.
     *
     * @return The list of files to exclude
     */
    public ArrayList<File> createExclusionList() {

      // Create a file which represents the filesystem to exclude
      File fileSystem = new File(System.getProperty("java.io.tmpdir") +
                                 File.separator + "filesystem" +
                                 File.separator + "mp3");

      // Create a list for the exclusion files
      ArrayList<File> excludeList = new ArrayList<File>();

      // Add the file to the list
      excludeList.add(fileSystem);

      return excludeList;
    }

    /**
     * Populates the inclusion list of the given meta file manager.
     *
     * @param manager The meta file manager to populate
     */
    public void populateInclusionList(MetaFileManager manager) {

      // Create a file which represents our data filesystem
      File fileSystem = new File(System.getProperty("java.io.tmpdir") +
                                 File.separator + "filesystem");

      // Add the file's URI to the inclusion list
      manager.addFileSystem(fileSystem.toURI());
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
