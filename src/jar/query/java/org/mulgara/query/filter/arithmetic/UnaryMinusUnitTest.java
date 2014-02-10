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
package org.mulgara.query.filter.arithmetic;

import java.net.URI;

import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.TestContext;
import org.mulgara.query.filter.TestContextOwner;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;

import org.mulgara.query.filter.value.NumericExpression;
import org.mulgara.query.filter.value.NumericLiteral;
import org.mulgara.query.filter.value.SimpleLiteral;
import org.mulgara.query.filter.value.Var;
import static org.mulgara.query.rdf.XSD.NAMESPACE;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the unary minus operation.
 *
 * @created Apr 14, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class UnaryMinusUnitTest extends TestCase {

  URI xsdInt = URI.create(NAMESPACE + "int");
  URI xsdLong = URI.create(NAMESPACE + "long");
  URI xsdFloat = URI.create(NAMESPACE + "float");
  URI xsdDouble = URI.create(NAMESPACE + "double");

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public UnaryMinusUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new UnaryMinusUnitTest("testLiteral"));
    suite.addTest(new UnaryMinusUnitTest("testVar"));
    return suite;
  }

  public void testLiteral() throws Exception {
    Number op = 7.3;
    basicTest(new NumericLiteral(op.intValue()));
    basicTest(new NumericLiteral(op.longValue()));
    basicTest(new NumericLiteral(op.floatValue()));
    basicTest(new NumericLiteral(op.doubleValue()));
    op = -42;
    basicTest(new NumericLiteral(op.intValue()));
    basicTest(new NumericLiteral(op.longValue()));
    basicTest(new NumericLiteral(op.floatValue()));
    basicTest(new NumericLiteral(op.doubleValue()));
  }

  private void basicTest(NumericExpression value) throws Exception {
    UnaryMinus op = new UnaryMinus(value);
    NumericExpression expectedResult = new NumericLiteral(negate(value.getNumber()));
    assertTrue(op.equals(expectedResult));
    assertFalse(op.isBlank());
    assertFalse(op.isIRI());
    assertTrue(op.isLiteral());
    assertFalse(op.isURI());
    assertTrue(expectedResult.getType().equals(op.getType()));
    assertEquals(SimpleLiteral.EMPTY, op.getLang());
  }

  private Number negate(Number n) {
    if (n instanceof Integer) return -n.intValue();
    if (n instanceof Long) return -n.longValue();
    if (n instanceof Float) return -n.floatValue();
    if (n instanceof Double) return -n.doubleValue();
    throw new IllegalArgumentException("Unexpected numeric type: " + n.getClass().getSimpleName());
  }

  public void testVar() throws Exception {
    Var x = new Var("x");
    UnaryMinus fn = new UnaryMinus(x);
    
    URI fooBar = URI.create("foo:bar");
    Number op = 7.3;
    Literal iop = new LiteralImpl("" + op.intValue(), xsdInt);
    Literal lop = new LiteralImpl("" + op.longValue(), xsdLong);
    Literal fop = new LiteralImpl("" + op.floatValue(), xsdFloat);
    Literal dop = new LiteralImpl("" + op.doubleValue(), xsdDouble);
    Node[][] rows = {
      new Node[] {iop},
      new Node[] {lop},
      new Node[] {fop},
      new Node[] {dop},
      // The following are to fail
      new Node[] {new LiteralImpl("foo", "en")},
      new Node[] {new LiteralImpl("foo", fooBar)},
      new Node[] {new URIReferenceImpl(fooBar)},
      new Node[] {new BlankNodeImpl()},
      new Node[] {null}
    };
    TestContext c = new TestContext(new String[] {"x"}, rows);
    c.beforeFirst();
    fn.setContextOwner(new TestContextOwner(c));

    // check the context setting
    fn.setCurrentContext(c);

    assertTrue(c.next());
    assertTrue(new NumericLiteral(-op.intValue()).equals(fn));

    assertTrue(c.next());
    assertTrue(new NumericLiteral(-op.longValue()).equals(fn));

    assertTrue(c.next());
    assertTrue(new NumericLiteral(-op.floatValue()).equals(fn));

    assertTrue(c.next());
    assertTrue(new NumericLiteral(-op.doubleValue()).equals(fn));

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Negated a language string");
    } catch (QueryException qe) { }

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Negated ab unknown typed literal");
    } catch (QueryException qe) { }

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Negated a uri");
    } catch (QueryException qe) { }

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Negated a blank node");
    } catch (QueryException qe) { }

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Negated an unbound");
    } catch (QueryException qe) { }

    assertFalse(c.next());
  }
  
}
