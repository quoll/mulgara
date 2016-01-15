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

import org.mulgara.query.filter.value.BlankNodeValue;
import org.mulgara.query.filter.value.Bool;
import org.mulgara.query.filter.value.IRI;
import org.mulgara.query.filter.value.TypedLiteral;
import org.mulgara.query.filter.value.Var;

import static org.mulgara.query.rdf.XSD.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the equals, not-equals, and sameTerm functions.
 *
 * @created Apr 15, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class EqualityComparisonUnitTest extends TestCase {

  protected URI xsdInt = INT_URI;
  protected URI xsdFloat = FLOAT_URI;
  protected URI xsdString = STRING_URI;
  Bool t = Bool.TRUE;
  Bool f = Bool.FALSE;

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public EqualityComparisonUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new EqualityComparisonUnitTest("testLiteral"));
    suite.addTest(new EqualityComparisonUnitTest("testVar"));
    return suite;
  }

  public void testLiteral() throws Exception {
    // compares two equal literals
    Equals fn = new Equals(t, t);
    assertTrue(t.equals(fn));
    assertTrue(fn.equals(t));
    assertTrue(t.equals(new SameTerm(t, t)));
    assertTrue(f.equals(new NotEquals(t, t)));

    // compares two other equal literals
    fn = new Equals(f, f);
    assertTrue(t.equals(fn));
    assertTrue(fn.equals(t));
    assertTrue(t.equals(new SameTerm(f, f)));
    assertTrue(f.equals(new NotEquals(f, f)));

    // compare unequal literals
    RDFTerm lhs = t;
    RDFTerm rhs = f;
    assertTrue(f.equals(new Equals(lhs, rhs)));
    assertTrue(f.equals(new SameTerm(lhs, rhs)));
    assertTrue(t.equals(new NotEquals(lhs, rhs)));

    // compare incomparable literals
    lhs = TypedLiteral.newLiteral(7);
    try {
      assertTrue(f.equals(new Equals(lhs, rhs)));
      fail("Incomparable literals should throw an exception when compared for equality");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error"));
    }

    // compare equivalent but different literals
    lhs = TypedLiteral.newLiteral(7);
    rhs = TypedLiteral.newLiteral(7.0);
    assertTrue(t.equals(new Equals(lhs, rhs)));
    assertTrue(f.equals(new SameTerm(lhs, rhs)));
    assertTrue(f.equals(new NotEquals(lhs, rhs)));

    // compare unequal literal strings
    lhs = TypedLiteral.newLiteral("foo", null, null);
    rhs = TypedLiteral.newLiteral("fool", null, null);
    assertTrue(f.equals(new Equals(lhs, rhs)));
    assertTrue(f.equals(new SameTerm(lhs, rhs)));
    assertTrue(t.equals(new NotEquals(lhs, rhs)));

    // compare unequal literals types
    lhs = TypedLiteral.newLiteral("foo", null, null);
    rhs = TypedLiteral.newLiteral("foo", xsdString, null);
    try {
      assertTrue(f.equals(new Equals(lhs, rhs)));
      fail("Unequal literals should throw an exception when compared for equality");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error"));
    }
    assertTrue(f.equals(new SameTerm(lhs, rhs)));
    try {
      assertTrue(t.equals(new NotEquals(lhs, rhs)));
      fail("Unequal literals should throw an exception when compared for equality");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Type Error"));
    }

    // compare unequal languages
    lhs = TypedLiteral.newLiteral("foo", null, "en");
    rhs = TypedLiteral.newLiteral("foo", null, "fr");
    assertTrue(f.equals(new Equals(lhs, rhs)));
    assertTrue(f.equals(new SameTerm(lhs, rhs)));
    assertTrue(t.equals(new NotEquals(lhs, rhs)));

    // compare equal languages
    lhs = TypedLiteral.newLiteral("foo", null, "en");
    rhs = TypedLiteral.newLiteral("foo", null, "en");
    assertTrue(t.equals(new Equals(lhs, rhs)));
    assertTrue(t.equals(new SameTerm(lhs, rhs)));
    assertTrue(f.equals(new NotEquals(lhs, rhs)));

    // compare different URIs
    lhs = new IRI(URI.create("http://mulgara.org/path/to/data.rdf"));
    rhs = new IRI(URI.create("http://mulgara.org/path/to/../to/data.rdf"));
    assertTrue(f.equals(new Equals(lhs, rhs)));
    assertTrue(f.equals(new SameTerm(lhs, rhs)));
    assertTrue(t.equals(new NotEquals(lhs, rhs)));

    // compare the same URIs
    lhs = new IRI(URI.create("http://mulgara.org/path/to/data.rdf"));
    rhs = new IRI(URI.create("http://mulgara.org/path/to/data.rdf"));
    assertTrue(t.equals(new Equals(lhs, rhs)));
    assertTrue(t.equals(new SameTerm(lhs, rhs)));
    assertTrue(f.equals(new NotEquals(lhs, rhs)));

    // compare different blank nodes
    lhs = new BlankNodeValue(new BlankNodeImpl(101));
    rhs =  new BlankNodeValue(new BlankNodeImpl(102));
    assertTrue(f.equals(new Equals(lhs, rhs)));
    assertTrue(f.equals(new SameTerm(lhs, rhs)));
    assertTrue(t.equals(new NotEquals(lhs, rhs)));

    // compare the same blank nodes
    lhs = new BlankNodeValue(new BlankNodeImpl(42));
    rhs =  new BlankNodeValue(new BlankNodeImpl(42));
    assertTrue(t.equals(new Equals(lhs, rhs)));
    assertTrue(t.equals(new SameTerm(lhs, rhs)));
    assertTrue(f.equals(new NotEquals(lhs, rhs)));
  }

  public void testVar() throws Exception {
    Var x = new Var("x");
    Var y = new Var("y");
    AbstractFilterValue eq = new Equals(x, y);
    AbstractFilterValue same = new SameTerm(x, y);
    AbstractFilterValue ne = new NotEquals(x, y);

    Literal seven = new LiteralImpl("7", xsdInt);
    Literal sevenF = new LiteralImpl("7.0", xsdFloat);
    Literal simple = new LiteralImpl("foo");
    Literal str = new LiteralImpl("foo", xsdString);
    Literal strEn = new LiteralImpl("foo", "en");
    Literal strFr = new LiteralImpl("foo", "fr");
    Node[][] rows = {
      new Node[] {seven, seven},
      new Node[] {seven, sevenF},

      new Node[] {simple, simple},
      new Node[] {simple, str},
      new Node[] {simple, strEn},
      new Node[] {simple, strFr},

      new Node[] {seven, str},
      new Node[] {sevenF, str},
      new Node[] {str, seven},
      new Node[] {str, sevenF},

      new Node[] {str, str},
      new Node[] {str, strEn},
      new Node[] {strEn, strEn},
      new Node[] {strEn, strFr},

      new Node[] {str, new URIReferenceImpl(xsdInt)},
      new Node[] {str, new BlankNodeImpl(101)},

      new Node[] {new URIReferenceImpl(xsdInt), new URIReferenceImpl(xsdInt)},
      new Node[] {new URIReferenceImpl(xsdInt), new URIReferenceImpl(xsdFloat)},
      new Node[] {new URIReferenceImpl(xsdInt), new BlankNodeImpl(100)},

      new Node[] {null, str},

      new Node[] {new BlankNodeImpl(101), new BlankNodeImpl(101)},
      new Node[] {new BlankNodeImpl(101), new BlankNodeImpl(102)}
    };
    TestContext c = new TestContext(new String[] {"x", "y"}, rows);
    c.beforeFirst();
    eq.setContextOwner(new TestContextOwner(c));
    same.setContextOwner(new TestContextOwner(c));
    ne.setContextOwner(new TestContextOwner(c));
    // check the context setting
    eq.setCurrentContext(c);
    same.setCurrentContext(c);
    ne.setCurrentContext(c);
                   // 01 2345 6789 0123 45 678 9 01
    String results = "te tlff llll tltf ff tff x tf";
    runTests(c, eq, same, ne, results);

  }
  
  private void runTests(TestContext c, AbstractFilterValue eq, AbstractFilterValue same, AbstractFilterValue ne, String results) throws Exception {
    c.beforeFirst();
    int i = 0;
    for (char result: results.toCharArray()) {
      if (result == ' ') continue;
      String it = "iteration: " + i++;
      assertTrue(c.next());
      switch (result) {
      case 't':  // equal
        assertTrue(it, t.equals(eq));
        assertTrue(it, t.equals(same));
        assertTrue(it, f.equals(ne));
        break;

      case 'f':  // unequal
        assertTrue(it, f.equals(eq));
        assertTrue(it, f.equals(same));
        assertTrue(it, t.equals(ne));
        break;

      case 'l':  // unequal literals
        assertTrue(it, f.equals(same));
        try {
          assertTrue(it, t.equals(ne));
          fail("Unequal literals should throw an exception when compared for equality: " + i);
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Type Error"));
        }
        try {
          assertTrue(it, f.equals(eq));
          fail("Unequal literals should throw an exception when compared for equality: " + i);
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Type Error"));
        }
        break;

      case 'e':  // equivalent but unequal
        assertTrue(it, f.equals(same));
        assertTrue(it, f.equals(ne));
        assertTrue(it, t.equals(eq));
        break;

      case 'x':  // exception due to unbound
        try {
          assertTrue(it, f.equals(eq));
          fail("No exception when testing an unbound value for equality: " + i);
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Unbound column"));
        }
        try {
          assertTrue(it, f.equals(same));
          fail("No exception when testing an unbound value for equivalency: " + i);
        } catch (QueryException qe) {
          assertTrue(qe.getMessage().startsWith("Unbound column"));
        }
        try {
          assertTrue(it, f.equals(ne));
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
