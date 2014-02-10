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

package org.mulgara.jrdf;

// Java 2 standard packages
import java.net.*;

// Third party packages
import junit.framework.*;
import org.apache.log4j.*;

// JRDF
import org.jrdf.graph.*;

// Mulgara
import org.mulgara.server.*;
import org.mulgara.server.driver.*;

/**
 * Unit test for server-side JRDF Graph representing a mulgara model.
 *
 * @created 2004-10-13
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/04/03 10:22:46 $
 *
 * @maintenanceAuthor $Author: tomadams $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class JRDFGraphUnitTest extends AbstractGraphTest {

  /**
   * init the logging class
   */
  private static Logger logger =
    Logger.getLogger(JRDFGraphUnitTest.class.getName());

  /** name used for the server */
  private static String SERVER_NAME = "server1";

  /** name of the model */
  private static String MODEL_NAME = "itqlGraphModel";

  /** URI for the mulgara server */
  private static URI serverURI = null;

  /** URI for the test model */
  private static URI modelURI = null;

  /** The session used by the graph */
  private LocalJRDFSession session = null;

  /** Last returned graph  (requires closing) */
  private JRDFGraph graph = null;

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public JRDFGraphUnitTest(String name) {
    super(name);
    //Logger.getRootLogger().setLevel(Level.ERROR);
  }

  /**
   * Create a graph implementation.
   *
   * @return A new ItqlGraphUnitTest.
   * @throws Exception
   */
  public Graph newGraph() throws Exception {

    //reset graph
    if (graph != null) {
      graph.close();
    }

    //reset model
    this.dropModel(modelURI);
    this.createModel(modelURI);

    //create and return graph
    graph = new JRDFGraph(session, modelURI);
    return graph;
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new JRDFGraphUnitTest("testEmpty"));
    suite.addTest(new JRDFGraphUnitTest("testFactory"));
    suite.addTest(new JRDFGraphUnitTest("testAddition"));
    suite.addTest(new JRDFGraphUnitTest("testRemoval"));
    suite.addTest(new JRDFGraphUnitTest("testContains"));
    suite.addTest(new JRDFGraphUnitTest("testFinding"));
    suite.addTest(new JRDFGraphUnitTest("testIteration"));
    return suite;
  }

  public void testEmpty() throws Exception {

    try {

      super.testEmpty();
    } catch (Exception e) {

      e.printStackTrace();
      throw e;
    }
  }

  // wrap exception in a RuntimeException since JRDF has a bug missing the Exception signature
  public void testFactory() {

    try {

      super.testFactory();
    } catch (Exception e) {

      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void testAddition() throws Exception {

    try {

      super.testAddition();
    } catch (Exception e) {

      e.printStackTrace();
      throw e;
    }
  }

  public void testRemoval() throws Exception {

    try {

      super.testRemoval();
    } catch (Exception e) {

      e.printStackTrace();
      throw e;
    }
  }

  public void testContains() throws Exception {

    try {

      super.testContains();
    } catch (Exception e) {

      e.printStackTrace();
      throw e;
    }
  }

  public void testFinding() throws Exception {

    try {

      super.testFinding();
    } catch (Exception e) {

      e.printStackTrace();
      throw e;
    }
  }

  public void testIteration() throws Exception {

    try {

      super.testIteration();
    } catch (Exception e) {

      e.printStackTrace();
      throw e;
    }
  }

  /**
   * Default test runner.
   *
   * @param args The command line arguments
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    junit.textui.TestRunner.run(suite());
  }

  //set up and tear down

  /**
   * Initialise members.
   *
   * @throws Exception if something goes wrong
   */
  public void setUp() throws Exception {

    // Store persistence files in the temporary directory
    try {

      String hostname = InetAddress.getLocalHost().getCanonicalHostName();
      serverURI = new URI("rmi", hostname, "/" + SERVER_NAME, null);
      modelURI = new URI("rmi", hostname, "/" + SERVER_NAME, MODEL_NAME);

      SessionFactory sessionFactory = SessionFactoryFinder.newSessionFactory(serverURI, false);
      this.session = (LocalJRDFSession) sessionFactory.newJRDFSession();

      //initialize model
      this.createModel(modelURI);

      //let superclass set up too
      super.setUp();
    } catch (Exception exception) {

      exception.printStackTrace();

      //try to tear down first
      try {

        tearDown();
      } catch (Throwable t) {
        logger.error("Encountered exception in exception handler. Ignoring.", t);
      }

      throw exception;
    }
  }

  /**
   * The teardown method for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void tearDown() throws Exception {

    this.dropModel(modelURI);

    //close the graph
    if (graph != null) {
      graph.close();
    }

    //close the session
    if (session != null) {
      session.close();
    }

    //allow super to close down too
    super.tearDown();
  }

  /**
   * Returns an answer that contains all the statements for the graph.
   *
   * @param modelURI URI
   * @throws Exception
   */
  private void createModel(URI modelURI) throws Exception {

    this.session.createModel(modelURI, Session.MULGARA_GRAPH_URI);
  }

  /**
   * Returns an answer that contains all the statements for the graph.
   *
   * @param modelURI URI
   * @throws Exception
   */
  private void dropModel(URI modelURI) throws Exception {

    try {

      this.session.removeModel(modelURI);
    } catch (Exception e) {

      e.printStackTrace();
      throw e;
    }
  }
}
