package org.mulgara.query.filter;

import java.net.URI;

import org.jrdf.graph.Literal;
import org.jrdf.graph.Node;
import org.mulgara.query.QueryException;
import org.mulgara.query.filter.value.Bool;
import org.mulgara.query.filter.value.IRI;
import org.mulgara.query.filter.value.Var;
import org.mulgara.query.rdf.LiteralImpl;
import org.mulgara.query.rdf.URIReferenceImpl;
import static org.mulgara.query.rdf.XSD.INT_URI;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the isURI and isIRI functions.
 *
 * @created Apr 15, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class AbstractIsIriFnUnitTest extends TestCase {

  protected URI xsdInt = INT_URI;
  Bool t = Bool.TRUE;
  Bool f = Bool.FALSE;

  public AbstractIsIriFnUnitTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite();
  }

  abstract public AbstractFilterValue createFn(RDFTerm arg);

  public void testLiteral() throws Exception {
    AbstractFilterValue fn = createFn(t);
    assertTrue(f.equals(fn));
    assertTrue(fn.equals(f));
  
    fn = createFn(new IRI(URI.create("foo:bar")));
    assertTrue(t.equals(fn));
    assertTrue(fn.equals(t));
  
    fn = createFn(t.getType());
    assertTrue(t.equals(fn));
    assertTrue(fn.equals(t));
  }

  public void testVar() throws Exception {
    Var x = new Var("x");
    AbstractFilterValue fn = createFn(x);
  
    Literal seven = new LiteralImpl("7", xsdInt);
    Node[][] rows = {
      new Node[] {seven},
      new Node[] {null},
      new Node[] {new URIReferenceImpl(xsdInt)}
    };
    TestContext c = new TestContext(new String[] {"x"}, rows);
    c.beforeFirst();
    fn.setContextOwner(new TestContextOwner(c));
  
    // check the context setting
    fn.setCurrentContext(c);
  
    assertTrue(c.next());
    assertTrue(f.equals(fn));
    assertTrue(fn.equals(f));
  
    assertTrue(c.next());
    try {
      assertTrue(f.equals(fn));
      fail("No exception when testing an unbound value");
    } catch (QueryException qe) {
      assertTrue(qe.getMessage().startsWith("Unbound column"));
    }
  
    assertTrue(c.next());
    assertTrue(t.equals(fn));
    assertTrue(fn.equals(t));
  
    assertFalse(c.next());
  }

}