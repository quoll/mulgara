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

// Third party packages
import junit.framework.*; // JUnit
import org.apache.log4j.Logger; // Log4J

// Locally written packages
import org.mulgara.query.Variable;

/**
 * Test case for {@link UnboundJoin}.
 *
 * @created 2003-09-04
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.12 $
 *
 * @modified $Date: 2005/03/07 19:42:40 $
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
public class UnboundJoinUnitTest extends TestCase {

  /**
   * Logger.
   */
  private static final Logger logger = Logger.getLogger(UnboundJoinUnitTest.class);

  /**
   * Description of the Field
   */
  final Variable x = new Variable("x");

  /**
   * Description of the Field
   */
  final Variable y = new Variable("y");

  /**
   * Description of the Field
   */
  final Variable z = new Variable("z");

  /**
   * Description of the Field
   */
  final Variable w = new Variable("w");

  /**
   * Description of the Field
   */
  final Variable u = new Variable("u");

  /**
   * Description of the Field
   */
  final Variable v = new Variable("v");

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public UnboundJoinUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite testSuite = new TestSuite();
    testSuite.addTest(new UnboundJoinUnitTest("testZeroOperands"));
    testSuite.addTest(new UnboundJoinUnitTest("testOneOperand"));
    testSuite.addTest(new UnboundJoinUnitTest("testCartesianDyad"));
    testSuite.addTest(new UnboundJoinUnitTest("testCartesianTriad"));
    testSuite.addTest(new UnboundJoinUnitTest("testTwoOperands"));
    testSuite.addTest(new UnboundJoinUnitTest("testSuffixJoin"));
    testSuite.addTest(new UnboundJoinUnitTest("testSuffixJoin2"));
    testSuite.addTest(new UnboundJoinUnitTest("testNullJoin"));
    testSuite.addTest(new UnboundJoinUnitTest("testNullPrefixBoundInSuffix"));
    testSuite.addTest(new UnboundJoinUnitTest("testNullPropagation"));
    testSuite.addTest(new UnboundJoinUnitTest("testLeadingPrefixNull"));
    testSuite.addTest(new UnboundJoinUnitTest("testPartialMGR36"));

    return testSuite;

    //return new TestSuite(UnboundJoinTest.class);
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

  /**
   * The teardown method for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void tearDown() throws Exception {

    // null implementation
  }

  //
  // Test cases
  //

  /**
   * Test {@link UnboundJoin}. When passed a list of zero arguments, the result
   * of a join should be unconstrained.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testZeroOperands() throws Exception {

    Tuples joinedTuples = new UnboundJoin(new Tuples[] {});
    assertTrue(joinedTuples.isUnconstrained());
    assertEquals(0, ((UnboundJoin)joinedTuples).getNrGroups());
  }

  /**
   * Test {@link UnboundJoin}. When passed a single argument, the result should
   * be identical to that argument.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testOneOperand() throws Exception {

    Tuples operand =
        TuplesFactory.newInstance().newTuples(new MemoryTuples(x, 1).and(y, 2)
        .or(x, 3).and(y, 4));
    assert operand != null;

    Tuples joinedTuples = new UnboundJoin(new Tuples[] {operand});
    assertEquals(1, ((UnboundJoin)joinedTuples).getNrGroups());

    joinedTuples.beforeFirst();

    TuplesTestingUtil.testTuplesRow(joinedTuples, new long[] { 1, 2 } );
    TuplesTestingUtil.testTuplesRow(joinedTuples, new long[] { 3, 4 } );

    assertTrue(!joinedTuples.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { joinedTuples, operand } );
  }

  /**
   * Test {@link UnboundJoin}. When passed two arguments without common
   * variables, the result should be a cartesian product.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testCartesianDyad() throws Exception {

    Tuples lhs =
        TuplesFactory.newInstance().newTuples(new MemoryTuples(x, 1).and(y, 2)
        .or(x, 3).and(y, 4));

    Tuples rhs =
        TuplesFactory.newInstance().newTuples(new MemoryTuples(z, 5).and(w, 6)
        .or(z, 7).and(w, 8));

    Tuples joined = new UnboundJoin(new Tuples[] {lhs, rhs});
    assertEquals(2, ((UnboundJoin)joined).getNrGroups());

    Variable[] variables = joined.getVariables();

    TuplesTestingUtil.testVariables(new Variable[] { x, y, z, w }, variables);
    TuplesTestingUtil.testTuplesRow(joined, new Variable[] { x, y, z, w } );

    TuplesTestingUtil.checkBeforeFirst(joined);
    joined.beforeFirst();

    TuplesTestingUtil.testTuplesRow(joined, new long[] { 1, 2, 5, 6 } );
    TuplesTestingUtil.testTuplesRow(joined, new long[] { 1, 2, 7, 8 } );
    TuplesTestingUtil.testTuplesRow(joined, new long[] { 3, 4, 5, 6 } );
    TuplesTestingUtil.testTuplesRow(joined, new long[] { 3, 4, 7, 8 } );

    assertTrue(!joined.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { joined, lhs, rhs });
  }

  /**
   * Test {@link UnboundJoin}. When passed three arguments without common
   * variables, the result should be a cartesian product.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testCartesianTriad() throws Exception {

    Tuples lhs =
        TuplesFactory.newInstance().newTuples(new MemoryTuples(x, 1).and(y, 2)
        .or(x, 3).and(y, 4));

    Tuples mhs =
        TuplesFactory.newInstance().newTuples(new MemoryTuples(z, 5).and(w, 6)
        .or(z, 7).and(w, 8));

    Tuples rhs =
        TuplesFactory.newInstance().newTuples(new MemoryTuples(u, 9).and(v, 10)
        .or(u, 11).and(v, 12));

    Tuples joined = new UnboundJoin(new Tuples[] {lhs, mhs, rhs});
    assertEquals(3, ((UnboundJoin)joined).getNrGroups());

    Variable[] variables = joined.getVariables();
    assertEquals(6, variables.length);
    assertEquals(x, variables[0]);
    assertEquals(y, variables[1]);
    assertEquals(z, variables[2]);
    assertEquals(w, variables[3]);
    assertEquals(u, variables[4]);
    assertEquals(v, variables[5]);

    assertEquals(0, joined.getColumnIndex(x));
    assertEquals(1, joined.getColumnIndex(y));
    assertEquals(2, joined.getColumnIndex(z));
    assertEquals(3, joined.getColumnIndex(w));
    assertEquals(4, joined.getColumnIndex(u));
    assertEquals(5, joined.getColumnIndex(v));

    TuplesTestingUtil.checkBeforeFirst(joined);
    joined.beforeFirst();

    TuplesTestingUtil.testTuplesRow(joined, new long[] { 1, 2, 5, 6, 9, 10 } );
    TuplesTestingUtil.testTuplesRow(joined, new long[] { 1, 2, 5, 6, 11, 12 } );
    TuplesTestingUtil.testTuplesRow(joined, new long[] { 1, 2, 7, 8, 9, 10 } );
    TuplesTestingUtil.testTuplesRow(joined, new long[] { 1, 2, 7, 8, 11, 12 } );
    TuplesTestingUtil.testTuplesRow(joined, new long[] { 3, 4, 5, 6, 9, 10 } );
    TuplesTestingUtil.testTuplesRow(joined, new long[] { 3, 4, 5, 6, 11, 12 } );
    TuplesTestingUtil.testTuplesRow(joined, new long[] { 3, 4, 7, 8, 9, 10 } );
    TuplesTestingUtil.testTuplesRow(joined, new long[] { 3, 4, 7, 8, 11, 12 } );

    assertTrue(!joined.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { joined, lhs, rhs, mhs } );
  }

  /**
   * Test {@link UnboundJoin}. When passed two arguments with common variables,
   * the result should be a natural join.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testTwoOperands() throws Exception {
    String[] lvars = new String[] {"x", "y"};
    String[] rvars = new String[] {"y", "z"};

    LiteralTuples lhs = new LiteralTuples(lvars);
    lhs.appendTuple(new long[] {1, 2});
    lhs.appendTuple(new long[] {3, 4});

    LiteralTuples rhs = new LiteralTuples(rvars);
    rhs.appendTuple(new long[] {2, 5});
    rhs.appendTuple(new long[] {4, 6});

    TuplesFactory.newInstance().newTuples(new MemoryTuples(y, 2).and(z, 5)
        .or(y, 4).and(z, 6));

    UnboundJoin joined = new UnboundJoin(new Tuples[] {lhs, rhs});
    assertEquals(1, joined.getNrGroups());
    Tuples actual = TuplesOperations.sort(joined);

    // First, try a straightforward iteration through all rows
    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] { 1, 2, 5 } );
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 3, 4, 6 } );

    assertTrue(!actual.next());

    // Second, try prefix searches
    actual.beforeFirst(new long[] { 3 }, 0);
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 3, 4, 6 } );

    assertTrue(!actual.next());

    // Third, try more prefix searches
    actual.beforeFirst(new long[] { 3, 4, 6 }, 0);
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 3, 4, 6 } );

    assertTrue(!actual.next());
    actual.close();

    // Try joining in the opposite order (RHS to LHS)
    actual = new UnboundJoin(new Tuples[] {
        rhs, lhs});

    Variable[] variables = actual.getVariables();
    assertEquals(3, variables.length);
    assertEquals(y, variables[0]);
    assertEquals(z, variables[1]);
    assertEquals(x, variables[2]);

    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] { 2, 5, 1 } );
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 4, 6, 3 } );

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { actual, lhs, rhs} );
  }

  /**
   * Test {@link UnboundJoin}. Perform a join with conditions which requires
   * iterative rather than indexed resolution
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testSuffixJoin() throws Exception {

    Tuples lhs =
        TuplesFactory.newInstance().newTuples(new MemoryTuples(x, 1).and(y, 4)
        .or(x, 1)
        .and(y, 5)
        .or(x, 2)
        .and(y, 6)
        .or(x, 3).and(y,
        7));

    Tuples rhs =
        TuplesFactory.newInstance().newTuples(new MemoryTuples(y, 4).and(x, 1)
        .or(y, 6).and(x,
        2));

    Tuples actual = new UnboundJoin(new Tuples[] {
        lhs, rhs});

    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] { 1, 4 } );
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 2, 6 } );

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { lhs, rhs, actual } );
  }

  /**
   * Test {@link UnboundJoin}. Perform another join with conditions which
   * requires iterative rather than indexed resolution
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testSuffixJoin2() throws Exception {

    Variable e = new Variable("e");
    Variable v = new Variable("vcard");
    Variable f = new Variable("_from");

    Tuples lhs =
        TuplesFactory.newInstance().newTuples(new MemoryTuples(e, 62).and(v, 37)
        .and(f, 36)
        .or(e, 89)
        .and(v, 78)
        .and(f, 36)
        .or(e, 106)
        .and(v, 97)
        .and(f, 36)
        .or(e, 121)
        .and(v, 113)
        .and(f, 36));

    Tuples rhs =
        TuplesFactory.newInstance().newTuples(new MemoryTuples(e, 89).and(f, 36));

    Tuples actual = new UnboundJoin(new Tuples[] {
        lhs, rhs});

    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] { 89, 78, 36 } );

//    assertTrue(actual.next());
//    assertEquals(89, actual.getColumnValue(0));
//    assertEquals(78, actual.getColumnValue(1));
//    assertEquals(36, actual.getColumnValue(2));

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { actual, rhs, lhs } );
  }

  /**
   * Test {@link UnboundJoin}.n the presence of unconstrained elements.
   */
  public void testNullJoin() throws Exception {
    String[] vars = new String[] {"x", "y"};

    LiteralTuples lhs = new LiteralTuples(vars);
    lhs.appendTuple(new long[] {Tuples.UNBOUND, 2});
    lhs.appendTuple(new long[] {1, Tuples.UNBOUND});

    LiteralTuples rhs = new LiteralTuples(vars);
    rhs.appendTuple(new long[] {1, 1});
    rhs.appendTuple(new long[] {2, 2});

    Tuples actual = TuplesOperations.sort(new UnboundJoin(new Tuples[] {lhs,
        rhs}));

    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] { 1, 1 } );
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 2, 2 } );

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { actual, lhs, rhs } );
  }


  /**
  * Test {@link UnboundJoin}.n correctly binds a variable from a rhs operand when
  * unbound in the lhs.
  */
 public void testNullPrefixBoundInSuffix() throws Exception {
    String[] lvars = new String[] { "x", "z" };
    final long[][] lhsValues = new long[][] {
        new long[] { Tuples.UNBOUND, 1 },
        new long[] { 2, Tuples.UNBOUND },
    };

    LiteralTuples lhs = LiteralTuples.create(lvars, lhsValues);

    String[] rvars = new String[] { "x", "y", "z" };
    final long[][] rhsValues = new long[][] {
        new long[] { 2, 3, 1 }
    };

    LiteralTuples rhs = LiteralTuples.create(rvars, rhsValues);

    Tuples actual = TuplesOperations.sort(new UnboundJoin(new Tuples[] {lhs, rhs}));
    actual.beforeFirst();

    // Note:  If only one row is returned this is *NOT* a bug, but currently planned
    // implementation should return a duplicate row.
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 2, 1, 3} );
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 2, 1, 3} );

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { actual, lhs, rhs } );
  }

  /**
   * Test {@link UnboundJoin}.n the presence of unconstrained elements.
   * <pre>
   *   x y z           x z       x y z
   *  | 5 6 7 |      | * 1 |   | 2 3 1 |
   *  | 2 3 1 | join | 2 * | = | 2 3 1 |
   *  | 2 4 * |                | 2 4 * |
   *                           | 2 4 1 |
   * </pre>
   */
  public void testNullPropagation() throws Exception {
    String[] lvars = new String[] { "x", "y", "z" };
    final long[][] lhsValues = new long[][] {
        new long[] { 2, 3, 1 },
        new long[] { 2, 4, Tuples.UNBOUND } };
    LiteralTuples lhs = LiteralTuples.create(lvars, lhsValues);

    String[] rvars = new String[] { "x", "z" };
    final long[][] rhsValues = new long[][] {
        new long[] { Tuples.UNBOUND, 1 },
        new long[] { 2, Tuples.UNBOUND } };
    LiteralTuples rhs = LiteralTuples.create(rvars, rhsValues);

    Tuples actual = TuplesOperations.sort(new UnboundJoin(new Tuples[] {lhs, rhs}));

    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] { 2, 3, 1 } );
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 2, 3, 1 } );
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 2, 4, Tuples.UNBOUND } );
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 2, 4, 1} );

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { actual, lhs, rhs });
  }


  /**
   * Test {@link UnboundJoin}.n the presence of unconstrained elements.
   * <pre>
   *   x y           x y       x y
   * | * 2 |       | * 3 |   | 1 3 |
   * | 1 * |  join | 4 * | = | 4 2 |
   * </pre>
   */
  public void testLeadingPrefixNull() throws Exception {
    String[] vars = new String[] { "x", "y" };
    final long[][] lhsValues = new long[][] {
        new long[] { Tuples.UNBOUND, 2 },
        new long[] { 1, Tuples.UNBOUND } };

    final long[][] rhsValues = new long[][] {
        new long[] { Tuples.UNBOUND, 3 },
        new long[] { 4, Tuples.UNBOUND } };

    LiteralTuples lhs = LiteralTuples.create(vars, lhsValues);
    LiteralTuples rhs = LiteralTuples.create(vars, rhsValues);

    Tuples actual = TuplesOperations.sort(new UnboundJoin(new Tuples[] {lhs, rhs}));

//    logger.warn("testLeadingPrefixNull result = " + actual);

    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] { 1, 3} );
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 4, 2} );

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { actual, lhs, rhs });
  }

  /**
   * Test {@link UnboundJoin}.n the expected final join of MGR-36 case 2.
   * <pre>
   *   s          s p o x       s p o x
   * | 1 | join | 1 2 3 * | = | 1 2 3 * |
   *            | 2 4 5 * |   | 1 5 3 2 |
   *            | 2 4 6 * |   | 1 6 3 2 |
   *            | 1 5 3 2 |
   *            | 1 6 3 2 |
   * </pre>
   */
  public void testPartialMGR36() throws Exception {
    String[] lvars = new String[] { "s" };
    final long[][] lhsValues = new long[][] {
        new long[] { 1 } };

    String[] rvars = new String[] { "s", "p", "o", "x" };
    final long[][] rhsValues = new long[][] {
        new long[] { 1, 2, 3, Tuples.UNBOUND },
        new long[] { 1, 5, 3, 2 },
        new long[] { 1, 6, 3, 2 },
        new long[] { 2, 4, 5, Tuples.UNBOUND },
        new long[] { 2, 4, 6, Tuples.UNBOUND } };


    LiteralTuples lhs = LiteralTuples.create(lvars, lhsValues);
    LiteralTuples rhs = LiteralTuples.create(rvars, rhsValues);

    UnboundJoin joined = new UnboundJoin(new Tuples[] {lhs, rhs});
    assertEquals(1, joined.getNrGroups());
    Tuples actual = TuplesOperations.sort(joined);

    logger.warn("testPartialMGR36 result = " + actual);

    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] { 1, 2, 3, Tuples.UNBOUND } );
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 1, 5, 3, 2 } );
    TuplesTestingUtil.testTuplesRow(actual, new long[] { 1, 6, 3, 2 } );

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { actual, lhs, rhs });
  }
}
