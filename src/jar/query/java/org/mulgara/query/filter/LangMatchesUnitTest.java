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
 * Tests the langMatches function.
 *
 * @created Apr 17, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class LangMatchesUnitTest extends TestCase {

  Bool t = Bool.TRUE;
  Bool f = Bool.FALSE;

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public LangMatchesUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new LangMatchesUnitTest("testLiteral"));
    suite.addTest(new LangMatchesUnitTest("testVar"));
    return suite;
  }

  public void testLiteral() throws Exception {
    SimpleLiteral litEn = new SimpleLiteral("foo", "en");
    SimpleLiteral langEn = new SimpleLiteral("en");
    SimpleLiteral langEnCaps = new SimpleLiteral("EN");
    LangMatches fn = new LangMatches(litEn, langEn);
    assertTrue(t.equals(fn));
    assertTrue(fn.equals(t));
    fn = new LangMatches(litEn, langEnCaps);
    assertTrue(t.equals(fn));
    assertTrue(fn.equals(t));

    SimpleLiteral langFr = new SimpleLiteral("fr");
    fn = new LangMatches(litEn, langFr);
    assertTrue(f.equals(fn));
    assertTrue(fn.equals(f));

    SimpleLiteral litEnGB = new SimpleLiteral("en-GB");
    fn = new LangMatches(litEnGB, langEn);
    assertTrue(f.equals(fn));
    assertTrue(fn.equals(f));
    
    SimpleLiteral litEmpty = new SimpleLiteral("foo");
    fn = new LangMatches(litEmpty, langEn);
    assertTrue(f.equals(fn));
    SimpleLiteral langAll = new SimpleLiteral("*");
    fn = new LangMatches(litEmpty, langAll);
    assertTrue(f.equals(fn));
    fn = new LangMatches(litEn, langAll);
    assertTrue(t.equals(fn));

    ValueLiteral lit = TypedLiteral.newLiteral("en");
    fn = new LangMatches(lit, langEn);
    try {
      assertTrue(f.equals(fn));
      fail("Tested the language on a typed literal");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error:"));
    }
    fn = new LangMatches(lit, langAll);
    try {
      assertTrue(f.equals(fn));
      fail("Tested the language on a typed literal");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error:"));
    }
    fn = new LangMatches(litEn, lit);
    try {
      assertTrue(f.equals(fn));
      fail("Tested the language on a typed literal");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error:"));
    }
}

  public void testVar() throws Exception {
    LangMatches fn = new LangMatches(new Var("x"), new Var("y"));

    Literal fooEn = new LiteralImpl("foo", "en");
    Literal fooFr = new LiteralImpl("foo", "fr");
    Literal fooEnGB = new LiteralImpl("foo", "en-GB");
    Literal fooSimple = new LiteralImpl("foo");
    Literal fooTyped = new LiteralImpl("en", SimpleLiteral.STRING_TYPE.getValue());
    Literal en = new LiteralImpl("en");
    Literal all = new LiteralImpl("*");
    URIReferenceImpl xsdString = new URIReferenceImpl(SimpleLiteral.STRING_TYPE.getValue());
    BlankNodeImpl bn = new BlankNodeImpl(101);
    Node[][] rows = {
      new Node[] {fooEn, en},
      new Node[] {fooFr, en},
      new Node[] {fooEnGB, en},
      new Node[] {fooEn, all},

      new Node[] {fooSimple, en},
      new Node[] {fooSimple, all},

      new Node[] {fooTyped, en},
      new Node[] {fooEn, fooTyped},
      new Node[] {fooSimple, fooTyped},

      new Node[] {xsdString, en},
      new Node[] {xsdString, all},

      new Node[] {null, all},
      new Node[] {null, en},

      new Node[] {bn, en},
      new Node[] {bn, all},
    };
    TestContext c = new TestContext(new String[] {"x", "y"}, rows);
    c.beforeFirst();
    fn.setContextOwner(new TestContextOwner(c));
    // check the context setting
    fn.setCurrentContext(c);

    String results = "tftt ff eee ee xx ee";
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
