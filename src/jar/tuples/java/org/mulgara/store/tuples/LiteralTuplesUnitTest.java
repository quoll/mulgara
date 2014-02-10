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

package org.mulgara.store.tuples;

// Java 2 standard packages
import java.net.*;
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;    // Apache Log4J
import junit.framework.*;          // JUnit

// Local packages
import org.mulgara.query.Cursor;
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Test case for {@link LiteralTuples}.
 *
 * @created 2003-01-10
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:10 $
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
public class LiteralTuplesUnitTest extends TestCase {

  /**
   * Logger.
   */
  private static final Logger logger = Logger.getLogger(LiteralTuplesUnitTest.class);

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public LiteralTuplesUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTest(new LiteralTuplesUnitTest("testVariableConstructor"));
    suite.addTest(new LiteralTuplesUnitTest("testEmptyVariableConstructor"));
    suite.addTest(new LiteralTuplesUnitTest("testStringConstructor"));
    suite.addTest(new LiteralTuplesUnitTest("testEmptyStringConstructor"));
    suite.addTest(new LiteralTuplesUnitTest("testStringVariableEquivalance"));
    suite.addTest(new LiteralTuplesUnitTest("testEmptyIsEmpty"));
    suite.addTest(new LiteralTuplesUnitTest("testSingleton"));
    suite.addTest(new LiteralTuplesUnitTest("testSingletonUnbound"));
    suite.addTest(new LiteralTuplesUnitTest("testMultiple"));
    suite.addTest(new LiteralTuplesUnitTest("testMultipleUnbound"));
    suite.addTest(new LiteralTuplesUnitTest("testMultiplePrefix"));
    suite.addTest(new LiteralTuplesUnitTest("testMultipleUnboundPrefix"));

