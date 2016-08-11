/*
 * The contents of this file are subject to the Open Software License
 * Version 3.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.opensource.org/licenses/osl-3.0.txt
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 */

package org.mulgara.store.tuples;

// Third party packages
import junit.framework.*; // JUnit
import org.apache.log4j.Logger; // Log4J

// Locally written packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Test case for {@link Difference}.
 *
 * @created 2005-03-31
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @copyright &copy; 2003 <A href="http://www.topazproject.org/">The Topaz Project</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Open Software License v3.0</a>
 */
public class DifferenceUnitTest extends TestCase {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(DifferenceUnitTest.class);

  /** Test variable. */
  final Variable x = new Variable("x");

  /** Test variable. */
  final Variable y = new Variable("y");

  /** Test variable. */
  final Variable z = new Variable("z");

  /** Test variable. */
  final Variable w = new Variable("w");

  /** Test variable. */
  final Variable u = new Variable("u");

  /** Test variable. */
  final Variable v = new Variable("v");

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public DifferenceUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite testSuite = new TestSuite();
    testSuite.addTest(new DifferenceUnitTest("testNoCommonVars"));
    testSuite.addTest(new DifferenceUnitTest("testOneIntersectingVar"));
    testSuite.addTest(new DifferenceUnitTest("testIntersectingVars"));

    // The following test requires suffix matching with unbound prefixes, which is not working

    // testSuite.addTest(new DifferenceUnitTest("testNullJoin"));
    // testSuite.addTest(new DifferenceUnitTest("testNullPropagation"));
    // testSuite.addTest(new DifferenceUnitTest("testLeadingPrefixNull"));

    return testSuite;

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
   * Test {@link Difference}. When passed two arguments without common
   * variables, an IllegalArgumentException should be thrown.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testNoCommonVars() throws Exception {

    Tuples lhs =
        TuplesFactory.newInstance().newTuples(new MemoryTuples(x, 1).and(y, 2)
        .or(x, 3).and(y,
        4));

    Tuples rhs =
        TuplesFactory.newInstance().newTuples(new MemoryTuples(z, 5).and(w, 6)
        .or(z, 7).and(w,
        8));

    try {
      new Difference(lhs, rhs);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      /* no-op */
    }

    TuplesTestingUtil.closeTuples(new Tuples[] { lhs, rhs });
  }

