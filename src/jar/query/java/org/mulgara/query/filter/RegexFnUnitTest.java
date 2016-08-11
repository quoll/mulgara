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

import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.TestContext;
import org.mulgara.query.filter.TestContextOwner;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;

import org.mulgara.query.filter.value.Bool;
import org.mulgara.query.filter.value.SimpleLiteral;
import org.mulgara.query.filter.value.TypedLiteral;
import org.mulgara.query.filter.value.ValueLiteral;
import org.mulgara.query.filter.value.Var;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the Regex function.
 *
 * @created Apr 17, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class RegexFnUnitTest extends TestCase {

  Bool t = Bool.TRUE;
  Bool f = Bool.FALSE;

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public RegexFnUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new RegexFnUnitTest("testLiteral"));
    suite.addTest(new RegexFnUnitTest("testVar"));
    return suite;
  }

  public void testLiteral() throws Exception {
    SimpleLiteral str = new SimpleLiteral("a foolish test");
    SimpleLiteral diffStr = new SimpleLiteral("another test");
    SimpleLiteral pattern = new SimpleLiteral("foo");
    SimpleLiteral patternCaps = new SimpleLiteral("FOO");
    SimpleLiteral pattern2 = new SimpleLiteral("foo.*test");
    SimpleLiteral noTest = new SimpleLiteral("fred");
    SimpleLiteral caseFlag = new SimpleLiteral("i");
    ValueLiteral typed = TypedLiteral.newLiteral("a foolish test");
    ValueLiteral typedPattern = TypedLiteral.newLiteral("foo");

    RegexFn fn = new RegexFn(str, pattern);
    assertTrue(t.equals(fn));
    assertTrue(fn.equals(t));
    fn = new RegexFn(str, pattern2);
    assertTrue(t.equals(fn));
    assertTrue(fn.equals(t));

    fn = new RegexFn(diffStr, pattern);
    assertTrue(f.equals(fn));
    assertTrue(fn.equals(f));
    fn = new RegexFn(diffStr, pattern2);
    assertTrue(f.equals(fn));
    assertTrue(fn.equals(f));

    fn = new RegexFn(str, noTest);
    assertTrue(f.equals(fn));

    fn = new RegexFn(str, patternCaps);
    assertTrue(f.equals(fn));
    fn = new RegexFn(str, patternCaps, caseFlag);
    assertTrue(t.equals(fn));

    fn = new RegexFn(typed, pattern);
    try {
      assertTrue(f.equals(fn));
      fail("Tested regex on a typed literal");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error:"));
    }

    fn = new RegexFn(str, typedPattern);
    try {
      assertTrue(f.equals(fn));
      fail("Tested regex on a typed literal");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error:"));
    }
}

  public void testVar() throws Exception {
    RegexFn fn = new RegexFn(new Var("x"), new Var("y"), new Var("z"));

    Literal noFlags = new LiteralImpl("");
    Literal caseFlag = new LiteralImpl("i");

    Literal str = new LiteralImpl("a foolish test");
    Literal diffStr = new LiteralImpl("another test");
    Literal pattern = new LiteralImpl("foo");
    Literal patternCaps = new LiteralImpl("FOO");
    Literal pattern2 = new LiteralImpl("foo.*test");
    Literal noTest = new LiteralImpl("fred");
    Literal typed = new LiteralImpl("a foolish test", SimpleLiteral.STRING_TYPE.getValue());
    Literal typedPattern = new LiteralImpl("foo", SimpleLiteral.STRING_TYPE.getValue());
    URIReferenceImpl xsdString = new URIReferenceImpl(SimpleLiteral.STRING_TYPE.getValue());
    BlankNodeImpl bn = new BlankNodeImpl(101);
    Node[][] rows = {
      new Node[] {str, pattern, noFlags},
      new Node[] {diffStr, pattern, noFlags},
      new Node[] {str, pattern2, noFlags},
      new Node[] {diffStr, pattern, noFlags},
      new Node[] {diffStr, pattern2, noFlags},

      new Node[] {str, noTest, noFlags},

      new Node[] {str, patternCaps, noFlags},
      new Node[] {str, patternCaps, caseFlag},

      new Node[] {typed, pattern, noFlags},
      new Node[] {str, typedPattern, caseFlag},

      new Node[] {xsdString, pattern, noFlags},
      new Node[] {str, xsdString, noFlags},
      new Node[] {str, pattern, xsdString},

      new Node[] {null, pattern, noFlags},
      new Node[] {str, null, noFlags},
      new Node[] {str, pattern, null},

      new Node[] {bn, pattern, noFlags},
      new Node[] {str, bn, noFlags},
      new Node[] {str, pattern, bn},
    };
    TestContext c = new TestContext(new String[] {"x", "y", "z"}, rows);
    c.beforeFirst();
    fn.setContextOwner(new TestContextOwner(c));
    // check the context setting
    fn.setCurrentContext(c);

    String results = "tftff f ft ee eee xxx eee";
    runTests(c, fn, results);
  }

  private void runTests(TestContext c, AbstractFilterValue fn, String results) throws Exception {
    c.beforeFirst();
    for (char result: results.toCharArray()) {
      if (result == ' ') continue;
      assertTrue(c.next());
      switch (result) {
      case 't':  // equal
        assertTrue(t.equals(fn));
        break;

      case 'f':  // unequal
        assertTrue(f.equals(fn));
        break;

      case 'e':  // typing error
        try {
          assertTrue(f.equals(fn));
          fail("Successfully tested values that were not simple literals");
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Type Error"));
        }
        break;

      case 'x':  // exception due to unbound
        try {
          assertTrue(f.equals(fn));
          fail("No exception when testing an unbound value");
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Unbound column"));
        }
        break;
        
      default:
        fail("Bad test data");
      }
    }
    assertFalse(c.next());
  }

}
