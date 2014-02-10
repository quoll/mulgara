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
import org.mulgara.query.filter.value.TypedLiteral;
import org.mulgara.query.filter.value.ValueLiteral;
import org.mulgara.query.filter.value.Var;
import static org.mulgara.query.rdf.XSD.NAMESPACE;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the numeric operation classes.
 * This uses a convoluted mechanism to let the compiler determine the correct return types
 * in each of the implementing classes.
 *
 * @created Apr 10, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class AbstractOperationUnitTest extends TestCase {

  URI xsdInt = URI.create(NAMESPACE + "int");
  URI xsdLong = URI.create(NAMESPACE + "long");
  URI xsdFloat = URI.create(NAMESPACE + "float");
  URI xsdDouble = URI.create(NAMESPACE + "double");

  /** The first operand to use for this test */
  abstract Number op1();

  /** The second operand to use for this test */
  abstract Number op2();

  /** Get an instance of the operation for this test */
  abstract BinaryOperation getOperation(NumericExpression e1, NumericExpression e2);

  /** perform the operation using native data types which are passed in two literals */
  abstract ValueLiteral doOperation(Literal l1, Literal l2) throws QueryException;

  NumericLiteral doOperation(NumericLiteral l1, NumericLiteral l2) throws QueryException {
    return (NumericLiteral)doOperation(new LiteralImpl(l1.getLexical(), l1.getType().getValue()), new LiteralImpl(l2.getLexical(), l2.getType().getValue()));
  }

  enum NumberType { tInt, tLong, tFloat, tDouble };

  /** Gets the type of a URI as one of the defined number types */
  NumberType getType(Literal l) {
    URI dt = l.getDatatypeURI();
    if (dt.equals(xsdInt)) return NumberType.tInt;
    if (dt.equals(xsdLong)) return NumberType.tLong;
    if (dt.equals(xsdFloat)) return NumberType.tFloat;
    if (dt.equals(xsdDouble)) return NumberType.tDouble;
    throw new Error("Unknown number type: " + dt);
  }

  /** request that the implementing class do the operation and creates a literal */
  ValueLiteral getLiteralResult(Node n1, Node n2) throws QueryException {
    Literal l1 = (Literal)n1;
    Literal l2 = (Literal)n2;
    return doOperation(l1, l2);
  }

  // use compiler dispatch and autoboxing to convert a value to a typed literal
  ValueLiteral newLiteral(int x) throws QueryException { return TypedLiteral.newLiteral(x); }
  ValueLiteral newLiteral(long x) throws QueryException { return TypedLiteral.newLiteral(x); }
  ValueLiteral newLiteral(float x) throws QueryException { return TypedLiteral.newLiteral(x); }
  ValueLiteral newLiteral(double x) throws QueryException { return TypedLiteral.newLiteral(x); }
  
  // convert a literal to a basic type, explicitly
  int getInt(Literal l) { return Integer.parseInt(l.getLexicalForm()); }
  long getLong(Literal l) { return Long.parseLong(l.getLexicalForm()); }
  float getFloat(Literal l) { return Float.parseFloat(l.getLexicalForm()); }
  double getDouble(Literal l) { return Double.parseDouble(l.getLexicalForm()); }


  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public AbstractOperationUnitTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite();
  }

  public void testLiteral() throws Exception {
    NumericLiteral op1i = new NumericLiteral(op1().intValue());
    NumericLiteral op2i = new NumericLiteral(op2().intValue());
    NumericLiteral op1l = new NumericLiteral(op1().longValue());
    NumericLiteral op2l = new NumericLiteral(op2().longValue());
    NumericLiteral op1f = new NumericLiteral(op1().floatValue());
    NumericLiteral op2f = new NumericLiteral(op2().floatValue());
    NumericLiteral op1d = new NumericLiteral(op1().doubleValue());
    NumericLiteral op2d = new NumericLiteral(op2().doubleValue());

    basicTest(op1i, op2i, doOperation(op1i, op2i));
    basicTest(op1i, op2l, doOperation(op1i, op2l));
    basicTest(op1i, op2f, doOperation(op1i, op2f));
    basicTest(op1i, op2d, doOperation(op1i, op2d));
    basicTest(op1l, op2l, doOperation(op1l, op2l));
    basicTest(op1l, op2f, doOperation(op1l, op2f));
    basicTest(op1l, op2d, doOperation(op1l, op2d));
    basicTest(op1f, op2f, doOperation(op1f, op2f));
    basicTest(op1f, op2d, doOperation(op1f, op2d));
    basicTest(op1d, op2d, doOperation(op1d, op2d));
  }

  private void basicTest(NumericLiteral literal1, NumericLiteral literal2, NumericLiteral literalResult) throws Exception {
    BinaryOperation op = getOperation(literal1, literal2);
    assertTrue(op.equals(literalResult));
    assertFalse(op.isBlank());
    assertFalse(op.isIRI());
    assertTrue(op.isLiteral());
    assertFalse(op.isURI());
    assertTrue(literalResult.getType().equals(op.getType()));
    assertEquals(SimpleLiteral.EMPTY, op.getLang());
  }

  public void testVar() throws Exception {
    Var x = new Var("x");
    Var y = new Var("y");
    BinaryOperation fn = getOperation(x, y);
    
    URI fooBar = URI.create("foo:bar");
    Literal iop1 = new LiteralImpl("" + op1().intValue(), xsdInt);
    Literal iop2 = new LiteralImpl("" + op2().intValue(), xsdInt);
    Literal lop1 = new LiteralImpl("" + op1().longValue(), xsdLong);
    Literal lop2 = new LiteralImpl("" + op2().longValue(), xsdLong);
    Literal fop1 = new LiteralImpl("" + op1().floatValue(), xsdFloat);
    Literal fop2 = new LiteralImpl("" + op2().floatValue(), xsdFloat);
    Literal dop1 = new LiteralImpl("" + op1().doubleValue(), xsdDouble);
    Literal dop2 = new LiteralImpl("" + op2().doubleValue(), xsdDouble);
    Node[][] rows = {
      new Node[] {iop1, iop2},
      new Node[] {iop1, lop2},
      new Node[] {iop1, fop2},
      new Node[] {iop1, dop2},
      new Node[] {lop1, lop2},
      new Node[] {lop1, fop2},
      new Node[] {lop1, dop2},
      new Node[] {fop1, fop2},
      new Node[] {fop1, dop2},
      new Node[] {dop1, dop2},
      // The following are to fail
      new Node[] {new LiteralImpl("foo", "en"), iop2},
      new Node[] {new LiteralImpl("foo", fooBar), iop2},
      new Node[] {new URIReferenceImpl(fooBar), iop2},
      new Node[] {new BlankNodeImpl(), iop2},
      new Node[] {null, iop2}
    };
    TestContext c = new TestContext(new String[] {"x", "y"}, rows);
    c.beforeFirst();
    fn.setContextOwner(new TestContextOwner(c));

    // check the context setting
    fn.setCurrentContext(c);

    for (int r = 0; r < 10; r++) {
      assertTrue(c.next());
      assertTrue(getLiteralResult(rows[r][0], rows[r][1]).equals(fn));
    }

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Applied operation to a language string and an integer");
    } catch (QueryException qe) { }

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Applied operation to an unknown typed literal and an integer");
    } catch (QueryException qe) { }

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Applied operation to a uri and an integer");
    } catch (QueryException qe) { }

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Applied operation to a blank node and an integer");
    } catch (QueryException qe) { }

    assertTrue(c.next());
    try {
      fn.getValue();
      fail("Applied operation to an unbound and an integer");
    } catch (QueryException qe) { }

    assertFalse(c.next());
  }
  
}
