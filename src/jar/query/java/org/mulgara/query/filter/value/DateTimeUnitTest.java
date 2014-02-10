/**
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
package org.mulgara.query.filter.value;

import java.util.Date;

import org.mulgara.query.QueryException;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.TestContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the DataTime literal class
 *
 * @created Mar 31, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class DateTimeUnitTest extends TestCase {

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public DateTimeUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new DateTimeUnitTest("testValues"));
    suite.addTest(new DateTimeUnitTest("testFilter"));
    suite.addTest(new DateTimeUnitTest("testType"));
    suite.addTest(new DateTimeUnitTest("testProperties"));
    return suite;
  }

  public void testValues() throws Exception {
    Date date = new Date();
    DateTime dt = new DateTime(date);
    assertEquals(dt.getValue(), date);
    Date d2 = new Date(date.getTime() + 1);
    assertFalse(d2.equals(dt.getValue()));
  }

  public void testFilter() throws Exception {
    Context c = new TestContext();
    DateTime dt = new DateTime(new Date());
    try {
      dt.test(c);
      fail("DateTime effective boolean value should not return a value");
    } catch (QueryException qe) { }
  }

  public void testType() throws Exception {
    DateTime dt = new DateTime(new Date());
    assertTrue(dt.getType().isIRI());
    assertEquals(dt.getType().getValue(), DateTime.TYPE);
  }


  public void testProperties() throws Exception {
    DateTime dt = new DateTime(new Date());
    assertFalse(dt.isBlank());
    assertFalse(dt.isIRI());
    assertTrue(dt.isLiteral());
    assertFalse(dt.isURI());
  }
}
