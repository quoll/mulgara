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
import org.mulgara.query.rdf.LiteralImpl;

import org.mulgara.query.filter.arithmetic.AddOperation;
import org.mulgara.query.filter.value.Bool;
import org.mulgara.query.filter.value.NumericLiteral;
import org.mulgara.query.filter.value.Var;

import static org.mulgara.query.rdf.XSD.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests the addition and comparisons functions cascaded together.
 *
 * @created Apr 15, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class CompoundStatementUnitTest extends TestCase {

  protected URI xsdInt = INT_URI;
  protected URI xsdFloat = FLOAT_URI;
  Bool t = Bool.TRUE;
  Bool f = Bool.FALSE;

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public CompoundStatementUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new CompoundStatementUnitTest("testValues"));
    suite.addTest(new CompoundStatementUnitTest("testVar"));
    return suite;
  }

  public void testValues() throws Exception {
    NumericLiteral four = new NumericLiteral(4);
    NumericLiteral five = new NumericLiteral(5);
    NumericLiteral seven = new NumericLiteral(7);
    NumericLiteral ten = new NumericLiteral(10);

    TestContext c = new TestContext(new String[] {}, new Node[][] {});
    c.beforeFirst();

    AddOperation addition = new AddOperation(five, four);
    Filter stmt = new LessThan(addition, ten);

    stmt.setContextOwner(new TestContextOwner(c));
    stmt.setCurrentContext(c);
    assertTrue(stmt.test(c));

    addition = new AddOperation(five, seven);
    stmt = new LessThan(addition, ten);

    stmt.setContextOwner(new TestContextOwner(c));
    stmt.setCurrentContext(c);
    assertTrue(!stmt.test(c));
  }


  public void testVar() throws Exception {
    Var x = new Var("x");
    Var y = new Var("y");
    NumericLiteral ten = new NumericLiteral(10);

    AddOperation addition = new AddOperation(x, y);
    Filter stmt = new LessThan(addition, ten);

    Literal four = new LiteralImpl("4", xsdInt);
    Literal five = new LiteralImpl("5", xsdInt);
    Literal seven = new LiteralImpl("7.0", xsdFloat);
    Node[][] rows = {
      new Node[] {four, five},
      new Node[] {five, seven},
      new Node[] {null, five},
    };
    TestContext c = new TestContext(new String[] {"x", "y"}, rows);
    c.beforeFirst();
    stmt.setContextOwner(new TestContextOwner(c));
    // check the context setting
    stmt.setCurrentContext(c);

    c.next();
    assertTrue(stmt.test(c));  // 4 + 5 < 10
    c.next();
    assertTrue(!stmt.test(c));  // 5 + 7 < 10
    c.next();

    try {
      assertTrue(stmt.test(c));  // null + 5 < 10
      fail("No exception when testing an unbound value for equality");
    } catch (QueryException qe) {
      String msg = qe.getMessage();
      assertTrue("Unexpected message: \"" + msg + "\"", msg.startsWith("Unbound column"));
    }

  }
  

}
