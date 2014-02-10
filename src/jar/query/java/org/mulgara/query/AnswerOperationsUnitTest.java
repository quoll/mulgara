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

// Log4J
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.util.ResultSetRow;
import org.mulgara.util.MemoryResultSet;

/**
 * Test case for {@link AnswerOperations}.
 *
 * @created 2004-03-09
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
public class AnswerOperationsUnitTest extends TestCase {

  /** Logger. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(AnswerOperationsUnitTest.class);

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
  public AnswerOperationsUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.addTest(new AnswerOperationsUnitTest("test1Equal"));
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
   * Test #1 on {@link AnswerOperations#equal}.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void test1Equal() throws Exception {
    /*
         MemoryResultSet expected = new MemoryResultSet(new String[] { "x", "y" });
         ResultSetRow row = new ResultSetRow(expected);
         row.setObject("x", "X2");
         row.setObject("y", "Y2");
         expected.addRow(row);

         row = new ResultSetRow(expected);
         row.setObject("x", "X1");
         row.setObject("y", "Y1");
         expected.addRow(row);
     */

    assertTrue(AnswerOperations.equal(answer, answer));
  }

  /**
   * Populate the test answer.
   *
   * @throws QueryException EXCEPTION TO DO
   * @throws SQLException EXCEPTION TO DO
   */
  protected void setUp() throws TuplesException, SQLException {

    MemoryResultSet trs1 = new MemoryResultSet(new String[] {
        "x", "y"});
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
}
