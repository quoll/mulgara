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
import java.util.*;

// Third party packages
import org.apache.log4j.Logger;  // Apache Log4J
import junit.framework.*;          // JUnit

// Local packages
import org.mulgara.query.TuplesException;
import org.mulgara.query.Variable;

/**
 * Test case for {@link MemoryTuples}.
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
public class MemoryTuplesUnitTest extends TestCase {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(MemoryTuplesUnitTest.class);

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public MemoryTuplesUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTest(new MemoryTuplesUnitTest("test1equals"));
    suite.addTest(new MemoryTuplesUnitTest("test2equals"));
    suite.addTest(new MemoryTuplesUnitTest("test1unequals"));
    suite.addTest(new MemoryTuplesUnitTest("test2unequals"));
    suite.addTest(new MemoryTuplesUnitTest("testCtor"));

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
   * Test {@link MemoryTuples}. When passed a list of zero arguments, the result
   * of a join should be unconstrained.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testCtor() throws Exception {

    Variable x = new Variable("x");
    Variable y = new Variable("y");

    Tuples operand = new MemoryTuples(x, 1).and(y, 2).or(x, 3).and(y, 4);

    assertEquals(new HashSet<Variable>(Arrays.asList(new Variable[] {
        x, y})),
        new HashSet<Variable>(Arrays.asList(operand.getVariables())));

    try {

      operand.getColumnValue(0);
      fail("Shouldn't be able to get a value before first row");
    }
    catch (TuplesException e) {

      // correct behavior
    }

    operand.beforeFirst();
    assertTrue(operand.next());
    assertEquals(1, operand.getColumnValue(0));
    assertEquals(2, operand.getColumnValue(1));

    try {

      operand.getColumnValue( -1);
      fail("Shouldn't be able to get a value before first column");
    }
    catch (TuplesException e) {

      // correct behavior
    }

    try {

      operand.getColumnValue(2);
      fail("Shouldn't be able to get a value after last column");
    }
    catch (TuplesException e) {

      // correct behavior
    }

    assertTrue(operand.next());
    assertEquals(3, operand.getColumnValue(0));
    assertEquals(4, operand.getColumnValue(1));

    assertTrue(!operand.next());

    try {

      operand.getColumnValue(0);
      fail("Shouldn't be able to get a value after last row");
    }
    catch (TuplesException e) {

      // correct behavior
    }
  }

  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void test1equals() throws Exception {

    Variable x = new Variable("x");
    Variable y = new Variable("y");

    Tuples lhs = new MemoryTuples(x, 1).and(y, 2).or(x, 3).and(y, 4);
    Tuples rhs = new MemoryTuples(x, 1).and(y, 2).or(x, 3).and(y, 4);

    assertEquals(lhs, rhs);
  }

  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void test2equals() throws Exception {

    Variable x = new Variable("x");
    Variable y = new Variable("y");

    Tuples lhs = new MemoryTuples(x, 1).or(x, 3).and(y, 4);
    Tuples rhs = new MemoryTuples(x, 1).or(x, 3).and(y, 4);

    assertEquals(lhs, rhs);
  }

  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void test1unequals() throws Exception {

    Variable x = new Variable("x");
    Variable y = new Variable("y");

    Tuples lhs = new MemoryTuples(x, 1).and(y, 3).or(x, 2).and(y, 4);
    Tuples rhs = new MemoryTuples(x, 1).and(y, 2).or(x, 3).and(y, 4);

    assertTrue("Tuples: " + lhs + " and " + rhs + " should be unequal",
        !lhs.equals(rhs));
  }

  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void test2unequals() throws Exception {

    Variable x = new Variable("x");
    Variable y = new Variable("y");
    Variable z = new Variable("z");

    Tuples lhs = new MemoryTuples(x, 1).or(x, 3).and(y, 4).or(z, 5);
    Tuples rhs = new MemoryTuples(x, 1).or(x, 3).and(y, 4).or(z, 4);

    assertTrue("Tuples: " + lhs + " and " + rhs + " should be unequal",
        !lhs.equals(rhs));
  }
}
