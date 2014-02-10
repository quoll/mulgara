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
import java.util.Date;

import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.TestContext;
import org.mulgara.query.filter.TestContextOwner;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;

import org.mulgara.query.filter.value.Bool;
import org.mulgara.query.filter.value.ComparableExpression;
import org.mulgara.query.filter.value.DateTime;
import org.mulgara.query.filter.value.TypedLiteral;
import org.mulgara.query.filter.value.Var;

import static org.mulgara.query.rdf.XSD.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the inequality functions.
 *
 * @created Apr 16, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class BinaryComparisonUnitTest extends TestCase {

  protected URI xsdInt = INT_URI;
  protected URI xsdFloat = FLOAT_URI;
  protected URI xsdString = STRING_URI;
  protected URI xsdDate = DATE_TIME_URI;
  Bool t = Bool.TRUE;
  Bool f = Bool.FALSE;

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public BinaryComparisonUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new BinaryComparisonUnitTest("testLiteral"));
    suite.addTest(new BinaryComparisonUnitTest("testVarCompatible"));
    suite.addTest(new BinaryComparisonUnitTest("testVarInCompatible"));
    return suite;
  }

  public void testLiteral() throws Exception {
    // integers
    ComparableExpression smaller = TypedLiteral.newLiteral(7);
    ComparableExpression larger = TypedLiteral.newLiteral(8);
    compatibleTest(smaller, larger);
    // floats
    smaller = TypedLiteral.newLiteral(7.0);
    larger = TypedLiteral.newLiteral(8.0);
    compatibleTest(smaller, larger);
    // float/integer
    larger = TypedLiteral.newLiteral(8);
    compatibleTest(smaller, larger);

    // simple literals
    smaller = TypedLiteral.newLiteral("foo", null, null);
    larger = TypedLiteral.newLiteral("goo", null, null);
    compatibleTest(smaller, larger);
    // simple literals, with language codes
    smaller = TypedLiteral.newLiteral("foo", null, "en");
    larger = TypedLiteral.newLiteral("foo", null, "fr");
    compatibleTest(smaller, larger);
    larger = TypedLiteral.newLiteral("goo", null, "en");
    compatibleTest(smaller, larger);
    // typed literal strings
    smaller = TypedLiteral.newLiteral("foo");
    larger = TypedLiteral.newLiteral("goo");
    compatibleTest(smaller, larger);

    // booleans
    smaller = f;
    larger = t;
    compatibleTest(smaller, larger);

    // typed literal strings
    Date time = new Date();
    smaller = new DateTime(time);
    time = new Date();
    time.setTime(time.getTime() + 100);
    larger = new DateTime(time);
    compatibleTest(smaller, larger);

    // compare unequal literal types starting with an int
    smaller = TypedLiteral.newLiteral(7);
    larger = TypedLiteral.newLiteral("7", null, null);
    incompatibleTest(smaller, larger);
    larger = TypedLiteral.newLiteral("foo", null, "en");
    incompatibleTest(smaller, larger);
    larger = TypedLiteral.newLiteral("foo");
    incompatibleTest(smaller, larger);
    larger = t;
    incompatibleTest(smaller, larger);
    larger = new DateTime(time);
    incompatibleTest(smaller, larger);

    // compare unequal literal types starting with a float
    smaller = TypedLiteral.newLiteral(7.0);
    larger = TypedLiteral.newLiteral("7", null, null);
    incompatibleTest(smaller, larger);
    larger = TypedLiteral.newLiteral("foo", null, "en");
    incompatibleTest(smaller, larger);
    larger = TypedLiteral.newLiteral("foo");
    incompatibleTest(smaller, larger);
    larger = t;
    incompatibleTest(smaller, larger);
    larger = new DateTime(time);
    incompatibleTest(smaller, larger);

    // compare unequal literal types starting with a simple literal
    smaller = TypedLiteral.newLiteral("foo", null, null);
    larger = TypedLiteral.newLiteral("foo");
    incompatibleTest(smaller, larger);
    larger = t;
    incompatibleTest(smaller, larger);
    larger = new DateTime(time);
    incompatibleTest(smaller, larger);

    // compare unequal literal types starting with a language coded simple literal
    smaller = TypedLiteral.newLiteral("foo", null, "en");
    larger = TypedLiteral.newLiteral("foo");
    incompatibleTest(smaller, larger);
    larger = t;
    incompatibleTest(smaller, larger);
    larger = new DateTime(time);
    incompatibleTest(smaller, larger);

    // compare unequal literal types starting with a string literal
    smaller = TypedLiteral.newLiteral("foo");
    larger = t;
    incompatibleTest(smaller, larger);
    larger = new DateTime(time);
    incompatibleTest(smaller, larger);

    // compare unequal literal types
    smaller = t;
    larger = new DateTime(time);
    incompatibleTest(smaller, larger);

    
    smaller = TypedLiteral.newLiteral("foo", null, null);
    larger = TypedLiteral.newLiteral("foo", xsdString, null);
    incompatibleTest(smaller, larger);

  }

  private void compatibleTest(ComparableExpression smaller, ComparableExpression larger) throws Exception {
    assertTrue(t.equals(new LessThan(smaller, larger)));
    assertTrue(f.equals(new LessThan(larger, smaller)));
    assertTrue(f.equals(new GreaterThan(smaller, larger)));
    assertTrue(t.equals(new GreaterThan(larger, smaller)));
    assertTrue(t.equals(new LessThanEqualTo(smaller, larger)));
    assertTrue(f.equals(new LessThanEqualTo(larger, smaller)));
    assertTrue(t.equals(new LessThanEqualTo(smaller, smaller)));
    assertTrue(f.equals(new GreaterThanEqualTo(smaller, larger)));
    assertTrue(t.equals(new GreaterThanEqualTo(larger, smaller)));
    assertTrue(t.equals(new GreaterThanEqualTo(smaller, smaller)));
  }

  private void checkIncompatible(BinaryComparisonFilter op) throws Exception {
    try {
      op.getValue();
      fail("Successfully compared incompatible types");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error:"));
    }
  }

  private void incompatibleTest(ComparableExpression lhs, ComparableExpression rhs) throws Exception {
    checkIncompatible(new LessThan(lhs, rhs));
    checkIncompatible(new LessThan(rhs, lhs));
    checkIncompatible(new GreaterThan(lhs, rhs));
    checkIncompatible(new GreaterThan(rhs, lhs));
    checkIncompatible(new LessThanEqualTo(lhs, rhs));
    checkIncompatible(new LessThanEqualTo(rhs, lhs));
    checkIncompatible(new GreaterThanEqualTo(lhs, rhs));
    checkIncompatible(new GreaterThanEqualTo(rhs, lhs));
  }

  public void testVarCompatible() throws Exception {
    Var x = new Var("x");
    Var y = new Var("y");
    BinaryComparisonFilter ltT = new LessThan(x, y);
    BinaryComparisonFilter ltF = new LessThan(y, x);
    BinaryComparisonFilter gtT = new GreaterThan(x, y);
    BinaryComparisonFilter gtF = new GreaterThan(y, x);

    BinaryComparisonFilter lteT = new LessThanEqualTo(x, y);
    BinaryComparisonFilter lteF = new LessThanEqualTo(y, x);
    BinaryComparisonFilter lteE = new LessThanEqualTo(x, x);
    BinaryComparisonFilter gteT = new GreaterThanEqualTo(x, y);
    BinaryComparisonFilter gteF = new GreaterThanEqualTo(y, x);
    BinaryComparisonFilter gteE = new GreaterThanEqualTo(x, x);

    BinaryComparisonFilter[] comps  = new BinaryComparisonFilter[] { ltT, ltF, gtT, gtF, lteT, lteF, lteE, gteT, gteF, gteE };
    
    Literal seven = new LiteralImpl("7", xsdInt);
    Literal eight = new LiteralImpl("8", xsdInt);
    Literal sevenF = new LiteralImpl("7.0", xsdFloat);
    Literal eightF = new LiteralImpl("8.0", xsdFloat);
    Literal simpleFoo = new LiteralImpl("foo");
    Literal simpleGoo = new LiteralImpl("goo");
    Literal simpleFooEn = new LiteralImpl("foo", "en");
    Literal simpleFooFr = new LiteralImpl("foo", "fr");
    Literal simpleGooEn = new LiteralImpl("goo", "en");
    Literal foo = new LiteralImpl("foo", xsdString);
    Literal goo = new LiteralImpl("goo", xsdString);
    Literal litFalse = new LiteralImpl("false", t.getType().getValue());
    Literal litTrue = new LiteralImpl("true", t.getType().getValue());
    Literal now = new LiteralImpl("2008-04-16T21:57:00Z", xsdDate);
    Literal soon = new LiteralImpl("2008-04-16T21:58:01Z", xsdDate);
    Node[][] rows = {
      new Node[] {seven, eight},
      new Node[] {sevenF, eightF},

      new Node[] {simpleFoo, simpleGoo},
      new Node[] {simpleFooEn, simpleFooFr},
      new Node[] {simpleFooEn, simpleGooEn},
      new Node[] {foo, goo},
      new Node[] {litFalse, litTrue},
      new Node[] {now, soon}
    };
    TestContext c = new TestContext(new String[] {"x", "y"}, rows);
    c.beforeFirst();
    TestContextOwner ctxOwner = new TestContextOwner(c);
    for (BinaryComparisonFilter f: comps) f.setContextOwner(ctxOwner);

    // check the context setting
    for (BinaryComparisonFilter f: comps) f.setCurrentContext(c);

    // run the tests
    while (c.next()) compatibleTest(c, comps);
  }

  private void compatibleTest(TestContext c, BinaryComparisonFilter[] comps) throws Exception {
    assertTrue(t.equals(comps[0]));
    assertTrue(f.equals(comps[1]));
    assertTrue(f.equals(comps[2]));
    assertTrue(t.equals(comps[3]));
    assertTrue(t.equals(comps[4]));
    assertTrue(f.equals(comps[5]));
    assertTrue(t.equals(comps[6]));
    assertTrue(f.equals(comps[7]));
    assertTrue(t.equals(comps[8]));
    assertTrue(t.equals(comps[9]));
  }

  public void testVarInCompatible() throws Exception {
    Var x = new Var("x");
    Var y = new Var("y");
    BinaryComparisonFilter ltT = new LessThan(x, y);
    BinaryComparisonFilter ltF = new LessThan(y, x);
    BinaryComparisonFilter gtT = new GreaterThan(x, y);
    BinaryComparisonFilter gtF = new GreaterThan(y, x);
    BinaryComparisonFilter lteT = new LessThanEqualTo(x, y);
    BinaryComparisonFilter lteF = new LessThanEqualTo(y, x);
    BinaryComparisonFilter gteT = new GreaterThanEqualTo(x, y);
    BinaryComparisonFilter gteF = new GreaterThanEqualTo(y, x);

    BinaryComparisonFilter[] comps  = new BinaryComparisonFilter[] { ltT, ltF, gtT, gtF, lteT, lteF, gteT, gteF };
    
    Literal seven = new LiteralImpl("7", xsdInt);
    Literal sevenSimple = new LiteralImpl("7");
    Literal sevenF = new LiteralImpl("7.0", xsdFloat);
    Literal simpleFoo = new LiteralImpl("foo");
    Literal simpleFooEn = new LiteralImpl("foo", "en");
    Literal foo = new LiteralImpl("foo", xsdString);
    Literal litTrue = new LiteralImpl("true", t.getType().getValue());
    Literal now = new LiteralImpl("2008-04-16T21:57:00Z", xsdDate);
    Literal nowDt = new LiteralImpl("2008-04-16T21:57:00Z", DATE_URI);
    URIReferenceImpl intRef = new URIReferenceImpl(xsdInt);
    BlankNodeImpl bn = new BlankNodeImpl(101);
    Node[][] rows = {
      new Node[] {seven, sevenSimple},
      new Node[] {seven, simpleFooEn},
      new Node[] {seven, foo},
      new Node[] {seven, litTrue},
      new Node[] {seven, now},
      new Node[] {sevenF, sevenSimple},
      new Node[] {sevenF, simpleFooEn},
      new Node[] {sevenF, foo},
      new Node[] {sevenF, litTrue},
      new Node[] {sevenF, now},
      new Node[] {simpleFoo, foo},
      new Node[] {simpleFoo, litTrue},
      new Node[] {simpleFoo, now},
      new Node[] {simpleFooEn, foo},
      new Node[] {simpleFooEn, litTrue},
      new Node[] {simpleFooEn, now},
      new Node[] {foo, litTrue},
      new Node[] {foo, now},  // 17
      new Node[] {foo, nowDt},
      new Node[] {litTrue, now},
      new Node[] {seven, intRef},  // 20
      new Node[] {sevenF, intRef},
      new Node[] {simpleFoo, intRef},
      new Node[] {simpleFooEn, intRef},
      new Node[] {foo, intRef},
      new Node[] {litTrue, intRef},
      new Node[] {now, intRef},
      new Node[] {seven, bn},
      new Node[] {sevenF, bn},
      new Node[] {simpleFoo, bn},
      new Node[] {simpleFooEn, bn},
      new Node[] {foo, bn},
      new Node[] {litTrue, bn},
      new Node[] {now, bn}
    };
    TestContext c = new TestContext(new String[] {"x", "y"}, rows);
    c.beforeFirst();
    TestContextOwner ctxOwner = new TestContextOwner(c);
    for (BinaryComparisonFilter f: comps) f.setContextOwner(ctxOwner);

    // check the context setting
    for (BinaryComparisonFilter f: comps) f.setCurrentContext(c);

    // run the tests
    int r = 0;
    while (c.next()) {
      int test = 0;
      try {
        for (BinaryComparisonFilter f: comps) {
          checkIncompatible(f);
          test++;
        }
      } catch (Error e) {
        System.err.println("Failed on row: " + r + "  test=" + test);
        throw e;
      }
      r++;
    }
  }

}
