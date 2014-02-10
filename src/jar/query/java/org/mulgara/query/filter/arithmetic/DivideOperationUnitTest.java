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
package org.mulgara.query.filter.arithmetic;

import java.util.ArrayList;
import java.util.List;

import org.jrdf.graph.Literal;
import org.mulgara.query.QueryException;

import org.mulgara.query.filter.value.NumericExpression;
import org.mulgara.query.filter.value.NumericLiteral;
import org.mulgara.query.filter.value.SimpleLiteral;
import org.mulgara.query.filter.value.ValueLiteral;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Tests the dividing operation classes.
 *
 * @created Apr 14, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class DivideOperationUnitTest extends AbstractOperationUnitTest {

  /**
   * Build the unit test.
   * @param name The name of the test
   */
  public DivideOperationUnitTest(String name) {
    super(name);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * @return The test suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new DivideOperationUnitTest("testLiteral"));
    suite.addTest(new DivideOperationUnitTest("testVar"));
    suite.addTest(new DivideOperationUnitTest("testMultiOp"));
    return suite;
  }

  Number op1() { return 12; }

  Number op2() { return 5; }

  BinaryOperation getOperation(NumericExpression literal1, NumericExpression literal2) {
    return new DivideOperation(literal1, literal2);
  }

  ValueLiteral doOperation(Literal l1, Literal l2) throws QueryException {
    NumberType t1 = getType(l1);
    NumberType t2 = getType(l2);
    switch (t1) {
    case tInt:
      int int1 = getInt(l1);
      switch (t2) {
      case tInt: return newLiteral(int1 / getInt(l2));
      case tLong: return newLiteral(int1 / getLong(l2));
      case tFloat: return newLiteral(int1 / getFloat(l2));
      case tDouble: return newLiteral(int1 / getDouble(l2));
      }
    case tLong:
      long long1 = getLong(l1);
      switch (t2) {
      case tInt: return newLiteral(long1 / getInt(l2));
      case tLong: return newLiteral(long1 / getLong(l2));
      case tFloat: return newLiteral(long1 / getFloat(l2));
      case tDouble: return newLiteral(long1 / getDouble(l2));
      }
    case tFloat:
      float float1 = getFloat(l1);
      switch (t2) {
      case tInt: return newLiteral(float1 / getInt(l2));
      case tLong: return newLiteral(float1 / getLong(l2));
      case tFloat: return newLiteral(float1 / getFloat(l2));
      case tDouble: return newLiteral(float1 / getDouble(l2));
      }
    case tDouble:
      double double1 = getDouble(l1);
      switch (t2) {
      case tInt: return newLiteral(double1 / getInt(l2));
      case tLong: return newLiteral(double1 / getLong(l2));
      case tFloat: return newLiteral(double1 / getFloat(l2));
      case tDouble: return newLiteral(double1 / getDouble(l2));
      }
    }
    throw new IllegalArgumentException("Unable to process argument of types: " + t1 + ", " + t2);
  }

  public void testMultiOp() throws Exception {
    List<NumericExpression> ops = new ArrayList<NumericExpression>();
    ops.add(new NumericLiteral(100));
    ops.add(new NumericLiteral(4));
    ops.add(new NumericLiteral(5));
    DivideOperation op = DivideOperation.newDivideOperation(ops);
    NumericLiteral literalResult = new NumericLiteral(5);
    assertTrue(op.equals(literalResult));
    assertFalse(op.isBlank());
    assertFalse(op.isIRI());
    assertTrue(op.isLiteral());
    assertFalse(op.isURI());
    assertTrue(literalResult.getType().equals(op.getType()));
    assertEquals(SimpleLiteral.EMPTY, op.getLang());

    ops.clear();
    ops.add(new NumericLiteral(100));
    ops.add(new NumericLiteral((float)2.0));
    ops.add(new NumericLiteral(5L));
    ops.add(new NumericLiteral(4.0));
    op = DivideOperation.newDivideOperation(ops);
    literalResult = new NumericLiteral(100 / ((float)2.0) / 5L / 4.0);
    assertTrue(op.equals(literalResult));
    assertFalse(op.isBlank());
    assertFalse(op.isIRI());
    assertTrue(op.isLiteral());
    assertFalse(op.isURI());
    assertTrue(literalResult.getType().equals(op.getType()));
    assertEquals(SimpleLiteral.EMPTY, op.getLang());
  }

}