    return suite;
  }

  /**
   * Default text runner.
   *
   * @param args The command line arguments
   */
  public static void main(String[] args) {

    junit.textui.TestRunner.run(suite());
  }

  /**
   * Create test instance.
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void setUp() throws Exception {

    // null implementation
  }

  //
  // Test cases
  //

  /**
   * Test {@link LiteralTuples}. When passed a list of zero arguments, the result
   * of a join should be unconstrained.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testVariableConstructor() throws Exception {
    Variable[] cvars = new Variable[] { new Variable("x"), new Variable("y"), new Variable("z") };
    LiteralTuples t1 = new LiteralTuples(cvars);
    Variable[] tvars = t1.getVariables();

    for (int i = 0; i < cvars.length; i++) {
      assertEquals(cvars[i], tvars[i]);
    }
  }

  public void testEmptyVariableConstructor() throws Exception {
    Variable[] cvars = new Variable[0];
    LiteralTuples t1 = new LiteralTuples(cvars);
    Variable[] tvars = t1.getVariables();
    assertTrue(tvars.length == 0);
  }

  public void testStringConstructor() throws Exception {
    String[] cvars = new String[] { "x", "y", "z" };
    LiteralTuples t1 = new LiteralTuples(cvars);
    Variable[] tvars = t1.getVariables();

    for (int i = 0; i < cvars.length; i++) {
      assertEquals(new Variable(cvars[i]), tvars[i]);
    }
  }

  public void testEmptyStringConstructor() throws Exception {
    String[] cvars = new String[0];
    LiteralTuples t1 = new LiteralTuples(cvars);
    Variable[] tvars = t1.getVariables();
    assertTrue(tvars.length == 0);
  }

  public void testStringVariableEquivalance() throws Exception {
    String[] svars = new String[] { "x", "y", "z" };
    Variable[] vvars = new Variable[] { new Variable("x"), new Variable("y"), new Variable("z") };

    LiteralTuples t1 = new LiteralTuples(svars);
    LiteralTuples t2 = new LiteralTuples(vvars);

    assertEquals(t1, t2);
  }

  public void testEmptyIsEmpty() throws Exception {
    String[] vars = new String[] { "x", "y", "z" };
    LiteralTuples t1 = new LiteralTuples(vars);
    assertTrue(t1.getRowCardinality() == Cursor.ZERO);
    t1.beforeFirst();
    assertFalse(t1.next());
  }

  public void testSingleton() throws Exception {
    String[] vars = new String[] { "x", "y", "z" };
    LiteralTuples t1 = new LiteralTuples(vars);
    long[] tuple = new long[] { 1, 2, 3 };
    t1.appendTuple(tuple);

    t1.beforeFirst();
    assertTrue(t1.next());

    for (int i = 0; i < tuple.length; i++) {
      assertTrue(t1.getColumnValue(i) == tuple[i]);
    }

    assertFalse(t1.next());
  }


  public void testSingletonUnbound() throws Exception {
    String[] vars = new String[] { "x", "y", "z" };
    LiteralTuples t1 = new LiteralTuples(vars);
    long[] tuple = new long[] { Tuples.UNBOUND, Tuples.UNBOUND, Tuples.UNBOUND };
    t1.appendTuple(tuple);

    t1.beforeFirst();
    assertTrue(t1.next());

    for (int i = 0; i < tuple.length; i++) {
      assertTrue(t1.isColumnEverUnbound(i));
      assertTrue(t1.getColumnValue(i) == tuple[i]);
    }

    assertFalse(t1.next());
  }


  public void testMultiple() throws Exception {
    String[] vars = new String[] { "x", "y" };
    LiteralTuples t1 = new LiteralTuples(vars);
    long[][] tuples = new long[][] {
                        new long[] { 1, 1 },
                        new long[] { 1, 2 },
                        new long[] { 2, 1 },
                        new long[] { 2, 2 } };

    for (int i = 0; i < tuples.length; i++) {
      t1.appendTuple(tuples[i]);
    }

    assertFalse(t1.isColumnEverUnbound(0));
    assertFalse(t1.isColumnEverUnbound(1));
    t1.beforeFirst();

    for (int i = 0; i < tuples.length; i++) {
      assertTrue(t1.next());

      for (int j = 0; j < vars.length; j++) {
        assertTrue(t1.getColumnValue(j) == tuples[i][j]);
      }
    }

    assertFalse(t1.next());
  }

  public void testMultipleUnbound() throws Exception {
    String[] vars = new String[] { "x", "y", "z" };
    LiteralTuples t1 = new LiteralTuples(vars);
    long[][] tuples = new long[][] {
                        new long[] { Tuples.UNBOUND, Tuples.UNBOUND, 1 },
                        new long[] { Tuples.UNBOUND, 1 , 2},
                        new long[] { Tuples.UNBOUND, 2 , 3},
                        new long[] { 1, Tuples.UNBOUND, 4 },
                        new long[] { 1, 1, 5 },
                        new long[] { 1, 2, 6 },
                        new long[] { 2, Tuples.UNBOUND, 7 },
                        new long[] { 2, 1, 8 },
                        new long[] { 2, 2, 9 } };

    for (int i = 0; i < tuples.length; i++) {
      t1.appendTuple(tuples[i]);
    }

    assertTrue(t1.isColumnEverUnbound(0));
    assertTrue(t1.isColumnEverUnbound(1));
    assertFalse(t1.isColumnEverUnbound(2));
    t1.beforeFirst();

    for (int i = 0; i < tuples.length; i++) {
      assertTrue(t1.next());

      for (int j = 0; j < vars.length; j++) {
        assertTrue(t1.getColumnValue(j) == tuples[i][j]);
      }
    }

    assertFalse(t1.next());
  }

  public void testMultiplePrefix() throws Exception {
    String[] vars = new String[] { "x", "y" };
    LiteralTuples t1 = new LiteralTuples(vars);
    long[][] tuples = new long[][] {
                        new long[] { 1, 1 },
                        new long[] { 1, 2 },
                        new long[] { 1, 3 },
                        new long[] { 2, 1 },
                        new long[] { 2, 2 },
                        new long[] { 2, 3 },
                        new long[] { 3, 1 },
                        new long[] { 3, 2 },
                        new long[] { 3, 3 } };

    for (int i = 0; i < tuples.length; i++) {
      t1.appendTuple(tuples[i]);
    }

    assertFalse(t1.isColumnEverUnbound(0));
    assertFalse(t1.isColumnEverUnbound(1));
    t1.beforeFirst(new long[] { 2 }, 0);

    for (int i = 3; tuples[i][0] == 2; i++) {
      assertTrue(t1.next());

      for (int j = 0; j < vars.length; j++) {
        assertTrue(t1.getColumnValue(j) == tuples[i][j]);
      }
    }

    assertFalse(t1.next());
  }

  public void testMultipleUnboundPrefix() throws Exception {
    String[] vars = new String[] { "x", "y", "z" };
    LiteralTuples t1 = new LiteralTuples(vars);
    long[][] tuples = new long[][] {
                        new long[] { Tuples.UNBOUND, Tuples.UNBOUND, 1 },
                        new long[] { Tuples.UNBOUND, 1 , 2},
                        new long[] { Tuples.UNBOUND, 2 , 3},
                        new long[] { 1, Tuples.UNBOUND, 4 },
                        new long[] { 1, 1, 5 },
                        new long[] { 1, 2, 6 },
                        new long[] { 2, Tuples.UNBOUND, 7 },
                        new long[] { 2, 1, 8 },
                        new long[] { 2, 2, 9 } };

    for (int i = 0; i < tuples.length; i++) {
      t1.appendTuple(tuples[i]);
    }

    assertTrue(t1.isColumnEverUnbound(0));
    assertTrue(t1.isColumnEverUnbound(1));
    assertFalse(t1.isColumnEverUnbound(2));
    t1.beforeFirst(new long[] { 1, Tuples.UNBOUND }, 0);

    assertTrue(t1.next());

    for (int j = 0; j < vars.length; j++) {
      assertTrue(t1.getColumnValue(j) == tuples[3][j]);
    }

    assertFalse(t1.next());
  }
}
