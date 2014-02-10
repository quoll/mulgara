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
import org.mulgara.query.Variable;
import static org.mulgara.query.filter.value.Bool.TRUE;
import static org.mulgara.store.tuples.Tuples.UNBOUND;

/**
 * Test case for {@link LeftJoin}. This is the
 * <a href="http://www.w3.org/TR/rdf-sparql-query/#optionals">OPTIONAL</a> operation in SPARQL.
 *
 * @created 2008-04-08
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @copyright &copy; 2008 <A href="http://www.topazproject.org/">The Topaz Project</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Open Software License v3.0</a>
 */
public class LeftJoinUnitTest extends TestCase {

  /**Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(LeftJoinUnitTest.class);

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
   * @param name the name of the test
   */
  public LeftJoinUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {
    TestSuite testSuite = new TestSuite();
    testSuite.addTest(new LeftJoinUnitTest("testNoCommonVars"));
    testSuite.addTest(new LeftJoinUnitTest("testOneIntersectingVar"));
    testSuite.addTest(new LeftJoinUnitTest("testIntersectingVars"));
    return testSuite;
  }

  /**
   * Default text runner.
   * @param args The command line arguments
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Create test instance.
   */
  public void setUp() {
  }

  /**
   * The teardown method for JUnit
   */
  public void tearDown() {
    // null implementation
  }

  //
  // Test cases
  //

  /**
   * Test {@link LeftJoin}. When passed two arguments without common
   * variables, an IllegalArgumentException should be thrown.
   * @throws Exception if query fails when it should have succeeded
   */
  public void testNoCommonVars() throws Exception {

    Tuples lhs = TuplesFactory.newInstance().newTuples(
            new MemoryTuples(x, 1).and(y, 2).or(x, 3).and(y, 4));

    Tuples rhs = TuplesFactory.newInstance().newTuples(
            new MemoryTuples(z, 5).and(w, 6).or(z, 7).and(w, 8));

    Tuples tuples = new LeftJoin(lhs, rhs, TRUE, null);
    tuples.beforeFirst();
    assertTrue(tuples.next());
    assertTrue(tuples.next());
    assertFalse(tuples.next());

    TuplesTestingUtil.closeTuples(new Tuples[] { lhs, rhs, tuples });
  }


