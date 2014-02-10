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

import java.net.URI;

import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.TestContext;
import org.mulgara.query.filter.TestContextOwner;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import static org.mulgara.query.rdf.XSD.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the data type function class.
 *
 * @created Mar 31, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class DataTypeFnUnitTest extends TestCase {

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public DataTypeFnUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new DataTypeFnUnitTest("testValues"));
    suite.addTest(new DataTypeFnUnitTest("testVar"));
    return suite;
  }


  public void testValues() throws Exception {
    String str = "test";
    URI t = STRING_URI;
    TypedLiteral l = (TypedLiteral) TypedLiteral.newLiteral(str, t, null);
    DataTypeFn fn = new DataTypeFn(l);
    IRI i = new IRI(t);
    assertTrue(fn.equals(i));
    assertFalse(fn.isBlank());
    assertTrue(fn.isIRI());
    assertFalse(fn.isLiteral());
    assertTrue(fn.isURI());
    try {
      fn.getType();
      fail("Should not be able to get a type for an IRI");
    } catch (QueryException qe) { }
    try {
      fn.getLang();
      fail("Should not be able to get a lang for an IRI");
    } catch (QueryException qe) { }

    l = (TypedLiteral)TypedLiteral.newLiteral(str);
    fn = new DataTypeFn(l);
    i = new IRI(t);
    assertTrue(fn.equals(i));
    assertFalse(fn.isBlank());
    assertTrue(fn.isIRI());
    assertFalse(fn.isLiteral());
    assertTrue(fn.isURI());

    String s2 = "foobar";
    URI t2 = URI.create(NAMESPACE + "foo:bar");
    l = (TypedLiteral)TypedLiteral.newLiteral(s2, t2, null);
    fn = new DataTypeFn(l);
    i = new IRI(t2);
    assertTrue(fn.equals(i));
    assertFalse(fn.isBlank());
    assertTrue(fn.isIRI());
    assertFalse(fn.isLiteral());
    assertTrue(fn.isURI());

    Long v = Long.valueOf(5);
    l = (TypedLiteral)TypedLiteral.newLiteral(v);
    fn = new DataTypeFn(l);
    i = new IRI(LONG_URI);
    assertTrue(fn.equals(i));
  }

  public void testVar() throws Exception {
    String vName = "foo";
    Var v = new Var(vName);
    DataTypeFn fn = new DataTypeFn(v);
    
    URI xsdString = STRING_URI;
    URI xsdDouble = DOUBLE_URI;
    URI fooBar = URI.create("foo:bar");
    Node[][] rows = {
      new Node[] {new LiteralImpl("foo")},
      new Node[] {new LiteralImpl("foo", xsdString)},
      new Node[] {new LiteralImpl("5.0", xsdDouble)},
      new Node[] {new LiteralImpl("foo", "en")},
      new Node[] {new LiteralImpl("foo", fooBar)},
      new Node[] {new URIReferenceImpl(fooBar)},
      new Node[] {new BlankNodeImpl()},
    };
    TestContext c = new TestContext(new String[] {vName}, rows);
    c.beforeFirst();
    fn.setContextOwner(new TestContextOwner(c));

    // check the context setting
    fn.setCurrentContext(c);

    assertTrue(c.next());
    assertEquals(xsdString, fn.getValue());
    assertTrue(c.next());
    assertEquals(xsdString, fn.getValue());
    assertTrue(c.next());
    assertEquals(xsdDouble, fn.getValue());
    assertTrue(c.next());
    assertEquals(SimpleLiteral.STRING_TYPE.getValue(), fn.getValue());
    assertTrue(c.next());
    assertEquals(fooBar, fn.getValue());
    assertTrue(c.next());
    try {
      Object o = fn.getValue();
      fail("Got the type of a URI reference: " + o);
    } catch (QueryException qe) { }
    assertTrue(c.next());
    try {
      Object o = fn.getValue();
      fail("Got the type of a Blank Node: " + o);
    } catch (QueryException qe) { }
    assertFalse(c.next());
  }
  
}
