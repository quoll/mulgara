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
import org.mulgara.query.filter.TestContext;
import org.mulgara.query.filter.TestContextOwner;
import org.mulgara.query.rdf.BlankNodeImpl;
import org.mulgara.query.rdf.LiteralImpl;

import org.mulgara.query.filter.value.Bool;
import org.mulgara.query.filter.value.Var;

import static org.mulgara.query.rdf.XSD.INT_URI;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the BOUND function.
 *
 * @created Apr 15, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class BoundFnUnitTest extends TestCase {

  protected URI xsdInt = INT_URI;
  Bool t = Bool.TRUE;
  Bool f = Bool.FALSE;

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public BoundFnUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new BoundFnUnitTest("testVar"));
    suite.addTest(new BoundFnUnitTest("testGuard"));
    return suite;
  }

  public void testVar() throws Exception {
    Var x = new Var("x");
    BoundFn fn = new BoundFn(x);

    Literal seven = new LiteralImpl("7", xsdInt);
    Node[][] rows = {
      new Node[] {seven},
      new Node[] {null},
      new Node[] {seven},
      new Node[] {null},
    };
    TestContext c = new TestContext(new String[] {"x"}, rows);
    c.beforeFirst();
    fn.setContextOwner(new TestContextOwner(c));

    // check the context setting
    fn.setCurrentContext(c);

    assertTrue(c.next());
    assertTrue(t.equals(fn));
    assertTrue(fn.equals(t));

    assertTrue(c.next());
    assertTrue(f.equals(fn));
    assertTrue(fn.equals(f));

    assertTrue(c.next());
    assertTrue(t.equals(fn));

    assertTrue(c.next());
    assertTrue(f.equals(fn));

    assertFalse(c.next());
  }
  
  public void testGuard() throws Exception {
    Var x = new Var("x");
    AbstractFilterValue fn = new And(new BoundFn(x), new IsBlankFn(x));

    Literal seven = new LiteralImpl("7", xsdInt);
    Node[][] rows = {
      new Node[] {seven},
      new Node[] {null},
      new Node[] {new BlankNodeImpl(101)},
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
    assertTrue(f.equals(fn));
    assertTrue(fn.equals(f));

    assertTrue(c.next());
    assertTrue(t.equals(fn));
    assertTrue(fn.equals(t));

    assertFalse(c.next());
  }
  
}
