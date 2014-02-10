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

// Third party packages
import junit.framework.*;

// Java 2 standard packages
// Java 2 standard packages
// Third party packages
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.soap.SOAPException;

/**
 * Test cases for Collaborator.
 *
 * @author Tate Jones
 *
 * @created 2002-04-09
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:15 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *      Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class CollaboratorUnitTest extends TestCase {

  /** Logger. */
  @SuppressWarnings("unused")
  private final static Logger logger = Logger.getLogger(CollaboratorUnitTest.class);

  /**
   * a flag to indicate if the collorator requires closing
   *
   * NOTE : retrieveAnnotationsTest toggles this flag
   */
  private boolean closeCollaborator = false;

  /** Collaborator to be tested; */
  Collaborator collaborator = null;

  /**
   * Create the testing class
   *
   * @param name The name of the test.
   */
  public CollaboratorUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite to run.
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTest(new CollaboratorUnitTest("initializeModelTest"));
    suite.addTest(new CollaboratorUnitTest("modelTests"));
    suite.addTest(new CollaboratorUnitTest("registrationTest"));
    suite.addTest(new CollaboratorUnitTest("checkAccessKeyTest"));
    suite.addTest(new CollaboratorUnitTest("addingAnnotationTest"));
    suite.addTest(new CollaboratorUnitTest("removingAnnotationTest"));
    suite.addTest(new CollaboratorUnitTest("editAnnotationTest"));
    suite.addTest(new CollaboratorUnitTest("retrieveAnnotationsTest"));

    return suite;
  }

  /**
   * Default test runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {

    junit.textui.TestRunner.run(suite());
  }

  /**
   * Setup for testing
   *
   * @throws IOException Description of Exception
   */
  public void setUp() throws IOException {

    //Construct the collaborator();
    collaborator = new Collaborator();
  }

  /**
   * Tear down on completion
   *
   * @throws IOException Description of Exception
   */
  public void tearDown() throws IOException {
    if (closeCollaborator) {
      collaborator.close();
    }
  }

  /**
   * Test the initialization of the model
   *
   * @throws Exception Test fails
   */
  public void initializeModelTest() throws Exception {

    assertTrue("Failed to initialize collaborator model", collaborator.initializeModel());
  }

  /**
   * Test the creation and dropping of the model
   *
   */
  public void modelTests() {

    try {

      assertTrue("Failed to drop collaborator model", collaborator.dropModel());

      assertTrue("Expected collaborator model to be droped", collaborator.dropModel() == false);

      assertTrue("Failed to create collaborator model", collaborator.createModel());
    } catch (SOAPException ex) {
      ex.printStackTrace();
      fail("Graph tests failed " + ex.getMessage());
    }
  }

  /**
   * Test the registration process
   */
  public void registrationTest() {

    try {

      assertTrue("Register user", collaborator.register("xyz@company.com", "Joe Brown"));
    } catch (SOAPException ex) {
      ex.printStackTrace();
      fail("Registration test failed " + ex.getMessage());
    }

    try {
      collaborator.register("xyz@company.com", "Alex Mole");
      fail("Failed to detect duplicate registered user");
    } catch (SOAPException ex) {
      assertTrue("Failed to get the expected message (Your Email address has already been registered)",
          ex.getMessage().equals("Your Email address has already been registered"));
    }

    try {
      collaborator.register("", "Alex Mole");
      fail("Failed to detect empty email address");
    } catch (SOAPException ex) {
      assertTrue("Failed to get the expected message (Email address and nick name must be supplied)",
          ex.getMessage().equals("Email address and nick name must be supplied"));
    }
  }

  /**
   * Check for valid access key
   */
  public void checkAccessKeyTest() {

    try {

      assertTrue("Unable to register user", collaborator.register("guest@mulgara.org", "Guest User"));

      assertTrue("Expected to find registered user with key :" +
          collaborator.lastAccessKeyCreated +
          " email address : guest@mulgara.org",
          collaborator.checkAccessKey(collaborator.lastAccessKeyCreated,
          "guest@mulgara.org"));
    } catch (SOAPException ex) {
      ex.printStackTrace();
      fail("Not expecting exception on checking access key " + ex.getMessage());
    }

    try {
      collaborator.checkAccessKey("12345", "guest@mulgara.org");
      fail("Failed to detect invalid access key");
    } catch (SOAPException ex) {
      assertTrue("Failed to get the expected message (Invaild access key)",
          ex.getMessage().indexOf("Invaild access key") >= 0);
    }
  }

  /**
   * Test adding annotations
   */
  public void addingAnnotationTest() {

    //Add a successful annotation
    try {
      assertTrue("Unable to register user", collaborator.register("guest2@mulgara.org", "Guest User 2"));

      String annotationId =
          collaborator.addAnnotation("guest2@mulgara.org",
          collaborator.lastAccessKeyCreated, "Guest User 2", "1234567890",
          "This is a test annotation", "1", "10", "11", "100", "101", "50",
          "50");

      assertTrue("Unable to add annotation", (annotationId != null) && (annotationId.length() > 0));
    } catch (SOAPException ex) {
      ex.printStackTrace();
      fail("Failed to add annotation");
    }

    // Add an invalid annotation - incorrect key
    try {
      collaborator.addAnnotation("guest2@mulgara.org", "badkey",
          "Guest User 2", "1234567890", "This is a test annotation", "1", "10",
          "11", "100", "101", "50", "50");

      fail("Expected bad access key error");
    } catch (SOAPException ex) {
      assertTrue("Failed to get the expected message (Invaild access key)",
          ex.getMessage().indexOf("Invaild access key") >= 0);
    }

    // Add an invalid annotation - empty parameters
    try {
      collaborator.addAnnotation("guest2@mulgara.org",
          collaborator.lastAccessKeyCreated, "Guest User 2", "1234567890",
          "This is a another annotation test", "", "", "11", "100", "101",
          "50", "50");

      fail("Expected bad parameters supplied");
    } catch (SOAPException ex) {
      assertTrue("Failed to get the expected message (Invalid paramaters supplied for annotation)",
          ex.getMessage().indexOf("Invalid paramaters supplied for annotation") >= 0);
    }
  }

  /**
   * Test removing annotations
   */
  public void removingAnnotationTest() {

    String annotationId = null;

    //Add and remove an annotation
    try {
      assertTrue("Unable to register user", collaborator.register("guest3@mulgara.org", "Guest User 3"));

      annotationId =
          collaborator.addAnnotation("guest3@mulgara.org",
          collaborator.lastAccessKeyCreated, "Guest User 3", "1234567890",
          "This is a test annotation", "1", "10", "11", "100", "101", "50",
          "50");

      assertTrue("Unable to add annotation", (annotationId != null) && (annotationId.length() > 0));

      assertTrue("Unable to remove annotation",
          collaborator.removeAnnotation("guest3@mulgara.org",
          collaborator.lastAccessKeyCreated, "1234567890", annotationId));
    } catch (SOAPException ex) {
      ex.printStackTrace();
      fail("Failed to add/remove annotation");
    }

    // Test removing an annotation that does not exist
    try {
      assertTrue("Unable to remove annotation",
          collaborator.removeAnnotation("guest3@mulgara.org",
          collaborator.lastAccessKeyCreated, "1234567890", annotationId));
      fail("Expected not to be a successful removal");
    } catch (SOAPException ex) {
      assertTrue(
          "Failed to get the expected message (Unable to locate annotation for removal)",
          ex.getMessage().indexOf("Unable to locate annotation for removal") >= 0);
    }
  }

  /**
   * Test editing annotations
   */
  public void editAnnotationTest() {

    String annotationId = null;

    //Add and remove an annotation
    try {
      assertTrue("Unable to register user", collaborator.register("guest4@mulgara.org", "Guest User 4"));

      annotationId =
          collaborator.addAnnotation("guest4@mulgara.org",
          collaborator.lastAccessKeyCreated, "Guest User 4", "1234567890",
          "This is a test annotation with a single ' quote", "1", "10", "11",
          "100", "101", "50", "50");

      assertTrue("Unable to add annotation for editing test", (annotationId != null) && (annotationId.length() > 0));

      annotationId =
          collaborator.editAnnotation("guest4@mulgara.org",
          collaborator.lastAccessKeyCreated, "Guest User 4", "1234567890",
          annotationId, "This is an edited annotation with a single ' quote",
          "10", "100", "110", "1000", "1010", "500", "500");

      assertTrue("Unable to edit annotation", annotationId != null);
    } catch (SOAPException ex) {
      ex.printStackTrace();
      fail("Failed to add/edit annotation");
    }
  }

  /**
   * Test retrieving annotations
   */
  public void retrieveAnnotationsTest() {

    String annotationId = null;

    //Add and remove an annotation
    try {

      assertTrue("Unable to register user",
          collaborator.register("guest5@mulgara.org", "Guest User 5"));

      assertTrue("Expect document to be updated for retrieval",
          collaborator.checkAnnotationUpdates("guest5@mulgara.org",
          collaborator.lastAccessKeyCreated, "newdocumentId"));

      annotationId =
          collaborator.addAnnotation("guest5@mulgara.org",
          collaborator.lastAccessKeyCreated, "Guest User 5", "newdocumentId",
          "Annotation text one", "1", "10", "11", "100", "101", "50", "50");

      annotationId =
          collaborator.addAnnotation("guest5@mulgara.org",
          collaborator.lastAccessKeyCreated, "Guest User 5", "newdocumentId",
          "Annotation text two", "2", "20", "22", "200", "202", "52", "52");

      assertTrue("Unable to add annotation for retreving test",
          (annotationId != null) && (annotationId.length() > 0));

      assertTrue("Expect document to be updated for retrieval",
          collaborator.checkAnnotationUpdates("guest5@mulgara.org",
          collaborator.lastAccessKeyCreated, "newdocumentId"));

      String results =
          collaborator.retrieveAnnotations("guest5@mulgara.org",
          collaborator.lastAccessKeyCreated, "newdocumentId");

      assertTrue("Unable to retrieve annotation",
          (results != null) && (results.length() > 0) &&
          (results.indexOf("newdocumentId") >= 0));

      assertTrue("Expect document not to be updated for retrieval",
          collaborator.checkAnnotationUpdates("guest5@mulgara.org",
          collaborator.lastAccessKeyCreated, "newdocumentId") == false);
    } catch (SOAPException ex) {
      ex.printStackTrace();
      fail("Failed to add/retrieve annotation");
    } finally {
      // force the teardown to close the collaborator
      closeCollaborator = true;
    }

  }
}
