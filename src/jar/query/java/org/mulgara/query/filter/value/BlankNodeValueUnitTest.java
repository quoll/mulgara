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

import org.mulgara.query.rdf.BlankNodeImpl;
import org.jrdf.graph.BlankNode;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the BlankNode value class.
 *
 * @created Mar 31, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class BlankNodeValueUnitTest extends TestCase {

  private BlankNode b = new BlankNodeImpl();
  
  private BlankNode b2 = new BlankNodeImpl();

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public BlankNodeValueUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new BlankNodeValueUnitTest("testValues"));
    suite.addTest(new BlankNodeValueUnitTest("testProperties"));
    return suite;
  }


  public void testValues() throws Exception {
    BlankNodeValue bv = new BlankNodeValue(b);
    assertEquals(b, bv.getValue());
    assertFalse(b2.equals(bv.getValue()));
    BlankNodeValue bv2 = new BlankNodeValue(b2);
    assertTrue(bv.equals(bv));
    assertFalse(bv.equals(bv2));
  }

  public void testProperties() throws Exception {
    BlankNodeValue bv = new BlankNodeValue(b);
    assertTrue(bv.isBlank());
    assertFalse(bv.isIRI());
    assertFalse(bv.isLiteral());
    assertFalse(bv.isURI());
  }
}
