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

// Third party packages
import junit.framework.*;        // JUnit
import org.apache.log4j.Logger;  // Log4J

// Locally written packages
import org.mulgara.query.rdf.LiteralImpl;

/**
 * Purpose: Test case for {@link ArrayAnswer}.
 *
 * @created 2001-10-09
 *
 * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
 *
 * @version $Revision: 1.8 $
 *
 * @modified $Date: 2005/01/05 04:58:20 $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
 *
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class ArrayAnswerUnitTest extends TestCase {

  /** Logger. */
  @SuppressWarnings("unused")
  private Logger logger = Logger.getLogger(ArrayAnswerUnitTest.class.getName());

  /**
   * Test instance.
   *
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
  private ArrayAnswer answer;

  /**
   * Constructs a new answer test with the given name.
   *
   * @param name the name of the test
   */
  public ArrayAnswerUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {
    return new TestSuite(ArrayAnswerUnitTest.class);
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
  // Methods overriding TestCase
  //

  /**
   * Populate the test answer.
   */
  protected void setUp() {
    answer = new ArrayAnswer(
        new Variable[] {new Variable("x"), new Variable("y")}
        ,
        new Object[] {new LiteralImpl("X1"), new LiteralImpl("Y1"),
        new LiteralImpl("X2"), new LiteralImpl("Y2")});
  }

  /**
   * Clean up the test answer.
   */
  public void tearDown() throws Exception {
    answer.close();
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
    Answer copied = new ArrayAnswer(answer);
    assertTrue(answer != copied);
    assertEquals(answer, copied);
    copied.close();
  }

  public void testConstructWithUnconstrained() throws Exception {
    ArrayAnswer arrayAnswer = new ArrayAnswer(new UnconstrainedAnswer());

    assertEquals(0, arrayAnswer.getNumberOfVariables());
    assertTrue(arrayAnswer.next());
    assertTrue(!arrayAnswer.next());

    try {
      arrayAnswer.close();
    }
    catch (Exception e) {
      fail("Should be able to close answer");
    }
  }

  /**
   * Test basic accessor methods.
   */
  public void testAccess() throws Exception {
    Variable x = new Variable("x");
    Variable y = new Variable("y");

    assertEquals(x, answer.getVariable(0));
    assertEquals(y, answer.getVariable(1));

    assertEquals(0, answer.getColumnIndex(x));
    assertEquals(1, answer.getColumnIndex(y));

    assertTrue(answer.next());
    assertEquals(new LiteralImpl("X1"), answer.getObject(0));
    assertEquals(new LiteralImpl("Y1"), answer.getObject(1));

    assertTrue(answer.next());
    assertEquals(new LiteralImpl("X2"), answer.getObject(0));
    assertEquals(new LiteralImpl("Y2"), answer.getObject(1));

    assertTrue(!answer.next());
  }
}
