/*
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

package org.mulgara.krule.rlog.ast;

import org.apache.log4j.Logger;
import org.mulgara.krule.rlog.ParseContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 *
 * @created Mar 10, 2009
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class CanonicalPredicateTest extends TestCase {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(CanonicalPredicateTest.class.getName());

  ParseContext context = new ParseContext();

  /**
   * Create the test
   * @param name The name of the test.
   */
  public CanonicalPredicateTest(String name) {
    super(name);
  }


  /**
   * Hook from which the test runner can obtain a test suite.
   *
   * @return the test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(CanonicalPredicateTest.class);
    return suite;
  }


  /**
   * This is a hook for initializing a test.
   */
  public void setup() {
  }


  /**
   * Test that a predicate can be canonicalized.
   *
   * @throws Exception Due to any kind of internal failure.
   */
  public void testBinaryLiteral() throws Exception {
    PredicateParam left = new BPredicateLiteral("foo", "bar", context);
    PredicateParam right = new BPredicateLiteral("foo", "baz", context);
    BPredicateLabel label = new BPredicateLiteral("foo", "pred", context);
    Predicate p = new BPredicate(label, left, right, context);
    CanonicalPredicate cp = p.getCanonical();
    assertEquals("foo:pred(foo:bar, foo:baz)", cp.toString());
    cp.invert();
    assertEquals("~foo:pred(foo:bar, foo:baz)", cp.toString());
  }

  /**
   * Test that a type predicate can be canonicalized.
   *
   * @throws Exception Due to any kind of internal failure.
   */
  public void testTypeLiteral() throws Exception {
    PredicateParam left = new BPredicateLiteral("foo", "bar", context);
    TypeLabel label = new TypeLiteral("foo", "Type", context);
    Predicate p = new TypeStatement(label, left, context);
    CanonicalPredicate cp = p.getCanonical();
    assertEquals("foo:Type(foo:bar)", cp.toString());
    cp.invert();
    assertEquals("~foo:Type(foo:bar)", cp.toString());
  }

  /**
   * Test that a binary predicate with variables can be canonicalized.
   *
   * @throws Exception Due to any kind of internal failure.
   */
  public void testBinaryVar() throws Exception {
    PredicateParam left = new BPredicateLiteral("foo", "bar", context);
    PredicateParam right = new Variable("X");
    BPredicateLabel label = new BPredicateLiteral("foo", "pred", context);
    Predicate p = new BPredicate(label, left, right, context);
    CanonicalPredicate cp = p.getCanonical();
    assertEquals("foo:pred(foo:bar, ?X)", cp.toString());
    VariableCanonicalizer con = new VariableCanonicalizer();
    cp.renameVariables(con);
    assertEquals("foo:pred(foo:bar, ?V1)", cp.toString());

    left = new Variable("A");
    right = new Variable("B");
    label = new Variable("C");
    p = new BPredicate(label, left, right, context);
    cp = p.getCanonical();
    assertEquals("?C(?A, ?B)", cp.toString());
    con = new VariableCanonicalizer();
    cp.renameVariables(con);
    assertEquals("?V2(?V1, ?V3)", cp.toString());
    cp.renameVariables(con);
    assertEquals("?V5(?V4, ?V6)", cp.toString());

    cp = p.getCanonical();
    cp.renameVariables(con);
    assertEquals("?V2(?V1, ?V3)", cp.toString());
  }

  /**
   * Test that a variable type predicate can be canonicalized.
   *
   * @throws Exception Due to any kind of internal failure.
   */
  public void testTypeVariable() throws Exception {
    PredicateParam left = new BPredicateLiteral("foo", "bar", context);
    TypeLabel label = new Variable("X");
    Predicate p = new TypeStatement(label, left, context);
    CanonicalPredicate cp = p.getCanonical();
    assertEquals("?X(foo:bar)", cp.toString());
    VariableCanonicalizer con = new VariableCanonicalizer();
    cp.renameVariables(con);
    assertEquals("?V1(foo:bar)", cp.toString());

    left = new Variable("B");
    label = new Variable("A");
    p = new TypeStatement(label, left, context);
    cp = p.getCanonical();
    assertEquals("?A(?B)", cp.toString());
    con = new VariableCanonicalizer();
    cp.renameVariables(con);
    assertEquals("?V1(?V2)", cp.toString());

    cp.renameVariables(con);
    assertEquals("?V3(?V4)", cp.toString());

    cp = p.getCanonical();
    cp.renameVariables(con);
    assertEquals("?V1(?V2)", cp.toString());
  }

  /**
   * Test that a variable type predicate can be equal.
   *
   * @throws Exception Due to any kind of internal failure.
   */
  public void testTypeEqual() throws Exception {
    PredicateParam left = new BPredicateLiteral("foo", "bar", context);
    TypeLabel label = new Variable("X");
    Predicate p = new TypeStatement(label, left, context);
    CanonicalPredicate cp = p.getCanonical();
    VariableCanonicalizer con = new VariableCanonicalizer();
    cp.renameVariables(con);
    assertEquals("?V1(foo:bar)", cp.toString());

    Predicate p2 = new TypeStatement(new Variable("V1"), left, context);
    assertNotSame(p, p2);
    assertEquals(cp, p2.getCanonical());
  }

}
