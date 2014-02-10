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
import org.mulgara.parser.MulgaraParserException;
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
 * Tests the external function class for construction functions.
 *
 * @created Apr 30, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class ExternalFnUnitTest extends TestCase {

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public ExternalFnUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new ExternalFnUnitTest("testValues"));
    suite.addTest(new ExternalFnUnitTest("testVarStr"));
    suite.addTest(new ExternalFnUnitTest("testVarInt"));
    return suite;
  }


  public void testValues() throws Exception {
    IRI xsdStr = new IRI(STRING_URI);
    IRI xsdInt = new IRI(INT_URI);
    IRI testUri = new IRI(URI.create("foo:bar"));
    ValueLiteral strData = TypedLiteral.newLiteral("42", xsdStr.getValue(), null);
    ValueLiteral intData = new NumericLiteral(Integer.valueOf(42));
    
    ExternalFn fn = new ExternalFn(xsdInt, strData);
    assertTrue(fn.equals(intData));
    assertEquals(xsdInt, fn.getType());

    fn = new ExternalFn(xsdStr, intData);
    assertTrue(fn.equals(strData));
    assertEquals(xsdStr, fn.getType());

    fn = new ExternalFn(xsdStr, new ExternalFn(xsdInt, strData));
    assertTrue(fn.equals(strData));
    assertEquals(xsdStr, fn.getType());

    fn = new ExternalFn(xsdStr, testUri);
    assertTrue(fn.equals(TypedLiteral.newLiteral(testUri.getValue().toString(), xsdStr.getValue(), null)));
    assertEquals(xsdStr, fn.getType());

    fn = new ExternalFn(xsdInt, testUri);
    try {
      assertFalse(fn.equals(testUri));
      fail("Unexpectedly converted an IRI to an integer");
    } catch (QueryException qe) { }

    fn = new ExternalFn(xsdInt, new SimpleLiteral("42"));
    assertTrue(fn.equals(intData));
    assertEquals(xsdInt, fn.getType());

    fn = new ExternalFn(xsdInt, new SimpleLiteral("42", "en"));
    assertTrue(fn.equals(intData));
    assertEquals(xsdInt, fn.getType());

    try {
      fn = new ExternalFn(xsdInt, testUri, xsdInt);
      fail("Unexpectedly created an XSD function with 2 parameters");
    } catch (MulgaraParserException qe) { }
  }

  public void testVarStr() throws Exception {
    URI fooBar = URI.create("foo:bar");
    Node[][] rows = {
      new Node[] {new LiteralImpl("foo")},
      new Node[] {new LiteralImpl("foo", STRING_URI)},
      new Node[] {new LiteralImpl("5", INT_URI)},
      new Node[] {new LiteralImpl("5.0", DOUBLE_URI)},
      new Node[] {new LiteralImpl("foo", "en")},
      new Node[] {new LiteralImpl("foo", fooBar)},
      new Node[] {new URIReferenceImpl(fooBar)},
      new Node[] {new BlankNodeImpl()},
    };

    String vData = "data";
    TestContext c = new TestContext(new String[] {vData}, rows);
    c.beforeFirst();

    IRI StringIri = new IRI(STRING_URI);
    ExternalFn fn = new ExternalFn(StringIri, new Var(vData));
    fn.setContextOwner(new TestContextOwner(c));
    ValueLiteral fooStr = TypedLiteral.newLiteral("foo");
    ValueLiteral fiveInt = TypedLiteral.newLiteral(Integer.valueOf(5).toString());
    ValueLiteral fiveDbl = TypedLiteral.newLiteral(Double.valueOf(5.0).toString());
    ValueLiteral fooBarIriStr = TypedLiteral.newLiteral("foo:bar");

    // check the context setting
    fn.setCurrentContext(c);

    assertTrue(c.next());
    assertEquals("foo", fn.getValue());
    assertTrue(fooStr.equals(fn));
    assertTrue(fn.equals(fooStr));
    assertEquals(StringIri, fn.getType());

    assertTrue(c.next());
    assertEquals("foo", fn.getValue());
    assertTrue(fooStr.equals(fn));
    assertTrue(fn.equals(fooStr));
    assertEquals(StringIri, fn.getType());

    assertTrue(c.next());
    assertEquals("5", fn.getValue());
    assertTrue(fiveInt.equals(fn));
    assertTrue(fn.equals(fiveInt));
    assertEquals(StringIri, fn.getType());

    assertTrue(c.next());
    assertEquals("5.0", fn.getValue());
    assertTrue(fiveDbl.equals(fn));
    assertTrue(fn.equals(fiveDbl));
    assertEquals(StringIri, fn.getType());

    assertTrue(c.next());
    assertEquals("foo", fn.getValue());
    assertTrue(fooStr.equals(fn));
    assertTrue(fn.equals(fooStr));
    assertEquals(StringIri, fn.getType());

    assertTrue(c.next());
    assertEquals("foo", fn.getValue());
    assertTrue(fooStr.equals(fn));
    assertTrue(fn.equals(fooStr));
    assertEquals(StringIri, fn.getType());

    assertTrue(c.next());
    assertEquals("foo:bar", fn.getValue());
    assertTrue(fooBarIriStr.equals(fn));
    assertTrue(fn.equals(fooBarIriStr));
    assertEquals(StringIri, fn.getType());

    assertTrue(c.next());
    try {
      Object o = fn.getValue();
      fail("Converted a blank node to a string: " + o);
    } catch (QueryException qe) { }
    assertFalse(c.next());

  }
  
  public void testVarInt() throws Exception {
    IRI intIri = new IRI(INT_URI);
    URI fooBar = URI.create("foo:bar");
    Node[][] rows = {
      new Node[] {new LiteralImpl("42")},
      new Node[] {new LiteralImpl("42", INT_URI)},
      new Node[] {new LiteralImpl("42.0", DOUBLE_URI)},
      new Node[] {new LiteralImpl("42.2", DOUBLE_URI)},
      new Node[] {new LiteralImpl("foo", "en")},
      new Node[] {new LiteralImpl("foo", fooBar)},
      new Node[] {new URIReferenceImpl(fooBar)},
      new Node[] {new BlankNodeImpl()},
    };

    String vData = "data";
    TestContext c = new TestContext(new String[] {vData}, rows);
    c.beforeFirst();

    ExternalFn fn = new ExternalFn(intIri, new Var(vData));
    fn.setContextOwner(new TestContextOwner(c));
    Integer ft = Integer.valueOf(42);
    ValueLiteral fortyTwo = new NumericLiteral(ft);

    // check the context setting
    fn.setCurrentContext(c);

    assertTrue(c.next()); // "42"^^xsd:string
    assertEquals(ft, fn.getValue());
    assertTrue(fortyTwo.equals(fn));
    assertTrue(fn.equals(fortyTwo));
    assertEquals(intIri, fn.getType());

    assertTrue(c.next()); // "42"^^xsd:int
    assertEquals(ft, fn.getValue());
    assertTrue(fortyTwo.equals(fn));
    assertTrue(fn.equals(fortyTwo));
    assertEquals(intIri, fn.getType());

    assertTrue(c.next()); // "42.0"^^xsd:double
    assertEquals(ft, fn.getValue());
    assertTrue(fortyTwo.equals(fn));
    assertTrue(fn.equals(fortyTwo));
    assertEquals(intIri, fn.getType());

    assertTrue(c.next()); // "42.2"^^xsd:double
    assertEquals(ft, fn.getValue());
    assertTrue(fortyTwo.equals(fn));
    assertTrue(fn.equals(fortyTwo));
    assertEquals(intIri, fn.getType());

    assertTrue(c.next());
    try {
      Object o = fn.getValue();
      fail("Converted a language coded string to an int: " + o);
    } catch (QueryException qe) { }

    assertTrue(c.next());
    try {
      Object o = fn.getValue();
      fail("Converted an unknown type to an int: " + o);
    } catch (QueryException qe) { }

    assertTrue(c.next());
    try {
      Object o = fn.getValue();
      fail("Converted a URI to an int: " + o);
    } catch (QueryException qe) { }

    assertTrue(c.next());
    try {
      Object o = fn.getValue();
      fail("Converted a blank node to an int: " + o);
    } catch (QueryException qe) { }

    assertFalse(c.next());

  }
  
}
