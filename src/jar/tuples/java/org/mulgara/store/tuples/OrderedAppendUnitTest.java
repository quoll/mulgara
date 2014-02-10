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
import junit.framework.*;   // JUnit
import org.apache.log4j.*;  // Log4J

// Locally written packages
import org.mulgara.query.Constraint;
import org.mulgara.query.ConstraintImpl;
import org.mulgara.query.Variable;
import org.mulgara.store.statement.StatementStore;

/**
 * Test case for {@link OrderedAppend}.
 *
 * @created 2003-02-10
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
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class OrderedAppendUnitTest extends TestCase {

  /** Logger. */
  @SuppressWarnings("unused")
  private Logger logger = Logger.getLogger(OrderedAppendUnitTest.class.getName());

  /**
   * Get line separator.
   */
  private static final String eol = System.getProperty("line.separator");

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public OrderedAppendUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTest(new OrderedAppendUnitTest("testZeroOperands"));
    suite.addTest(new OrderedAppendUnitTest("testOneOperand"));
    suite.addTest(new OrderedAppendUnitTest("testTwoOperands"));
    suite.addTest(new OrderedAppendUnitTest("testTwoOperandsWithGaps"));
    suite.addTest(new OrderedAppendUnitTest("testRenamingColumns"));
    suite.addTest(new OrderedAppendUnitTest("testIteration"));
    suite.addTest(new OrderedAppendUnitTest("testEmptyIteration"));

    return suite;
  }

  /*
     public void testUnionCompatibilityConditioning() throws Exception
     {
    Variable x = new Variable("x");
    Variable y = new Variable("y");
    Tuples lhs = new MemoryTuples(x, 1).and(y, Tuples.UNBOUND)
                            .or(x, 3);
    Tuples rhs = new MemoryTuples(x, 2).and(y, 6)
                            .or(x, 4).and(y, 5);
    assertEquals(new MemoryTuples(x, 1)
                            .or(x, 2).and(y, 6)
                            .or(x, 3)
                            .or(x, 4).and(y, 5),
                 new MemoryTuples(new OrderedAppend(new Tuples[] {lhs, rhs})));
     }
   */

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
   * Test {@link OrderedAppend}. When passed a list of zero arguments, the
   * result of an append should be empty.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testZeroOperands() throws Exception {

    Tuples appendedTuples = new OrderedAppend(new Tuples[] {});
    assertEquals(new MemoryTuples(), new MemoryTuples(appendedTuples));
  }

  /**
   * Test {@link OrderedAppend}. When passed a single argument, the result
   * should be identical to that argument.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testOneOperand() throws Exception {

    Variable x = new Variable("x");
    Variable y = new Variable("y");

    Tuples operand = new MemoryTuples(x, 1).and(y, 2).or(x, 3).and(y, 4);

    Tuples appendedTuples = new OrderedAppend(new Tuples[] {
        operand});

    Tuples test = new MemoryTuples(appendedTuples);
    assertEquals(operand, test);

    operand.close();
    appendedTuples.close();
    test.close();
  }

  /**
   * Test {@link OrderedAppend}. When passed two arguments with common
   * variables, the result should be correctly sorted.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testTwoOperands() throws Exception {

    Variable x = new Variable("x");
    Variable y = new Variable("y");

    Tuples lhs = new MemoryTuples(x, 1).and(y, 8).or(x, 3).and(y, 7);

    Tuples rhs = new MemoryTuples(x, 2).and(y, 6).or(x, 4).and(y, 5);

    Tuples std = new MemoryTuples(x, 1).and(y, 8).or(x, 2).and(y, 6).or(x, 3)
        .and(y, 7).or(x, 4).and(y, 5);
    Tuples test = new OrderedAppend(new Tuples[] {lhs, rhs});
    Tuples wrapper = new MemoryTuples(test);

    assertEquals(std, wrapper);

    lhs.close();
    rhs.close();
    std.close();
    test.close();
    wrapper.close();
  }

  /**
   * Test {@link OrderedAppend} in the presence of unspecified column values.
   * When passed two arguments with common variables, the result should be
   * correctly sorted.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testTwoOperandsWithGaps() throws Exception {

    Variable x = new Variable("x");
    Variable y = new Variable("y");

    Tuples lhs = new MemoryTuples(x, 1).and(y, 8).or(x, 3);

    Tuples rhs = new MemoryTuples(x, 2).and(y, 6).or(x, 4).and(y, 5);

    Tuples std = new MemoryTuples(x, 1).and(y, 8).or(x, 2).and(y, 6).or(x, 3)
        .or(x, 4).and(y, 5);
    Tuples test = new OrderedAppend(new Tuples[] {lhs, rhs});
    Tuples wrapper = new MemoryTuples(test);

    assertEquals(std, wrapper);

    lhs.close();
    rhs.close();
    std.close();
    test.close();
    wrapper.close();
  }

  /**
   * Test that renaming the default column names to new values.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testRenamingColumns() throws Exception {

    Variable subject = StatementStore.VARIABLES[0];
    Variable predicate = StatementStore.VARIABLES[1];

    Tuples lhs = new MemoryTuples(subject, 1).and(predicate, 8).or(subject, 3);

    Tuples rhs =
        new MemoryTuples(subject, 2).and(predicate, 6).or(subject,
        4).and(predicate, 5);

    Tuples test = new MemoryTuples(subject, 1).and(predicate, 8).or(subject, 2).
            and(predicate, 6).or(subject, 3).or(subject, 4).and(predicate, 5);

    assertEquals(
        eol + "{$Subject  $Predicate  (4 rows)" + eol +
        "[000001  000008  ]" + eol +
        "[000002  000006  ]" + eol +
        "[000003  000000  ]" + eol +
        "[000004  000005  ]" + eol + "}",
        test.toString());

    Tuples[] testArray = new Tuples[] { lhs, rhs };

    OrderedAppend newTuples = new OrderedAppend(testArray);

    Constraint newConstraint =
        new ConstraintImpl(new Variable("x"), new Variable("y"), new Variable("z"));

    newTuples.renameVariables(newConstraint);

    assertEquals(eol + "{$x      $y      (unevaluated, 4 rows max, 4 rows expected)" + eol +
        "[000001  000008  ]" + eol +
        "[000002  000006  ]" + eol +
        "[000003  000000  ]" + eol +
        "[000004  000005  ]" + eol + "}",
    newTuples.toString());

    lhs.close();
    rhs.close();
    test.close();
    newTuples.close();
    for (int i = 0; i < testArray.length; i++) testArray[i].close();
  }


  /**
   * Test {@link OrderedAppend}.  Earlier versions of OrderedAppend did not iterate correctly.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testIteration() throws Exception {

    Variable x = new Variable("x");
    Variable y = new Variable("y");

    Tuples lhs = new MemoryTuples(x, 1).and(y, 8).or(x, 3).and(y, 7);

    Tuples rhs = new MemoryTuples(x, 2).and(y, 6).or(x, 4).and(y, 5);

    OrderedAppend oa = new OrderedAppend(new Tuples[] {lhs, rhs});

    oa.beforeFirst(new long[] {}, 0);

    assertEquals(true, oa.next());
    assertEquals(1, oa.getColumnValue(0));
    assertEquals(8, oa.getColumnValue(1));

    assertEquals(true, oa.next());
    assertEquals(2, oa.getColumnValue(0));
    assertEquals(6, oa.getColumnValue(1));

    assertEquals(true, oa.next());
    assertEquals(3, oa.getColumnValue(0));
    assertEquals(7, oa.getColumnValue(1));

    assertEquals(true, oa.next());
    assertEquals(4, oa.getColumnValue(0));
    assertEquals(5, oa.getColumnValue(1));

    assertEquals(false, oa.next());

    lhs.close();
    rhs.close();
    oa.close();
  }


  /**
   * Test {@link OrderedAppend}.  Earlier versions of OrderedAppend did not iterate correctly.
   *
   * @throws Exception if iteration fails when it should have succeeded
   */
  public void testEmptyIteration() throws Exception {

    Variable x = new Variable("x");
    Variable y = new Variable("y");

    Tuples lhs = new MemoryTuples(x, 1).and(y, 8).or(x, 3).and(y, 7);

    Tuples rhs = new MemoryTuples();

    OrderedAppend oa = new OrderedAppend(new Tuples[] {lhs, rhs});

    oa.beforeFirst(new long[] {}, 0);

    assertEquals(true, oa.next());
    assertEquals(1, oa.getColumnValue(0));
    assertEquals(8, oa.getColumnValue(1));

    assertEquals(true, oa.next());
    assertEquals(3, oa.getColumnValue(0));
    assertEquals(7, oa.getColumnValue(1));

    assertEquals(false, oa.next());

    lhs.close();
    rhs.close();
    oa.close();
  }
}
