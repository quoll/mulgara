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

package org.mulgara.resolver;

import java.util.*;

// JUnit
import junit.framework.*;

// Log4J
import org.apache.log4j.Logger;

// Local packages
import org.mulgara.store.tuples.*;

/**
 * Test case for {@link WalkFunction}.
 *
 * @created 2004-06-15
 *
 * @author Andrew Newman
 *
 * @version $Revision: 1.3 $
 *
 * @modified $Date: 2005/01/05 04:58:24 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 * @copyright &copy; 2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class WalkFunctionUnitTest extends TestCase {

  /**
   * Logger.
   */
  @SuppressWarnings("unused")
  private Logger logger = Logger.getLogger(WalkFunctionUnitTest.class.getName());

  /**
   * Tuples for testing with.
   */
  LiteralTuples t1, t2;

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public WalkFunctionUnitTest(String name) {

    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTest(new WalkFunctionUnitTest("testWalkFrom1"));
    suite.addTest(new WalkFunctionUnitTest("testWalkFrom2"));
    suite.addTest(new WalkFunctionUnitTest("testWalkFrom5"));
    suite.addTest(new WalkFunctionUnitTest("testWalkFrom6"));
    suite.addTest(new WalkFunctionUnitTest("testWalkFrom7"));

    return suite;
  }

  /**
   * Create test instance.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void setUp() throws Exception {

    String[] var1 = new String[] { "xxx_", "xxx" };
    String[] var2 = new String[] { "xxx", "xxx_" };

    t1 = new LiteralTuples(var1);
    t2 = new LiteralTuples(var2);

    long[][] tuples = new long[][] {
        new long[] { 1, 3 },
        new long[] { 1, 4 },
        new long[] { 1, 5 },
        new long[] { 2, 5 },
        new long[] { 2, 6 },
        new long[] { 3, 4 },
        new long[] { 4, 1 },
        new long[] { 4, 6 },
        new long[] { 4, 7 },
        new long[] { 4, 8 },
        new long[] { 5, 8 },
        new long[] { 6, 2 },
        new long[] { 7, 1 },
        new long[] { 7, 8 },
        new long[] { 7, 9 },
    };

    for (int i = 0; i < tuples.length; i++) {
      ((LiteralTuples) t1).appendTuple(tuples[i]);
      ((LiteralTuples) t2).appendTuple(tuples[i]);
    }
  }

  /**
   * Default text runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {

    junit.textui.TestRunner.run(suite());
  }

  //
  // Test cases
  //

  /**
   * Test a walk a predetermined set of node numbers.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testWalkFrom1() throws Exception {

    // Expected results
    long[][] expectedTuples = new long[][] {
        new long[] { 1, 3 },
        new long[] { 1, 4 },
        new long[] { 1, 5 },
        new long[] { 3, 4 },
        new long[] { 4, 1 },
        new long[] { 4, 6 },
        new long[] { 4, 7 },
        new long[] { 4, 8 },
        new long[] { 5, 8 },
        new long[] { 6, 2 },
        new long[] { 7, 1 },
        new long[] { 7, 8 },
        new long[] { 7, 9 },
        new long[] { 2, 5 },
        new long[] { 2, 6 },
    };

    assertTrue("Same number of tuples in expected as in original",
        expectedTuples.length == 15);
    testTuples("xxx", "zzz", 1l, expectedTuples, t1);
  }

  /**
   * Test a walk a predetermined set of node numbers.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testWalkFrom2() throws Exception {

    // Expected results
    long[][] expectedTuples = new long[][] {
        new long[] { 2, 5 },
        new long[] { 2, 6 },
        new long[] { 5, 8 },
        new long[] { 6, 2 },
    };
    assertTrue("Expected number of tuples is 4",
        expectedTuples.length == 4);
    testTuples("xxx", "zzz", 2l, expectedTuples, t1);

    // Results are switched from original statements.
    expectedTuples = new long[][] {
        new long[] { 2, 6 },
        new long[] { 6, 2 },
        new long[] { 6, 4 },
        new long[] { 4, 1 },
        new long[] { 4, 3 },
        new long[] { 1, 4 },
        new long[] { 1, 7 },
        new long[] { 3, 1 },
        new long[] { 7, 4 },
    };

    assertTrue("Expected number of tuples is 9",
        expectedTuples.length == 9);
    testTuples("xxx", "zzz", 2l, expectedTuples, t2);
  }

  /**
   * Test a walk a predetermined set of node numbers.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testWalkFrom5() throws Exception {

    // Expected results
    long[][] expectedTuples = new long[][] {
        new long[] { 5, 8 },
    };
    testTuples("xxx", "zzz", 5l, expectedTuples, t1);
  }

  /**
   * Test a walk a predetermined set of node numbers.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testWalkFrom6() throws Exception {

    // Expected results
    long[][] expectedTuples = new long[][] {
        new long[] { 6, 2 },
        new long[] { 2, 5 },
        new long[] { 2, 6 },
        new long[] { 5, 8 },
    };
    testTuples("xxx", "zzz", 6l, expectedTuples, t1);

    // Results are switched from original statements.
    expectedTuples = new long[][] {
        new long[] { 6, 2 },
        new long[] { 6, 4 },
        new long[] { 2, 6 },
        new long[] { 4, 1 },
        new long[] { 4, 3 },
        new long[] { 1, 4 },
        new long[] { 1, 7 },
        new long[] { 3, 1 },
        new long[] { 7, 4 },
    };
    testTuples("xxx", "zzz", 6l, expectedTuples, t2);
  }

  /**
   * Test a walk a predetermined set of node numbers.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testWalkFrom7() throws Exception {

    // Expected results
    long[][] expectedTuples = new long[][] {
        new long[] { 7, 1 },
        new long[] { 7, 8 },
        new long[] { 7, 9 },
        new long[] { 1, 3 },
        new long[] { 1, 4 },
        new long[] { 1, 5 },
        new long[] { 3, 4 },
        new long[] { 4, 1 },
        new long[] { 4, 6 },
        new long[] { 4, 7 },
        new long[] { 4, 8 },
        new long[] { 5, 8 },
        new long[] { 6, 2 },
        new long[] { 2, 5 },
        new long[] { 2, 6 },
    };
    assertTrue("Same number of tuples in expected as in original",
        expectedTuples.length == 15);
    testTuples("xxx", "zzz", 7l, expectedTuples, t1);
  }

  /**
   * Test a walk a predetermined set of node numbers.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testTuples(String column1, String column2, long subject,
      long[][] expectedTuples, LiteralTuples initialTuples) throws Exception {

    // Start at subject 1, and unknown predicate.
    LiteralTuples given = new LiteralTuples(new String[] { column1 + "_"});
    given.appendTuple(new long[] { subject });

    // Visited.
    Set<Long> visited = new HashSet<Long>();
    visited.add(new Long(subject));

    // Results are the subject and object.
    LiteralTuples inferredResult = new LiteralTuples(new String[] { column1,
      column2 });

    // Walk graph.
    WalkFunction.walkStatements(initialTuples, given, visited, inferredResult);

    inferredResult.beforeFirst();
    inferredResult.next();
    int index = 0;
    do {

      long tuple[] = new long[] {
          inferredResult.getColumnValue(0),
          inferredResult.getColumnValue(1)
      };
      assertTrue("Expected tuple result: " + expectedTuples[index][0] + "," +
          expectedTuples[index][1] + " but was: " + tuple[0] + "," + tuple[1],
          expectedTuples[index][0] == tuple[0] &&
          expectedTuples[index][1] == tuple[1]);
//      System.err.println("Was: " + tuple[0] + "," + tuple[1]);
      index++;
    }
    while (inferredResult.next());
  }
}
