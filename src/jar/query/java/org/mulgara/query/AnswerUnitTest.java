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
import java.sql.*;
import java.util.*;

// Log4J
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.util.ResultSetRow;
import org.mulgara.util.MemoryResultSet;

/**
 * Purpose: Test case for {@link Answer}.
 *
 * @created 2001-10-09
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2003
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class AnswerUnitTest extends TestCase {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(AnswerUnitTest.class);

  /**
   * Test instance.
   * <table>
   *   <thead>
   *     <tr><th>x</th> <th>y</th></tr>
   *   </thead>
   *   <tbody>
   *     <tr><td>X1</td><td>Y1</td></tr>
   *     <tr><td>X2</td><td>Y2</td></tr>
   *   </tbody>
   * </table>
   */
  private AnswerImpl answer;

  /**
   * Constructs a new answer test with the given name.
   *
   * @param name the name of the test
   */
  public AnswerUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTest(new AnswerUnitTest("testAccess"));
    //suite.addTest(new AnswerTest("test1getResultSet"));
    suite.addTest(new AnswerUnitTest("testCopyConstructor"));
    suite.addTest(new AnswerUnitTest("testEmptyConstructor"));

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

  //
  // Test cases
  //

  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testCopyConstructor() throws Exception {

    Answer copied = new AnswerImpl(answer);
    assertTrue(answer != copied);
    assertEquals(answer, copied);
    copied.close();
  }

  /**
   * A unit test for JUnit
   *
   * @throws Exception EXCEPTION TO DO
   */
  public void testEmptyConstructor() throws Exception {

    List<Variable> variables = Arrays.asList(new Variable[] { new Variable("x") });
    AnswerImpl empty = new AnswerImpl(variables);
    assertTrue(!empty.next());
    empty.close();
    //assertTrue(empty.getNumberOfVariables() == 1);
    //assertTrue(empty.getVariable(0).equals(new Variable("x")));
  }

  /**
   * Test #1 on {@link Answer#getDependentResultSet}.
   *
   * @throws Exception if query fails when it should have succeeded
   */

  /*
       TODO: Figure out the exact meaning of null, zero column, and zero row Answers
     public void test1getDependentResultSet() throws Exception
     {
    MemoryResultSet expected = new MemoryResultSet(new String[] {});
    Set variables = new HashSet();
    MemoryResultSet result = answer.getDependentResultSet(variables);
    assertEqualsIgnoreOrder(expected, result);
     }
   */

  /**
   * Test #2 on {@link Answer#getDependentResultSet}.
   *
   * @throws Exception if query fails when it should have succeeded
   */

  /*
     public void test2getDependentResultSet() throws Exception
     {
    MemoryResultSet expected = new MemoryResultSet(new String[] {"x", "y"});
    MemoryResultSet.Row row;
    row = expected.new Row();
      row.setObject("x", "X2");
      row.setObject("y", "Y2");
    row = expected.new Row();
      row.setObject("x", "X1");
      row.setObject("y", "Y1");
    Set variables = new HashSet();
    variables.add(new Variable("x"));
    MemoryResultSet result = answer.getDependentResultSet(variables);
    assertEqualsIgnoreOrder(expected, result);
     }
   */

  /**
   * Test #3 on {@link Answer#getDependentResultSet}.
   *
   * @throws Exception if query fails when it should have succeeded
   */

  /*
     public void test3getDependentResultSet() throws Exception
     {
    MemoryResultSet expected = new MemoryResultSet(new String[] {"x", "y"});
    MemoryResultSet.Row row;
    row = expected.new Row();
      row.setObject("x", "X2");
      row.setObject("y", "Y2");
    row = expected.new Row();
      row.setObject("x", "X1");
      row.setObject("y", "Y1");
    Set variables = new HashSet();
    variables.add(new Variable("y"));
    variables.add(new Variable("x"));
    MemoryResultSet result = answer.getDependentResultSet(variables);
    assertEqualsIgnoreOrder(expected, result);
     }
   */

  /**
   * Test #4 on {@link Answer#getDependentResultSet}.
   *
   * @throws Exception if query fails when it should have succeeded
   */

  /*
     public void test4getDependentResultSet() throws Exception
     {
    MemoryResultSet expected = new MemoryResultSet(new String[] {"x", "y"});
    MemoryResultSet.Row row;
    row = expected.new Row();
      row.setObject("x", "X2");
      row.setObject("y", "Y2");
    row = expected.new Row();
      row.setObject("x", "X1");
      row.setObject("y", "Y1");
    Set variables = new HashSet();
    variables.add(new Variable("z"));
    variables.add(new Variable("y"));
    variables.add(new Variable("x"));
    MemoryResultSet result = answer.getDependentResultSet(variables);
    assertEqualsIgnoreOrder(expected, result);
     }
   */

  /**
   * Test basic accessor methods.
   */
  public void testAccess() throws Exception
  {
    Variable x = new Variable("x");
    Variable y = new Variable("y");

    assertEquals(x, answer.getVariable(0));
    assertEquals(y, answer.getVariable(1));

    assertEquals(0, answer.getColumnIndex(x));
    assertEquals(1, answer.getColumnIndex(y));

    assertTrue(answer.next());
    assertEquals("X1", answer.getObject(0));
    assertEquals("Y1", answer.getObject(1));

    assertTrue(answer.next());
    assertEquals("X2", answer.getObject(0));
    assertEquals("Y2", answer.getObject(1));

    assertTrue(!answer.next());
  }

  /**
   * Test #1 on {@link AnswerImpl#getResultSet}.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  /*
  public void test1getResultSet() throws Exception {

    MemoryResultSet expected = new MemoryResultSet(new String[] { "x", "y" });
    ResultSetRow row = new ResultSetRow(expected);
    row.setObject("x", "X2");
    row.setObject("y", "Y2");
    expected.addRow(row);

    row = new ResultSetRow(expected);
    row.setObject("x", "X1");
    row.setObject("y", "Y1");
    expected.addRow(row);

    MulgaraResultSet result =
      AnswerImpl.toMulgaraResultSet(answer.getResultSet());

    assertEqualsIgnoreOrder(expected, result);
  }
  */

  /**
   * Populate the test answer.
   *
   * @throws QueryException EXCEPTION TO DO
   * @throws SQLException EXCEPTION TO DO
   */
  protected void setUp() throws TuplesException, SQLException {

    MemoryResultSet trs1 = new MemoryResultSet(new String[] { "x", "y" });
    ResultSetRow row;
    row = new ResultSetRow(trs1);
    row.setObject("x", "X1");
    row.setObject("y", "Y1");
    trs1.addRow(row);
    row = new ResultSetRow(trs1);
    row.setObject("x", "X2");
    row.setObject("y", "Y2");
    trs1.addRow(row);
    answer = new AnswerImpl(trs1);
  }

  /**
   * Clean up the test answer.
   */
  public void tearDown() {

    answer.close();
  }

  //
  // Internal methods
  //

  /**
   * METHOD TO DO
   *
   * @param expected PARAMETER TO DO
   * @param result PARAMETER TO DO
   * @throws Exception EXCEPTION TO DO
   */
  /*
  private void assertEqualsIgnoreOrder(MulgaraResultSet expected,
    MulgaraResultSet result) throws Exception {

    try {

      assertTrue(expected.equalsIgnoreOrder(result));
    }
     catch (AssertionFailedError e) {

      logger.error("Expected result: " + expected + ", actual result: " +
        result);
      throw e;
    }
  }
  */
}
