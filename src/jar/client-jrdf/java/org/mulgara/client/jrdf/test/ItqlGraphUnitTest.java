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
 * of the notices in the Sou Code files of the Original Code. You
 * should use the text of this Exhibit A rather than the text found in the
 * Original Code Source Code for Your Modifications.]
 *
 */

package org.mulgara.client.jrdf.test;

// Java packages
import java.net.*;
import java.util.*;

// JRDF
import org.jrdf.graph.*;
import org.jrdf.util.*;

// Junit
import junit.framework.*;

// Local packages
import org.mulgara.client.jrdf.*;
import org.mulgara.client.jrdf.itql.*;
import org.mulgara.server.*;
import org.mulgara.server.driver.*;

/**
 * Unit test for client-side JRDF Graph representing a mulgara model (modelURI)
 * and uses an ItqlInterpreterBean and a Session.
 *
 * @created 2004-08-24
 *
 * @author <a href="mailto:robert.turner@tucanatech.com">Robert Turner</a>
 *
 * @version $Revision: 1.10 $
 *
 * @modified $Date: 2005/01/26 21:06:36 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy;2001 <a href="http://www.pisoftware.com/">Plugged In
 *   Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ItqlGraphUnitTest extends AbstractGraphTest {

  /** name used for the server */
  private static String SERVER_NAME = "server1";

  /** name of the model */
  private static String MODEL_NAME = "itqlGraphModel";

  /** URI for the mulgara server */
  private static URI serverURI = null;

  /** URI for the test model */
  private static URI modelURI = null;

  /** The session used by the graph (and setting up) */
  private JRDFSession session = null;


  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public ItqlGraphUnitTest(String name) {
    super(name);
  }

  /**
   * Create a graph implementation.
   *
   * @return A new ItqlGraphUnitTest.
   * @throws Exception
   */
  public Graph newGraph() throws Exception {

    //reset graph
    this.dropModel(modelURI);
    this.createModel(modelURI);

    //create and return graph
    return new ClientGraph(new ItqlGraphProxy(session, modelURI));
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new ItqlGraphUnitTest("testEmpty"));
    suite.addTest(new ItqlGraphUnitTest("testFactory"));
    suite.addTest(new ItqlGraphUnitTest("testSimpleAddition"));
    suite.addTest(new ItqlGraphUnitTest("testUnclosedIteratorCloseGraph"));
    suite.addTest(new ItqlGraphUnitTest("testCloseGraph"));

    /**
     *  BlankNodes are not supported
     *
     *  @todo implement BlankNode support
     */

//    suite.addTest(new ItqlGraphUnitTest("testAddition"));
//    suite.addTest(new ItqlGraphUnitTest("testRemoval"));
//    suite.addTest(new ItqlGraphUnitTest("testContains"));
//    suite.addTest(new ItqlGraphUnitTest("testFinding"));
//    suite.addTest(new ItqlGraphUnitTest("testIteration"));
    return suite;
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

  /**
   * Test bug where closing the graph without closing the iterator causes a
   * Concurrent modification error.
   *
   * @throws Exception if there's an error adding statements.
   */
  public void testUnclosedIteratorCloseGraph() throws Exception{

    // add in a triple by nodes
    graph.add(ref1, ref1, ref1);

    assertFalse(graph.isEmpty());
    assertEquals(1, graph.getNumberOfTriples());

    // Create iterator.
    graph.find(null, null, null);

    // Close the graph.
    graph.close();
  }

  /**
   * Normal test for creating an iterator.
   *
   * @throws Exception if there's an error adding statements.
   */
  public void testCloseGraph() throws Exception{

    // add in a triple by nodes
    graph.add(ref1, ref1, ref1);

    assertFalse(graph.isEmpty());
    assertEquals(1, graph.getNumberOfTriples());

    // Create iterator.
    ClosableIterator<Triple> iter = graph.find(null, null, null);
    iter.close();

    // Close the graph.
    graph.close();
  }

  /**
   * Tests adding literals and URIs.only.
   *
   * @throws Exception if there's an error adding statements.
   */
  public void testSimpleAddition() throws Exception{

    // add in a triple by nodes
    graph.add(ref1, ref1, ref1);

    assertFalse(graph.isEmpty());
    assertEquals(1, graph.getNumberOfTriples());

    // add in a whole triple
    Triple triple2 = graph.getElementFactory().createTriple(ref2, ref1, ref1);
    graph.add(triple2);

    assertFalse(graph.isEmpty());
    assertEquals(2, graph.getNumberOfTriples());

    // add in the first triple again
    graph.add(ref1, ref1, ref1);

    assertFalse(graph.isEmpty());
    assertEquals(2, graph.getNumberOfTriples());

    // add in the second whole triple again
    Triple triple2b = graph.getElementFactory().createTriple(ref2, ref1, ref1);
    graph.add(triple2b);
    assertFalse(graph.isEmpty());
    assertEquals(2, graph.getNumberOfTriples());

    // and again
    graph.add(triple2);
    assertFalse(graph.isEmpty());
    assertEquals(2, graph.getNumberOfTriples());

    // Add using iterator
    ArrayList<Triple> list = new ArrayList<Triple>();
    list.add(graph.getElementFactory().createTriple(ref2, ref2, ref1));
    list.add(graph.getElementFactory().createTriple(ref1, ref2, ref2));

    graph.add(list.iterator());
    assertFalse(graph.isEmpty());
    assertEquals(4, graph.getNumberOfTriples());
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

      //get session
      SessionFactory sessionFactory = SessionFactoryFinder.newSessionFactory(serverURI, true);
      this.session = (JRDFSession) sessionFactory.newJRDFSession();

      //initialize model
      this.createModel(modelURI);

      //let superclass set up too
      super.setUp();
    } catch (Exception exception) {

      //try to tear down first
      try {
        tearDown();
      } catch (Throwable t) { }

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

    this.session.createModel(modelURI, new URI("http://mulgara.org/mulgara#Model"));
  }

  /**
   * Returns an answer that contains all the statements for the graph.
   *
   * @param modelURI URI
   * @throws Exception
   */
  private void dropModel(URI modelURI) throws Exception {

    this.session.removeModel(modelURI);
  }
}