  /**
   * Test {@link Difference}. When passed arguments with some common variables,
   * the result should be the first argument, minus any matching rows from the second.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testOneIntersectingVar() throws Exception {
    String[] lvars = new String[] {"x", "y"};
    String[] rvars = new String[] {"y", "z"};

    LiteralTuples lhs = new LiteralTuples(lvars);
    lhs.appendTuple(new long[] {1, 2});
    lhs.appendTuple(new long[] {3, 4});
    lhs.appendTuple(new long[] {5, 6});
    lhs.appendTuple(new long[] {7, 8});
    lhs.appendTuple(new long[] {9, 10});

    LiteralTuples rhs = new LiteralTuples(rvars);
    rhs.appendTuple(new long[] {4, 11});
    rhs.appendTuple(new long[] {6, 12});
    rhs.appendTuple(new long[] {11, 13});

    Tuples diff = new Difference(lhs, rhs);
    Tuples actual = TuplesOperations.sort(diff);

    // check the variables
    Variable[] variables = diff.getVariables();
    assertEquals(2, variables.length);
    assertEquals(x, variables[0]);
    assertEquals(y, variables[1]);

    variables = actual.getVariables();
    assertEquals(2, variables.length);
    assertEquals(x, variables[0]);
    assertEquals(y, variables[1]);

    // First, try a straightforward iteration through all rows
    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] {1, 2});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {7, 8});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {9, 10});
    assertTrue(!actual.next());

    // Second, try prefix searches
    actual.beforeFirst(new long[] { 7 }, 0);
    TuplesTestingUtil.testTuplesRow(actual, new long[] {7, 8 });

    assertTrue(!actual.next());

    // Third, try more prefix searches
    actual.beforeFirst(new long[] { 9, 10 }, 0);
    TuplesTestingUtil.testTuplesRow(actual, new long[] {9, 10 });

    assertTrue(!actual.next());
    actual.close();

    // Try joining in the opposite order (RHS to LHS)

    // first, re-order the lhs
    lvars = new String[] {"y", "x"};
    lhs = new LiteralTuples(lvars);
    lhs.appendTuple(new long[] {2, 1});
    lhs.appendTuple(new long[] {4, 3});
    lhs.appendTuple(new long[] {6, 5});
    lhs.appendTuple(new long[] {8, 7});
    lhs.appendTuple(new long[] {10, 9});

    actual = new Difference(rhs, lhs);

    variables = actual.getVariables();
    assertEquals(2, variables.length);
    assertEquals(y, variables[0]);
    assertEquals(z, variables[1]);

    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] {11, 13});

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] {actual, lhs, rhs});
  }

  /**
   * Test {@link Difference}. When passed arguments with some common variables,
   * the result should be the first argument, minus any matching rows from the second.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testIntersectingVars() throws Exception {
    // lhs intersect rhs = rhs
    String[] lvars = new String[] {"x", "y", "z"};
    String[] rvars = new String[] {"y", "z"};

    LiteralTuples lhs = new LiteralTuples(lvars);
    lhs.appendTuple(new long[] {1, 2, 3});
    lhs.appendTuple(new long[] {4, 5, 6});
    lhs.appendTuple(new long[] {7, 5, 6});
    lhs.appendTuple(new long[] {9, 10, 11});
    lhs.appendTuple(new long[] {12, 13, 14});
    lhs.appendTuple(new long[] {15, 16, 17});
    lhs.appendTuple(new long[] {15, 7, 14});

    LiteralTuples rhs = new LiteralTuples(rvars);
    rhs.appendTuple(new long[] {5, 6});
    rhs.appendTuple(new long[] {6, 14});
    rhs.appendTuple(new long[] {7, 14});
    rhs.appendTuple(new long[] {8, 9});

    Tuples actual = TuplesOperations.sort(new Difference(lhs, rhs));

    // check the variables
    Variable[] variables = actual.getVariables();
    assertEquals(3, variables.length);
    assertEquals(x, variables[0]);
    assertEquals(y, variables[1]);
    assertEquals(z, variables[2]);

    // First, try a straightforward iteration through all rows
    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] {1, 2, 3});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {9, 10, 11});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {12, 13, 14});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {15, 16, 17});
    assertTrue(!actual.next());

    assertEquals(actual.getRowCount(), 4);

    // Second, try prefix searches
    actual.beforeFirst(new long[] {9}, 0);
    TuplesTestingUtil.testTuplesRow(actual, new long[] {9, 10, 11});
    assertTrue(!actual.next());

    // Third, try more prefix searches
    actual.beforeFirst(new long[] {12, 13}, 0);
    TuplesTestingUtil.testTuplesRow(actual, new long[] {12, 13, 14});
    assertTrue(!actual.next());

    // Now test the end
    actual.beforeFirst(new long[] {15}, 0);
    TuplesTestingUtil.testTuplesRow(actual, new long[] {15, 16, 17});
    assertTrue(!actual.next());

    actual.close();

    // variables: lhs intersect rhs = lhs
    // first, reorder the lhs
    lvars = new String[] {"y", "z", "x"};
    lhs = new LiteralTuples(lvars);
    lhs.appendTuple(new long[] {2, 3, 1});
    lhs.appendTuple(new long[] {5, 6, 4});
    lhs.appendTuple(new long[] {5, 6, 7});
    lhs.appendTuple(new long[] {10, 11, 9});
    lhs.appendTuple(new long[] {13, 14, 12});
    lhs.appendTuple(new long[] {16, 17, 15});
    lhs.appendTuple(new long[] {7, 14, 15});

    actual = TuplesOperations.sort(new Difference(rhs, lhs));

    actual.beforeFirst();
    TuplesTestingUtil.testTuplesRow(actual, new long[] {6, 14});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {8, 9});
    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] {actual, lhs, rhs});

    // variables: rhs intersect lhs != rhs != lhs
    rvars = new String[] {"y", "z", "a"};
    rhs = new LiteralTuples(rvars);
    rhs.appendTuple(new long[] {5, 6, 3});
    rhs.appendTuple(new long[] {6, 14, 1});
    rhs.appendTuple(new long[] {7, 14, 2});
    rhs.appendTuple(new long[] {8, 9, 11});

    actual = TuplesOperations.sort(new Difference(rhs, lhs));

    actual.beforeFirst();
    TuplesTestingUtil.testTuplesRow(actual, new long[] {6, 14, 1});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {8, 9, 11});
    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] {actual, lhs, rhs});
  }

  /**
   * Test {@link Difference}.n the presence of unconstrained elements.
   */
  public void testNullJoin() throws Exception {
    String[] vars = new String[] {"x", "y"};

    LiteralTuples lhs = new LiteralTuples(vars);
    lhs.appendTuple(new long[] {1, 1});
    lhs.appendTuple(new long[] {2, 2});

    LiteralTuples rhs = new LiteralTuples(vars);
    rhs.appendTuple(new long[] {Tuples.UNBOUND, 2});
    rhs.appendTuple(new long[] {1, Tuples.UNBOUND});

    Tuples actual = TuplesOperations.sort(new Difference(lhs, rhs));

    actual.beforeFirst();

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { actual, lhs, rhs } );
  }


  /**
   * Test {@link Difference}.n the presence of unconstrained elements.
   * <pre>
   *   x y z           x z       x y z
   *  | 5 6 7 |      | * 1 |   | 5 6 7 |
   *  | 2 3 1 |  -   | 2 * | =
   *  | 2 4 * |
   * </pre>
   */
  public void testNullPropagation() throws Exception {
    String[] lvars = new String[] { "x", "y", "z" };
    final long[][] lhsValues = new long[][] {
        new long[] { 5, 6, 7 },
        new long[] { 2, 3, 1 },
        new long[] { 2, 4, Tuples.UNBOUND } };
    LiteralTuples lhs = LiteralTuples.create(lvars, lhsValues);

    String[] rvars = new String[] { "x", "z" };
    final long[][] rhsValues = new long[][] {
        new long[] { Tuples.UNBOUND, 1 },
        new long[] { 2, Tuples.UNBOUND } };
    LiteralTuples rhs = LiteralTuples.create(rvars, rhsValues);

    Tuples actual = TuplesOperations.sort(new Difference(lhs, rhs));

    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] { 5, 6, 7 } );

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { actual, lhs, rhs });
  }


  /**
   * Test {@link Difference}.n the presence of unconstrained elements.
   * <pre>
   *   x y           x y       x y
   * | * 2 |       | * 3 | =
   * | 1 * |  join | 4 * |
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

    Tuples actual = TuplesOperations.sort(new Difference(lhs, rhs));

    actual.beforeFirst();

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { actual, lhs, rhs });
  }

  /**
   * Internal method used during debugging. This method writes the contents of a tuples
   * to STDOUT.
   * @param t The tuples to dump
   * @throws TuplesException If there was an exception accessing the tuples.
   */
  @SuppressWarnings("unused")
  private void dumpTuples(Tuples t) throws TuplesException {
    Variable[] v = t.getVariables();
    for (int i = 0; i < v.length; i++) {
      System.out.print(v[i].getName() + " ");
    }
    System.out.println();
    t.beforeFirst();
    while (t.next()) {
      String line = new String();
      for (int i = 0; i < v.length; i++) {
        line += t.getColumnValue(t.getColumnIndex(v[i])) + " ";
      }
      System.out.println(line);
    }
  }
}
