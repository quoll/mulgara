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
import java.util.ArrayList;
import java.util.List;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.jrdf.graph.URIReference;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.Context;
import org.mulgara.query.filter.ContextOwner;
import org.mulgara.query.filter.TestContext;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the variable class.
 *
 * @created Apr 10, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class VarUnitTest extends TestCase implements ContextOwner {

  /** The context for the variables */
  TestContext context;

  /** A literal used in the context */
  Literal foo = new LiteralImpl("foo");

  /** A URI used in the context */
  URIReference foobar = new URIReferenceImpl(URI.create("foo:bar"));

  /** A blank node used in the context */
  BlankNode bn = new BlankNodeImpl();

  /** A list of context owners that we may want to update if the context changes */
  List<ContextOwner> contextListeners = new ArrayList<ContextOwner>();

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public VarUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new VarUnitTest("testLiteral"));
    suite.addTest(new VarUnitTest("testURI"));
    suite.addTest(new VarUnitTest("testBN"));
    suite.addTest(new VarUnitTest("testUnbound"));
    return suite;
  }

  public void setUp() throws Exception {
    String[] vars = new String[] {"x"};

    Node[][] rows = {
      new Node[] {foo},
      new Node[] {foobar},
      new Node[] {bn},
      new Node[] {null}
    };
    context = new TestContext(vars, rows);
  }

  public void testLiteral() throws Exception {
    Var v = new Var("x");
    v.setContextOwner(this);
    context.beforeFirst();
    context.next();

    assertTrue(v.isBound());
    assertEquals(foo.getLexicalForm(), v.getValue());
    try {
      v.getNumber();
      assertTrue(false);
    } catch (QueryException e) { }
    assertEquals(foo.getLexicalForm(), v.getLexical());
    assertEquals(SimpleLiteral.STRING_TYPE, v.getType());
    assertTrue(v.equals(new SimpleLiteral("foo")));
    assertTrue(v.greaterThan(new SimpleLiteral("eoo")));
    assertTrue(v.greaterThanEqualTo(new SimpleLiteral("eoo")));
    assertTrue(v.lessThan(new SimpleLiteral("goo")));
    assertTrue(v.lessThanEqualTo(new SimpleLiteral("goo")));
    assertFalse(v.isBlank());
    assertFalse(v.isIRI());
    assertTrue(v.isLiteral());
    assertFalse(v.isURI());
    assertTrue(v.sameTerm(new SimpleLiteral("foo")));
    assertEquals(v.getLang(), SimpleLiteral.EMPTY);
    assertTrue(v.test(context));
  }

  public void testURI() throws Exception {
    Var v = new Var("x");
    v.setContextOwner(this);
    context.beforeFirst();
    context.next();
    context.next();

    assertTrue(v.isBound());
    assertEquals(foobar.getURI(), v.getValue());
    try {
      v.getNumber();
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      assertEquals(foobar.getURI().toString(), v.getLexical());
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.getType();
      assertTrue(false);
    } catch (QueryException e) { }
    IRI i = new IRI(foobar.getURI());
    assertTrue(v.equals(i));

    SimpleLiteral s = new SimpleLiteral("foobar");
    try {
      v.greaterThan(s);
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.greaterThanEqualTo(s);
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.lessThan(s);
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.lessThanEqualTo(s);
      assertTrue(false);
    } catch (QueryException e) { }

    assertFalse(v.isBlank());
    assertTrue(v.isIRI());
    assertFalse(v.isLiteral());
    assertTrue(v.isURI());
    assertTrue(v.sameTerm(i));
    try {
      v.getLang();
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.test(context);
      assertTrue(false);
    } catch (QueryException e) { }
  }

  public void testBN() throws Exception {
    Var v = new Var("x");
    v.setContextOwner(this);
    context.beforeFirst();
    context.next();
    context.next();
    context.next();

    assertTrue(v.isBound());
    assertEquals(bn, v.getValue());
    try {
      v.getNumber();
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      assertEquals(bn.toString(), v.getLexical());
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.getType();
      assertTrue(false);
    } catch (QueryException e) { }
    BlankNodeValue b = new BlankNodeValue(bn);
    assertTrue(v.equals(b));

    SimpleLiteral s = new SimpleLiteral("foobar");
    try {
      v.greaterThan(s);
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.greaterThanEqualTo(s);
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.lessThan(s);
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.lessThanEqualTo(s);
      assertTrue(false);
    } catch (QueryException e) { }

    assertTrue(v.isBlank());
    assertFalse(v.isIRI());
    assertFalse(v.isLiteral());
    assertFalse(v.isURI());
    assertTrue(v.sameTerm(b));
    try {
      v.getLang();
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.test(context);
      assertTrue(false);
    } catch (QueryException e) { }
  }

  public void testUnbound() throws Exception {
    Var v = new Var("x");
    v.setContextOwner(this);
    context.beforeFirst();
    context.next();
    context.next();
    context.next();
    context.next();

    assertFalse(v.isBound());
    try {
      v.getValue();
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.getNumber();
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.getLexical();
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.getType();
      assertTrue(false);
    } catch (QueryException e) { }
    BlankNodeValue b = new BlankNodeValue(bn);
    try {
      v.equals(b);
      assertTrue(false);
    } catch (QueryException e) { }

    SimpleLiteral s = new SimpleLiteral("foobar");
    try {
      v.greaterThan(s);
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.greaterThanEqualTo(s);
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.lessThan(s);
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.lessThanEqualTo(s);
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.isBlank();
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.isIRI();
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.isLiteral();
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.isURI();
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.sameTerm(b);
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.getLang();
      assertTrue(false);
    } catch (QueryException e) { }
    try {
      v.test(context);
      assertTrue(false);
    } catch (QueryException e) { }
  }

  public Context getCurrentContext() {
    return context;
  }

  public void setCurrentContext(Context context) {
    for (ContextOwner l: contextListeners) l.setCurrentContext(context);
  }

  /**
   * This provides a context, and does not need to refer to a parent.
   * @see org.mulgara.query.filter.ContextOwner#getContextOwner()
   */
  public ContextOwner getContextOwner() {
    throw new IllegalStateException("Should never be asking for the context owner of a Tuples");
  }

  /**
   * The owner of the context for a Tuples is never needed, since it is always provided by the Tuples.
   * @see org.mulgara.query.filter.ContextOwner#setContextOwner(org.mulgara.query.filter.ContextOwner)
   */
  public void setContextOwner(ContextOwner owner) { }

  /**
   * This provides a context and cannot be a parent
   * @see org.mulgara.query.filter.ContextOwner#addContextListener(org.mulgara.query.filter.ContextOwner)
   */
  public void addContextListener(ContextOwner l) {
    contextListeners.add(l);
  }
}
