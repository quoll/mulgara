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

import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.TestContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the Bool literal class.
 *
 * @created Mar 31, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class BoolUnitTest extends TestCase {

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public BoolUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new BoolUnitTest("testValues"));
    suite.addTest(new BoolUnitTest("testFilter"));
    suite.addTest(new BoolUnitTest("testType"));
    suite.addTest(new BoolUnitTest("testProperties"));
    return suite;
  }


  public void testValues() throws Exception {
    Bool b = new Bool(true);
    assertTrue((Boolean)b.getValue());
    b = new Bool(false);
    assertFalse((Boolean)b.getValue());
  }

  public void testFilter() throws Exception {
    Context c = new TestContext();
    Bool b = new Bool(true);
    assertTrue(b.test(c));
    b = new Bool(false);
    assertFalse(b.test(c));
  }

  public void testType() throws Exception {
    Bool b = new Bool(true);
    assertTrue(b.getType().isIRI());
    assertEquals(b.getType().getValue(), Bool.TYPE);
  }

  public void testProperties() throws Exception {
    Bool b = new Bool(true);
    assertFalse(b.isBlank());
    assertFalse(b.isIRI());
    assertTrue(b.isLiteral());
    assertFalse(b.isURI());
  }
}