  /**
   * Test {@link LeftJoin}. When passed arguments with some common variables,
   * the result should be the first argument, plus any matching rows from the
   * second, or unbound in the columns from the RHS.
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
    lhs.appendTuple(new long[] {11, 12});
    lhs.appendTuple(new long[] {13, 14});

    LiteralTuples rhs = new LiteralTuples(rvars);
    rhs.appendTuple(new long[] {4, 11});
    rhs.appendTuple(new long[] {6, 12});
    rhs.appendTuple(new long[] {12, 31});
    rhs.appendTuple(new long[] {12, 32});
    rhs.appendTuple(new long[] {15, 16});

    Tuples actual = new LeftJoin(lhs, rhs, TRUE, null);
//    Tuples actual = TuplesOperations.sort(opt);
//
//    // check the variables
//    Variable[] variables = opt.getVariables();
//    assertEquals(3, variables.length);
//    assertEquals(x, variables[0]);
//    assertEquals(y, variables[1]);
//    assertEquals(z, variables[2]);

    Variable[] variables = actual.getVariables();
    assertEquals(3, variables.length);
    assertEquals(x, variables[0]);
    assertEquals(y, variables[1]);
    assertEquals(z, variables[2]);

    // First, try a straightforward iteration through all rows
    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] {1, 2, UNBOUND});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {3, 4, 11});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {5, 6, 12});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {7, 8, UNBOUND});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {9, 10, UNBOUND});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {11, 12, 31});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {11, 12, 32});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {13, 14, UNBOUND});
    assertTrue(!actual.next());

    // Second, try prefix searches
    actual.beforeFirst(new long[] { 7 }, 0);
    TuplesTestingUtil.testTuplesRow(actual, new long[] {7, 8, UNBOUND });
    assertTrue(!actual.next());

    // Third, try more prefix searches
    actual.beforeFirst(new long[] { 9, 10 }, 0);
    TuplesTestingUtil.testTuplesRow(actual, new long[] {9, 10, UNBOUND });
    assertTrue(!actual.next());

    // Fourth, try a prefix search into the last column
    actual.beforeFirst(new long[] { 9, 10, UNBOUND }, 0);
    TuplesTestingUtil.testTuplesRow(actual, new long[] {9, 10, UNBOUND });
    assertTrue(!actual.next());

    // Fifth, try a prefix search into the last column with a bound value
    actual.beforeFirst(new long[] { 5, 6, 12 }, 0);
    TuplesTestingUtil.testTuplesRow(actual, new long[] {5, 6, 12 });
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
    lhs.appendTuple(new long[] {12, 11});
    lhs.appendTuple(new long[] {14, 13});

    actual = new LeftJoin(rhs, lhs, TRUE, null);

    variables = actual.getVariables();
    assertEquals(3, variables.length);
    assertEquals(y, variables[0]);
    assertEquals(z, variables[1]);
    assertEquals(x, variables[2]);

    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] {4, 11, 3});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {6, 12, 5});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {12, 31, 11});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {12, 32, 11});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {15, 16, UNBOUND});

    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] {actual, lhs, rhs});
  }

  /**
   * Test {@link LeftJoin}. When passed arguments with some common variables,
   * the result should be the first argument, regardless of matching rows from the second.
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

    LeftJoin join = new LeftJoin(lhs, rhs, TRUE, null);
    Tuples actual = TuplesOperations.sort(join);

    // check the variables
    Variable[] variables = actual.getVariables();
    assertEquals(3, variables.length);
    assertEquals(x, variables[0]);
    assertEquals(y, variables[1]);
    assertEquals(z, variables[2]);

    // First, try a straightforward iteration through all rows
    actual.beforeFirst();

    TuplesTestingUtil.testTuplesRow(actual, new long[] {1, 2, 3});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {4, 5, 6});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {7, 5, 6});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {9, 10, 11});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {12, 13, 14});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {15, 7, 14});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {15, 16, 17});
    assertTrue(!actual.next());

    assertEquals(actual.getRowCount(), 7);

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
    TuplesTestingUtil.testTuplesRow(actual, new long[] {15, 7, 14});
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

    actual = TuplesOperations.sort(new LeftJoin(rhs, lhs, TRUE, null));

    actual.beforeFirst();
    TuplesTestingUtil.testTuplesRow(actual, new long[] {5, 6, 4});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {5, 6, 7});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {6, 14, UNBOUND});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {7, 14, 15});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {8, 9, UNBOUND});
    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] {actual, lhs, rhs});

    // variables: rhs intersect lhs != rhs != lhs
    rvars = new String[] {"y", "z", "a"};
    rhs = new LiteralTuples(rvars);
    rhs.appendTuple(new long[] {5, 6, 3});
    rhs.appendTuple(new long[] {6, 14, 1});
    rhs.appendTuple(new long[] {7, 14, 2});
    rhs.appendTuple(new long[] {8, 9, 11});

    actual = TuplesOperations.sort(new LeftJoin(rhs, lhs, TRUE, null));

    actual.beforeFirst();
    TuplesTestingUtil.testTuplesRow(actual, new long[] {5, 6, 3, 4});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {5, 6, 3, 7});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {6, 14, 1, UNBOUND});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {7, 14, 2, 15});
    TuplesTestingUtil.testTuplesRow(actual, new long[] {8, 9, 11, UNBOUND});
    assertTrue(!actual.next());

    TuplesTestingUtil.closeTuples(new Tuples[] {actual, lhs, rhs});
  }

}
