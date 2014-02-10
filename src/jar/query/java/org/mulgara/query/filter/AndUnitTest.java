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

import java.net.URI;

import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.TestContext;
import org.mulgara.query.filter.TestContextOwner;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;

import org.mulgara.query.filter.value.Bool;
import org.mulgara.query.filter.value.NumericLiteral;
import org.mulgara.query.filter.value.TypedLiteral;
import org.mulgara.query.filter.value.Var;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Tests the AND operation.
 *
 * @created Apr 14, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class AndUnitTest extends AbstractLogicUnitTest {

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public AndUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new AndUnitTest("testLiteral"));
    suite.addTest(new AndUnitTest("testVar"));
    return suite;
  }

  public void testLiteral() throws Exception {
    Bool t = Bool.TRUE;
    Bool f = Bool.FALSE;
    basicTest(t, t, t);
    basicTest(f, t, f);
    basicTest(t, f, f);
    basicTest(f, f, f);

    basicTest(new NumericLiteral(7), t, t);
    basicTest(new NumericLiteral(0), t, f);
    basicTest(new NumericLiteral(7), f, f);
    basicTest(new NumericLiteral(0), f, f);

    basicTest(TypedLiteral.newLiteral("foo"), t, t);
    basicTest(TypedLiteral.newLiteral(""), t, f);
    basicTest(TypedLiteral.newLiteral("foo"), f, f);
    basicTest(TypedLiteral.newLiteral(""), f, f);
  }

  void basicTest(Filter lhs, Filter rhs, Bool result) throws Exception {
    basicTest(new And(lhs, rhs), result);
  }

  public void testVar() throws Exception {
    Var x = new Var("x");
    Var y = new Var("y");
    And fn = new And(x, y);

    URI fooBar = URI.create("foo:bar");
    Literal seven = new LiteralImpl("7", xsdInt);
    Literal zero = new LiteralImpl("0", xsdInt);
    Literal trueLiteral = new LiteralImpl("true", xsdBool);
    Literal falseLiteral = new LiteralImpl("false", xsdBool);
    Node[][] rows = {
      new Node[] {seven, seven},
      new Node[] {seven, zero},
      new Node[] {seven, trueLiteral},
      new Node[] {seven, falseLiteral},

      new Node[] {falseLiteral, new LiteralImpl("foo", "en")},
      new Node[] {trueLiteral, new LiteralImpl("foo", fooBar)},
      new Node[] {new LiteralImpl(""), trueLiteral},
      new Node[] {new LiteralImpl(""), falseLiteral},
      new Node[] {trueLiteral, new URIReferenceImpl(fooBar)},
      new Node[] {falseLiteral, new URIReferenceImpl(fooBar)},
      new Node[] {trueLiteral, new BlankNodeImpl(1001)},
      new Node[] {falseLiteral, new BlankNodeImpl(1002)},
      new Node[] {trueLiteral, null},
      new Node[] {falseLiteral, null}
    };
    TestContext c = new TestContext(new String[] {"x", "y"}, rows);
    c.beforeFirst();
    fn.setContextOwner(new TestContextOwner(c));

    // check the context setting
    fn.setCurrentContext(c);

    Bool t = Bool.TRUE;
    Bool f = Bool.FALSE;

    assertTrue(c.next());
    assertTrue(t.equals(fn));

    assertTrue(c.next());
    assertTrue(f.equals(fn));

    assertTrue(c.next());
    assertTrue(t.equals(fn));

    assertTrue(c.next());
    assertTrue(f.equals(fn));

    assertTrue(c.next());
    assertTrue(f.equals(fn));

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("EBV on an unknown type of literal");
    } catch (QueryException qe) { }

    assertTrue(c.next());
    assertTrue(f.equals(fn));

    assertTrue(c.next());
    assertTrue(f.equals(fn));

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Logic operation on a URI");
    } catch (QueryException qe) { }

    assertTrue(c.next());
    assertTrue(f.equals(fn));

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Logic operation on a blank node");
    } catch (QueryException qe) { }

    assertTrue(c.next());
    assertTrue(f.equals(fn));

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Logic operation on an unbound");
    } catch (QueryException qe) { }

    assertTrue(c.next());
    assertTrue(f.equals(fn));

    assertFalse(c.next());
  }
  
}
