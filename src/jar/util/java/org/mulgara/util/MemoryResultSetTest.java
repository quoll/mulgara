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

package org.mulgara.util;

// third party packages
import junit.framework.*;

// Java 2 standard packages
import java.sql.SQLException;
// import java.util.*;

// JUnit
import org.apache.log4j.Logger;

// Log4J

/**
 * Test case for {@link MemoryResultSet}.
 *
 * @created 2001-07-12
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 * @version $Revision: 1.9 $
 * @modified $Date: 2005/01/05 04:59:29 $
 * @maintenanceAuthor $Author: newmana $
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 * @copyright &copy; 2001-2003 <A href="http://www.PIsoftware.com/">Plugged In
 *      Software Pty Ltd</A>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class MemoryResultSetTest extends TestCase {

  /** Logger. Named after the class. */
  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(MemoryResultSetTest.class);

  /** Test object. */
//  private MemoryResultSet testResultSet;

  /**
   * CONSTRUCTOR MemoryResultSetTest TO DO
   *
   * @param name PARAMETER TO DO
   */
  public MemoryResultSetTest(String name) {
    super(name);
  }

  /**
   * Creates a test suite with various different output and compares the output.
   *
   * @return The test suite
   */
  public static TestSuite suite() {

    TestSuite suite = new TestSuite();

    //suite.addTest(new MemoryResultSetTest("test1Join"));
    //suite.addTest(new MemoryResultSetTest("test1RemoveDuplicateRows"));
    //suite.addTest(new MemoryResultSetTest("test2Join"));
    //suite.addTest(new MemoryResultSetTest("test2RemoveDuplicateRows"));
    //suite.addTest(new MemoryResultSetTest("testAppend"));
    suite.addTest(new MemoryResultSetTest("testgetInt"));
    //suite.addTest(new MemoryResultSetTest("testProject"));

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
   * Test {@link MemoryResultSet#getInt}.
   *
   * @throws SQLException if a result set operation fails
   */
  public void testgetInt() throws SQLException {

    // Create result set to test
    String[] columnNames = new String[] {
        "W", "X", "Y"};
    MemoryResultSet rs = new MemoryResultSet(columnNames);

    ResultSetRow row = new ResultSetRow(rs);
    rs.addRow(row);
    row.setInt(1, 1);
    row.setInt(2, 2);
    row.setInt(3, 3);

    row = new ResultSetRow(rs);
    rs.addRow(row);
    row.setInt(1, 2);
    row.setInt(2, 3);
    row.setInt(3, 4);

    // Test for correct return values
    assertTrue(rs.next());

    assertEquals(rs.getInt(1), 1);
    assertEquals(rs.getInt(2), 2);
    assertEquals(rs.getInt(3), 3);

    assertEquals(rs.getInt("W"), 1);
    assertEquals(rs.getInt("X"), 2);
    assertEquals(rs.getInt("Y"), 3);

    assertTrue(rs.next());

    assertEquals(rs.getInt(1), 2);
    assertEquals(rs.getInt(2), 3);
    assertEquals(rs.getInt(3), 4);

    assertEquals(rs.getInt("W"), 2);
    assertEquals(rs.getInt("X"), 3);
    assertEquals(rs.getInt("Y"), 4);

    assertTrue(!rs.next());
  }

  /**
   * Test {@link MemoryResultSet#append}.
   *
   * @throws SQLException if the operation fails
   */
  /*
  public void testAppend() throws SQLException {

    // Create result set to test
    MemoryResultSet rs = new MemoryResultSet(new String[] {
        "X", "Y"});
    ResultSetRow row = new ResultSetRow(rs);
    row.setInt(1, 1);
    row.setInt(2, 2);
    rs.addRow(row);

    row = new ResultSetRow(rs);
    row.setInt(1, 2);
    row.setInt(2, 3);
    rs.addRow(row);

    // Compose the expected result set
    MemoryResultSet expected = new MemoryResultSet(new String[] {
        "X", "Y"});
    row = new ResultSetRow(expected);
    row.setInt(1, 1);
    row.setInt(2, 2);
    expected.addRow(row);

    row = new ResultSetRow(expected);
    row.setInt(1, 2);
    row.setInt(2, 3);
    expected.addRow(row);

    row = new ResultSetRow(expected);
    row.setInt(1, 1);
    row.setInt(2, 2);
    expected.addRow(row);

    row = new ResultSetRow(expected);
    row.setInt(1, 2);
    row.setInt(2, 3);
    expected.addRow(row);

    // Call the method and verify the result
    rs.append(rs);
    assertEquals(expected, rs);
  }
  */

  /**
   * Test {@link MemoryResultSet#project}.
   *
   * @throws SQLException if the operation fails
   */
  /*
  public void testProject() throws SQLException {

    // Create result set to test
    MemoryResultSet rs = new MemoryResultSet(new String[] {
        "X", "Y", "Z"});

    ResultSetRow row = new ResultSetRow(rs);
    row.setInt(1, 1);
    row.setInt(2, 2);
    row.setInt(3, 3);
    rs.addRow(row);

    row = new ResultSetRow(rs);
    row.setInt(1, 2);
    row.setInt(2, 3);
    row.setInt(3, 4);
    rs.addRow(row);

    // Compose the expected result set
    MemoryResultSet expected = new MemoryResultSet(new String[] {
        "X", "Z"});
    row = new ResultSetRow(expected);
    row.setInt(1, 1);
    row.setInt(2, 3);
    expected.addRow(row);

    row = new ResultSetRow(expected);
    row.setInt(1, 2);
    row.setInt(2, 4);
    expected.addRow(row);

    // Call the method and verify the result
    assertTrue(expected.equalsIgnoreOrder(rs.project(new String[] {
        "X", "Z"})));
  }
  */

  /**
   * Test #1 for {@link MemoryResultSet#removeDuplicateRows}.
   *
   * @throws SQLException if the operation fails
   */
  /*
  public void test1RemoveDuplicateRows() throws SQLException {

    // Create result set to test
    MemoryResultSet rs = new MemoryResultSet(new String[] {
        "X", "Y"});
    ResultSetRow row = new ResultSetRow(rs);
    row.setInt(1, 1);
    row.setInt(2, 2);
    rs.addRow(row);

    row = new ResultSetRow(rs);
    row.setInt(1, 2);
    row.setInt(2, 3);
    rs.addRow(row);

    row = new ResultSetRow(rs);
    row.setInt(1, 1);
    row.setInt(2, 2);
    rs.addRow(row);

    // Compose the expected result set
    MemoryResultSet expected = new MemoryResultSet(new String[] {
        "X", "Y"});

    row = new ResultSetRow(expected);
    row.setInt(1, 1);
    row.setInt(2, 2);
    expected.addRow(row);

    row = new ResultSetRow(expected);
    row.setInt(1, 2);
    row.setInt(2, 3);
    expected.addRow(row);

    // Call the method and verify the result
    rs.removeDuplicateRows();
    assertTrue(expected.equalsIgnoreOrder(rs));
  }
  */

  /**
   * Test #2 for {@link MemoryResultSet#removeDuplicateRows}. This tests
   * duplicates that are {@link Object}s equal by value.
   *
   * @throws SQLException if the operation fails
   */
  /*
  public void test2RemoveDuplicateRows() throws SQLException {

    // Create result set to test
    MemoryResultSet rs = new MemoryResultSet(new String[] {
        "X", "Y"});
    ResultSetRow row = new ResultSetRow(rs);
    row.setObject(1, new Date(1));
    row.setObject(2, new Date(2));
    rs.addRow(row);

    row = new ResultSetRow(rs);
    row.setObject(1, new Date(2));
    row.setObject(2, new Date(3));
    rs.addRow(row);

    row = new ResultSetRow(rs);
    row.setObject(1, new Date(1));
    row.setObject(2, new Date(2));
    rs.addRow(row);

    // Compose the expected result set
    MemoryResultSet expected = new MemoryResultSet(new String[] {
        "X", "Y"});

    row = new ResultSetRow(rs);
    row.setObject(1, new Date(1));
    row.setObject(2, new Date(2));
    expected.addRow(row);

    row = new ResultSetRow(rs);
    row.setObject(1, new Date(2));
    row.setObject(2, new Date(3));
    expected.addRow(row);

    // Call the method and verify the result
    rs.removeDuplicateRows();

    if (!expected.equalsIgnoreOrder(rs)) {

      System.out.println(expected + " expected, got " + rs);
    }

    assertTrue(expected.equalsIgnoreOrder(rs));
  }
  */

  /**
   * Test #1 for {@link MemoryResultSet#join}. Join <pre>
   * W=1 X=2 Y=3
   * W=2 X=3 Y=4
   * W=4 X=2 Y=3
   * </pre> with <pre>
   * X=1 Y=2 Z=3
   * X=2 Y=3 Z=4
   * </pre> Expected result <pre>
   * W=1 X=2 Y=3 Z=4
   * W=4 X=2 Y=3 Z=4
   * </pre>
   *
   * @throws SQLException if the join method call fails
   */
  /*
  public void test1Join() throws SQLException {

    // Create first result set
    MemoryResultSet rs1 = new MemoryResultSet(new String[] {
        "W", "X", "Y"});
    ResultSetRow row = new ResultSetRow(rs1);
    row.setInt(1, 1);
    row.setInt(2, 2);
    row.setInt(3, 3);
    rs1.addRow(row);

    row = new ResultSetRow(rs1);
    row.setInt(1, 2);
    row.setInt(2, 3);
    row.setInt(3, 4);
    rs1.addRow(row);

    row = new ResultSetRow(rs1);
    row.setInt(1, 4);
    row.setInt(2, 2);
    row.setInt(3, 3);
    rs1.addRow(row);

    // Create second result set
    MemoryResultSet rs2 = new MemoryResultSet(new String[] {
        "X", "Y", "Z"});
    row = new ResultSetRow(rs2);
    row.setInt(1, 1);
    row.setInt(2, 2);
    row.setInt(3, 3);
    rs2.addRow(row);

    row = new ResultSetRow(rs2);
    row.setInt(1, 2);
    row.setInt(2, 3);
    row.setInt(3, 4);
    rs2.addRow(row);

    // Create expected result of joining the two
    String[] expectedColumnNames = new String[] {
        "W", "X", "Y", "Z"};
    MemoryResultSet expected = new MemoryResultSet(expectedColumnNames);
    row = new ResultSetRow(expected);
    row.setInt(1, 1);
    row.setInt(2, 2);
    row.setInt(3, 3);
    row.setInt(4, 4);
    expected.addRow(row);

    row = new ResultSetRow(expected);
    row.setInt(1, 4);
    row.setInt(2, 2);
    row.setInt(3, 3);
    row.setInt(4, 4);
    expected.addRow(row);

    // Perform the test
    assertTrue(rs1.join(rs2).project(expectedColumnNames).equalsIgnoreOrder(
        expected));
    assertTrue(rs2.join(rs1).project(expectedColumnNames).equalsIgnoreOrder(
        expected));
  }
  */

  /**
   * Test #2 for {@link MemoryResultSet#join}. This tests joins with nulls. Join
   * <pre>
   * X=1 Y=null
   * </pre> with <pre>
   * Y=null Z=2
   * Y=3    Z=null
   * </pre> Expected result <pre>
   * X=1 Y=null Z=2
   * X=1 Y=3    Z=null
   * </pre>
   *
   * @throws SQLException if the join method call fails
   */
  /*
  public void test2Join() throws SQLException {

    // Create first result set
    MemoryResultSet rs1 = new MemoryResultSet(new String[] {
        "X", "Y"});
    ResultSetRow row = new ResultSetRow(rs1);
    row.setInt(1, 1);
    rs1.addRow(row);

    // Create second result set
    MemoryResultSet rs2 = new MemoryResultSet(new String[] {
        "Y", "Z"});

    row = new ResultSetRow(rs2);
    row.setInt(2, 2);
    rs2.addRow(row);

    row = new ResultSetRow(rs2);
    row.setInt(1, 3);
    rs2.addRow(row);

    // Create expected result of joining the two
    String[] expectedColumnNames = new String[] {
        "X", "Y", "Z"};
    MemoryResultSet expected = new MemoryResultSet(expectedColumnNames);

    row = new ResultSetRow(expected);
    row.setInt(1, 1);
    row.setInt(3, 2);
    expected.addRow(row);

    row = new ResultSetRow(expected);
    row.setInt(1, 1);
    row.setInt(2, 3);
    expected.addRow(row);

    / *
         System.err.println("RS1: "+rs1);
         System.err.println("RS2: "+rs2);
         System.err.println("EXPECTED: "+expected);
         MemoryResultSet actual = rs1.join(rs2).project(expectedColumnNames);
         System.err.println("ACTUAL: "+actual);
         actual.removeDuplicateRows();
         System.err.println("UNIQUE: "+actual);
     * /
    // Perform the test
    assertTrue(rs1.join(rs2).project(expectedColumnNames).equalsIgnoreOrder(
        expected));
    assertTrue(rs2.join(rs1).project(expectedColumnNames).equalsIgnoreOrder(
        expected));
  }
  */

  /**
   * Populate the test object.
   *
   */
  protected void setUp() {

    // not yet implemented
  }
}
