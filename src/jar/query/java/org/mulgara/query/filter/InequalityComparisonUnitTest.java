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
import org.mulgara.query.rdf.LiteralImpl;

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
 * Tests the < and > functions.
 *
 * @created Apr 15, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class InequalityComparisonUnitTest extends TestCase {

  protected URI xsdInt = INT_URI;
  protected URI xsdFloat = FLOAT_URI;
  protected URI xsdString = STRING_URI;
  Bool t = Bool.TRUE;
  Bool f = Bool.FALSE;

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public InequalityComparisonUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new InequalityComparisonUnitTest("testLT"));
    suite.addTest(new InequalityComparisonUnitTest("testLTE"));
    suite.addTest(new InequalityComparisonUnitTest("testGT"));
    suite.addTest(new InequalityComparisonUnitTest("testGTE"));
    suite.addTest(new InequalityComparisonUnitTest("testVar"));
    return suite;
  }

  private void fnTst(BinaryComparisonFilter fn, Bool expected) throws QueryException {
    assertTrue(expected.equals(fn));
    assertTrue(fn.equals(expected));
  }


  public void testLT() throws Exception {
    // compares two equal literals
    fnTst(new LessThan(t, t), f);
    fnTst(new LessThan(f, f), f);
    fnTst(new LessThan(f, t), t);
    fnTst(new LessThan(t, f), f);

    ComparableExpression lhs = TypedLiteral.newLiteral(7);
    ComparableExpression rhs = TypedLiteral.newLiteral(7.0);
    fnTst(new LessThan(lhs, lhs), f);
    fnTst(new LessThan(lhs, rhs), f);
    fnTst(new LessThan(rhs, lhs), f);

    rhs = TypedLiteral.newLiteral(8.0);
    fnTst(new LessThan(lhs, rhs), t);
    fnTst(new LessThan(rhs, lhs), f);

    // compare unequal literal strings
    lhs = TypedLiteral.newLiteral("foo", null, null);
    rhs = TypedLiteral.newLiteral("fool", null, null);
    fnTst(new LessThan(lhs, rhs), t);
    fnTst(new LessThan(rhs, lhs), f);

    // compare unequal literals types
    rhs = TypedLiteral.newLiteral("foo", xsdString, null);
    try {
      assertTrue(f.equals(new LessThan(lhs, rhs)));
      fail("Unequal literals should throw an exception when compared for inequality");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error"));
    }

    Date date = new Date();
    lhs = new DateTime(date);
    rhs = new DateTime(new Date(date.getTime() + 1000));
    fnTst(new LessThan(lhs, rhs), t);
    fnTst(new LessThan(rhs, lhs), f);
  }

  public void testGT() throws Exception {
    // compares two equal literals
    fnTst(new GreaterThan(t, t), f);
    fnTst(new GreaterThan(f, f), f);
    fnTst(new GreaterThan(f, t), f);
    fnTst(new GreaterThan(t, f), t);

    ComparableExpression lhs = TypedLiteral.newLiteral(7);
    ComparableExpression rhs = TypedLiteral.newLiteral(7.0);
    fnTst(new GreaterThan(lhs, lhs), f);
    fnTst(new GreaterThan(lhs, rhs), f);
    fnTst(new GreaterThan(rhs, lhs), f);

    rhs = TypedLiteral.newLiteral(8.0);
    fnTst(new GreaterThan(lhs, rhs), f);
    fnTst(new GreaterThan(rhs, lhs), t);

    // compare unequal literal strings
    lhs = TypedLiteral.newLiteral("foo", null, null);
    rhs = TypedLiteral.newLiteral("foo", null, null);
    fnTst(new GreaterThan(lhs, rhs), f);
    fnTst(new GreaterThan(rhs, lhs), f);
    rhs = TypedLiteral.newLiteral("fool", null, null);
    fnTst(new GreaterThan(lhs, rhs), f);
    fnTst(new GreaterThan(rhs, lhs), t);

    // compare unequal literals types
    rhs = TypedLiteral.newLiteral("foo", xsdString, null);
    try {
      assertTrue(f.equals(new GreaterThan(lhs, rhs)));
      fail("Unequal literals should throw an exception when compared for inequality");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error"));
    }

    Date date = new Date();
    lhs = new DateTime(date);
    rhs = new DateTime(new Date(date.getTime() + 1000));
    fnTst(new GreaterThan(lhs, rhs), f);
    fnTst(new GreaterThan(rhs, lhs), t);
  }

  public void testLTE() throws Exception {
    // compares two equal literals
    fnTst(new LessThanEqualTo(t, t), t);
    fnTst(new LessThanEqualTo(f, f), t);
    fnTst(new LessThanEqualTo(f, t), t);
    fnTst(new LessThanEqualTo(t, f), f);

    ComparableExpression lhs = TypedLiteral.newLiteral(7);
    ComparableExpression rhs = TypedLiteral.newLiteral(7.0);
    fnTst(new LessThanEqualTo(lhs, lhs), t);
    fnTst(new LessThanEqualTo(lhs, rhs), t);
    fnTst(new LessThanEqualTo(rhs, lhs), t);

    rhs = TypedLiteral.newLiteral(8.0);
    fnTst(new LessThanEqualTo(lhs, rhs), t);
    fnTst(new LessThanEqualTo(rhs, lhs), f);

    // compare unequal literal strings
    lhs = TypedLiteral.newLiteral("foo", null, null);
    rhs = TypedLiteral.newLiteral("foo", null, null);
    fnTst(new LessThanEqualTo(lhs, rhs), t);
    fnTst(new LessThanEqualTo(rhs, lhs), t);
    rhs = TypedLiteral.newLiteral("fool", null, null);
    fnTst(new LessThanEqualTo(lhs, rhs), t);
    fnTst(new LessThanEqualTo(rhs, lhs), f);

    // compare unequal literals types
    rhs = TypedLiteral.newLiteral("foo", xsdString, null);
    try {
      assertTrue(f.equals(new LessThanEqualTo(lhs, rhs)));
      fail("Unequal literals should throw an exception when compared for inequality");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error"));
    }

    Date date = new Date();
    lhs = new DateTime(date);
    rhs = new DateTime(new Date(date.getTime() + 1000));
    fnTst(new LessThanEqualTo(lhs, rhs), t);
    fnTst(new LessThanEqualTo(rhs, lhs), f);
  }

  public void testGTE() throws Exception {
    // compares two equal literals
    fnTst(new GreaterThanEqualTo(t, t), t);
    fnTst(new GreaterThanEqualTo(f, f), t);
    fnTst(new GreaterThanEqualTo(f, t), f);
    fnTst(new GreaterThanEqualTo(t, f), t);

    ComparableExpression lhs = TypedLiteral.newLiteral(7);
    ComparableExpression rhs = TypedLiteral.newLiteral(7.0);
    fnTst(new GreaterThanEqualTo(lhs, lhs), t);
    fnTst(new GreaterThanEqualTo(lhs, rhs), t);
    fnTst(new GreaterThanEqualTo(rhs, lhs), t);

    rhs = TypedLiteral.newLiteral(8.0);
    fnTst(new GreaterThanEqualTo(lhs, rhs), f);
    fnTst(new GreaterThanEqualTo(rhs, lhs), t);

    // compare unequal literal strings
    lhs = TypedLiteral.newLiteral("foo", null, null);
    rhs = TypedLiteral.newLiteral("foo", null, null);
    fnTst(new GreaterThanEqualTo(lhs, rhs), t);
    fnTst(new GreaterThanEqualTo(rhs, lhs), t);
    rhs = TypedLiteral.newLiteral("fool", null, null);
    fnTst(new GreaterThan(lhs, rhs), f);
    fnTst(new GreaterThan(rhs, lhs), t);

    // compare unequal literals types
    rhs = TypedLiteral.newLiteral("foo", xsdString, null);
    try {
      assertTrue(f.equals(new GreaterThanEqualTo(lhs, rhs)));
      fail("Unequal literals should throw an exception when compared for inequality");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error"));
    }

    Date date = new Date();
    lhs = new DateTime(date);
    rhs = new DateTime(new Date(date.getTime() + 1000));
    fnTst(new GreaterThanEqualTo(lhs, rhs), f);
    fnTst(new GreaterThanEqualTo(rhs, lhs), t);
  }

  public void testVar() throws Exception {
    Var x = new Var("x");
    Var y = new Var("y");
    AbstractFilterValue lt = new LessThan(x, y);
    AbstractFilterValue lte = new LessThanEqualTo(x, y);
    AbstractFilterValue gt = new GreaterThan(x, y);
    AbstractFilterValue gte = new GreaterThanEqualTo(x, y);

    Literal seven = new LiteralImpl("7", xsdInt);
    Literal sevenF = new LiteralImpl("7.0", xsdFloat);
    Literal eight = new LiteralImpl("8", xsdInt);
    Literal eightF = new LiteralImpl("8.0", xsdFloat);
    Literal simple = new LiteralImpl("foo");
    Literal simpleLarge = new LiteralImpl("goo");
    Literal str = new LiteralImpl("foo", xsdString);
    Literal strLarge = new LiteralImpl("goo", xsdString);
    Node[][] rows = {
      new Node[] {seven, seven},
      new Node[] {seven, sevenF},
      new Node[] {seven, eight},
      new Node[] {seven, eightF},

      new Node[] {simple, simple},
      new Node[] {simple, simpleLarge},
      new Node[] {simple, str},

      new Node[] {seven, str},
      new Node[] {sevenF, str},
      new Node[] {str, seven},
      new Node[] {str, sevenF},

      new Node[] {str, str},
      new Node[] {str, strLarge},

      new Node[] {null, str},
    };
    TestContext c = new TestContext(new String[] {"x", "y"}, rows);
    c.beforeFirst();
    lt.setContextOwner(new TestContextOwner(c));
    lte.setContextOwner(new TestContextOwner(c));
    gt.setContextOwner(new TestContextOwner(c));
    gte.setContextOwner(new TestContextOwner(c));
    // check the context setting
    lt.setCurrentContext(c);
    lte.setCurrentContext(c);
    gt.setCurrentContext(c);
    gte.setCurrentContext(c);
                   // 0123 456 7890 12 3
    String results = "eell elx xxxx el n";
    runTests(c, lt, lte, gt, gte, results);

  }
  
  private void runTests(TestContext c, AbstractFilterValue lt, AbstractFilterValue lte, AbstractFilterValue gt, AbstractFilterValue gte, String results) throws Exception {
    c.beforeFirst();
    int i = 0;
    for (char result: results.toCharArray()) {
      if (result == ' ') continue;
      String it = "iteration: " + i++;
      assertTrue(c.next());
      switch (result) {
      case 'e':  // equal
        assertTrue(it, f.equals(lt));
        assertTrue(it, t.equals(lte));
        assertTrue(it, f.equals(gt));
        assertTrue(it, t.equals(gte));
        break;

      case 'l':  // less than
        assertTrue(it, t.equals(lt));
        assertTrue(it, t.equals(lte));
        assertTrue(it, f.equals(gt));
        assertTrue(it, f.equals(gte));
        break;

      case 'x':  // Type error
        try {
          assertTrue(it, t.equals(lt));
          fail("Unequal literals should throw an exception when compared for less than: " + i);
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Type Error"));
        }
        try {
          assertTrue(it, t.equals(lte));
          fail("Unequal literals should throw an exception when compared for less than or equal: " + i);
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Type Error"));
        }
        try {
          assertTrue(it, t.equals(gt));
          fail("Unequal literals should throw an exception when compared for greater than: " + i);
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Type Error"));
        }
        try {
          assertTrue(it, t.equals(gte));
          fail("Unequal literals should throw an exception when compared for greater than or equal: " + i);
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Type Error"));
        }
        break;

      case 'n':  // exception due to unbound
        try {
          assertTrue(it, f.equals(lt));
          fail("No exception when testing an unbound value for equality: " + i);
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Unbound column"));
        }
        try {
          assertTrue(it, f.equals(lte));
          fail("No exception when testing an unbound value for equivalency: " + i);
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Unbound column"));
        }
        try {
          assertTrue(it, f.equals(gt));
          fail("No exception when testing an unbound value for inequality: " + i);
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Unbound column"));
        }
        try {
          assertTrue(it, f.equals(gte));
          fail("No exception when testing an unbound value for inequality: " + i);
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
