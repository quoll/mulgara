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

package org.mulgara.server.rmi;


// JUnit
import junit.framework.*;

// Java 2 standard packages
import java.util.*;
import java.sql.*;

// Log4J
import org.apache.log4j.Logger;

// Locally written packages
import org.mulgara.query.*;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.util.ResultSetRow;
import org.mulgara.util.MemoryResultSet;

/**
 * Purpose: Test case for {@link AnswerPage}.
 *
 * @created 2004-03-29
 *
 * @author <a href="http://staff.pisoftware.com/pag">Paul Gearon</a>
 *
 * @version $Revision: 1.9 $
 *
 * @modified $Date: 2005/01/05 04:59:02 $ by $Author: newmana $
 *
 * @maintenanceAuthor $Author: newmana $
 *
 * @copyright &copy;2001-2004
 *   <a href="http://www.pisoftware.com/">Plugged In Software Pty Ltd</a>
 *
 * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
 */
public class AnswerPageImplUnitTest extends TestCase {

  /**
   * Logger.
   */
  private static final Logger logger = Logger.getLogger(AnswerPageImplUnitTest.class);

  /**
   * Constructs a new answer test with the given name.
   *
   * @param name the name of the test
   */
  public AnswerPageImplUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   *
   * @return The test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTest(new AnswerPageImplUnitTest("testConstructor"));
    suite.addTest(new AnswerPageImplUnitTest("testSize"));
    suite.addTest(new AnswerPageImplUnitTest("testSizeConstructor"));
    suite.addTest(new AnswerPageImplUnitTest("testNext"));
    suite.addTest(new AnswerPageImplUnitTest("testIndexedObjects"));
    suite.addTest(new AnswerPageImplUnitTest("testNamedObjects"));

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
   * Test the constructor.
   *
   * @throws Exception Shouldn't throw
   */
  public void testConstructor() throws Exception {

    AnswerPage page = new AnswerPageImpl(buildAnswer(4));
  }


  /**
   * Test size method.
   */
  public void testSize() throws Exception
  {
    AnswerPage page = new AnswerPageImpl(buildAnswer(4));

    assertEquals(4, page.getPageSize());
  }


  /**
   * Test the size constructor.
   *
   * @throws Exception Shouldn't throw
   */
  public void testSizeConstructor() throws Exception {

    AnswerPage page = new AnswerPageImpl(buildAnswer(4), 3);

    assertEquals(3, page.getPageSize());

    page = new AnswerPageImpl(buildAnswer(3), 4);

    assertEquals(3, page.getPageSize());
  }


  /**
   * Test next method.
   */
  public void testNext() throws Exception
  {
    AnswerPage page = new AnswerPageImpl(buildAnswer(4));

    page.beforeFirstInPage();

    int r = 0;
    while (page.nextInPage()) r++;

    assertEquals(4, r);
  }


  /**
   * Test indexed columns.
   */
  public void testIndexedObjects() throws Exception
  {
    AnswerPage page = new AnswerPageImpl(buildAnswer(4));

    page.beforeFirstInPage();

    int r = 0;
    while (page.nextInPage()) {
      assertEquals(new LiteralImpl("X"+r), page.getObjectFromPage(0));
      assertEquals(new LiteralImpl("Y"+r), page.getObjectFromPage(1));
      r++;
    }

    assertEquals(4, r);
  }


  /**
   * Test indexed columns.
   */
  public void testNamedObjects() throws Exception
  {
    AnswerPage page = new AnswerPageImpl(buildAnswer(4));

    page.beforeFirstInPage();

    int r = 0;
    while (page.nextInPage()) {
      assertEquals(new LiteralImpl("X"+r), page.getObjectFromPage("x"));
      assertEquals(new LiteralImpl("Y"+r), page.getObjectFromPage("y"));
      r++;
    }

    assertEquals(4, r);
  }


  /**
   * Populate a 2 column test answer.
   * @param rows The number of rows in the answer.
   */
  private Answer buildAnswer(int rows) throws TuplesException, SQLException {
   
    LiteralImpl[] fields = new LiteralImpl[rows*2];
    for (int r = 0; r < rows; r++) {
      fields[r*2+0] = new LiteralImpl("X"+r);
      fields[r*2+1] = new LiteralImpl("Y"+r);
    }
    // since this is an ArrayAnswer it is SAFE to not call close()!!!
    Answer answer = new ArrayAnswer(
                      new Variable[] { new Variable("x"), new Variable("y") },
                      fields
                    );
    answer.beforeFirst();
    return answer;
  }


  /**
   * Populate the test answer.
   *
   * @throws QueryException EXCEPTION TO DO
   * @throws SQLException EXCEPTION TO DO
   */
  protected void setUp() {
  }

  /**
   * Clean up the test answer.
   */
  public void tearDown() {

  }

}
