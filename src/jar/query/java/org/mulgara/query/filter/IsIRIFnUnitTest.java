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
package org.mulgara.query.filter;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Tests the isIRI function.
 *
 * @created Apr 15, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class IsIRIFnUnitTest extends AbstractIsIriFnUnitTest {

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public IsIRIFnUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new NotUnitTest("testLiteral"));
    suite.addTest(new IsIRIFnUnitTest("testVar"));
    return suite;
  }

  public AbstractFilterValue createFn(RDFTerm arg) {
    return new IsIriFn(arg);
  }

}
