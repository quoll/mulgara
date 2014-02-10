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

package org.mulgara.query;

// JUnit
import junit.framework.*;

// Java 2 standard packages
import java.net.*;
import java.util.*;

// Log4J
// import org.apache.log4j.Logger;

// Locally written packages.
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;

/**
 * Purpose: Test case for {@link Query}.
 *
 * @created 2001-10-23
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @copyright &copy;2001-2004 <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class QueryUnitTest extends TestCase {

  /**
   * Test instance.
   */
  private Query query;

  // /** Logger. */
  // private static final Logger logger = Logger.getLogger(QueryUnitTest.class);

  /**
   * Constructs a new answer test with the given name.
   *
   * @param name the name of the test
   */
  public QueryUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTest(new QueryUnitTest("test1Equals"));
    suite.addTest(new QueryUnitTest("test2Equals"));
    suite.addTest(new QueryUnitTest("test3Equals"));
    suite.addTest(new QueryUnitTest("testClone"));
    return suite;
  }

  /**
   * Default text runner.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Create test instance.
   *
   * @throws Exception EXCEPTION TO DO
   */
  @SuppressWarnings("unchecked")
  public void setUp() throws Exception {
    query = new Query(
        Arrays.asList(new SelectElement[] { new Variable("x") }), // variable list
        new GraphResource(new URI("x:m")),      // model expression
        new ConstraintImpl(new Variable("x"),   // constraint expression
        new URIReferenceImpl(new URI("x:p")),
        new LiteralImpl("o")),
        null,                                   // no having
        (List<Order>)Collections.EMPTY_LIST,    // no ordering
        null,                                   // no limit
        0,                                      // zero offset
        true,                                   // no duplicates
        new UnconstrainedAnswer());
  }

  //
  // Test cases
  //

  /**
   * Test on {@link Query}'s clone method.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testClone() throws Exception {
    Query copied = (Query) query.clone();
    assertTrue(copied != query);
    assertEquals(copied, query);
  }

  /**
   * Test #1 on {@link Query#equals}. This tests equality by reference.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void test1Equals() throws Exception {
    Query query2 = query;
    assertTrue(query.equals(query2));
  }

  /**
   * Test #2 on {@link Query#equals}. This tests equality by value.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  @SuppressWarnings("unchecked")
  public void test2Equals() throws Exception {
    // Compose test instances
    Query query2 = new Query(
        Arrays.asList(new SelectElement[] { new Variable("x") }), // variable list
        new GraphResource(new URI("x:m")),      // model expression
        new ConstraintImpl(new Variable("x"),   // constraint expression
        new URIReferenceImpl(new URI("x:p")),
        new LiteralImpl("o")),
        null,                                  // no having
        (List<Order>)Collections.EMPTY_LIST,   // no ordering
        null,                                  // no limit
        0,                                     // zero offset
        true,                                  // no duplicates
        new UnconstrainedAnswer());

    // These truths we hold to be self-evident:
    assertTrue(query.equals(query2));
    assertTrue(query2.equals(query));
  }

  /**
   * Test #3 on {@link Query#equals}. This tests for inequality with
   * <code>null</code>.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void test3Equals() throws Exception {
    assertTrue(!query.equals(null));
  }
}
