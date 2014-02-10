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

import java.math.BigDecimal;

import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.TestContext;
import static org.mulgara.query.rdf.XSD.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the NumericLiteral class
 *
 * @created Mar 31, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class NumericLiteralUnitTest extends TestCase {

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public NumericLiteralUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new NumericLiteralUnitTest("testValues"));
    suite.addTest(new NumericLiteralUnitTest("testFilter"));
    suite.addTest(new NumericLiteralUnitTest("testType"));
    suite.addTest(new NumericLiteralUnitTest("testProperties"));
    suite.addTest(new NumericLiteralUnitTest("testFactory"));
    return suite;
  }

  public void testValues() throws Exception {
    Integer five = Integer.valueOf(5);
    NumericLiteral n = new NumericLiteral(five);
    assertEquals(n.getValue(), five);
    assertFalse(Integer.valueOf(4).equals(n.getValue()));

    Double six = Double.valueOf(6.0);
    n = new NumericLiteral(six);
    assertEquals(n.getValue(), six);
    assertFalse(Double.valueOf(4).equals(n.getValue()));

    BigDecimal large = new BigDecimal("12345678901234567890");
    n = new NumericLiteral(large);
    assertEquals(n.getValue(), large);
    assertFalse(BigDecimal.valueOf(1234567890123456789L).equals(n.getValue()));
  }

  public void testFilter() throws Exception {
    Context c = new TestContext();
    NumericLiteral n = new NumericLiteral(Integer.valueOf(5));
    assertTrue(n.test(c));
    n = new NumericLiteral(Integer.valueOf(0));
    assertFalse(n.test(c));

    n = new NumericLiteral(Double.valueOf(5.0));
    assertTrue(n.test(c));
    n = new NumericLiteral(Double.valueOf(0));
    assertFalse(n.test(c));

    n = new NumericLiteral(BigDecimal.valueOf(5.0));
    assertTrue(n.test(c));
    n = new NumericLiteral(BigDecimal.ZERO);
    assertFalse(n.test(c));
  }

  public void testType() throws Exception {
    NumericLiteral n = new NumericLiteral(Integer.valueOf(5));
    assertTrue(n.getType().isIRI());
    assertEquals(n.getType().getValue(), INT_URI);

    n = new NumericLiteral(Long.valueOf(5));
    assertTrue(n.getType().isIRI());
    assertEquals(n.getType().getValue(), LONG_URI);

    n = new NumericLiteral(Double.valueOf(5));
    assertTrue(n.getType().isIRI());
    assertEquals(n.getType().getValue(), DOUBLE_URI);

    n = new NumericLiteral(Float.valueOf(5));
    assertTrue(n.getType().isIRI());
    assertEquals(n.getType().getValue(), FLOAT_URI);

    n = new NumericLiteral(new BigDecimal("12345678901234567890"));
    assertTrue(n.getType().isIRI());
    assertEquals(n.getType().getValue(), DECIMAL_URI);
  }


  public void testProperties() throws Exception {
    NumericLiteral n = new NumericLiteral(Integer.valueOf(5));
    assertFalse(n.isBlank());
    assertFalse(n.isIRI());
    assertTrue(n.isLiteral());
    assertFalse(n.isURI());
  }

  public void testFactory() throws Exception {
    Integer five = Integer.valueOf(5);
    NumericLiteral n = new NumericLiteral(five);
    ValueLiteral n2 = TypedLiteral.newLiteral(five);
    assertTrue(n.equals(n2));
    assertTrue(n.getType().equals(n2.getType()));

    Double six = Double.valueOf(6.0);
    n = new NumericLiteral(six);
    n2 = TypedLiteral.newLiteral(six);
    assertTrue(n.equals(n2));
    assertTrue(n.getType().equals(n2.getType()));

    BigDecimal large = new BigDecimal("12345678901234567890");
    n = new NumericLiteral(large);
    n2 = TypedLiteral.newLiteral(large);
    assertTrue(n.equals(n2));
    assertTrue(n.getType().equals(n2.getType()));
  }

}
