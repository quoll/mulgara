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

import java.util.List;

import org.mulgara.query.filter.value.NumericExpression;


/**
 * Represents a subtraction operation.
 *
 * @created Mar 14, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class MinusOperation extends BinaryOperation {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 3356396260843373856L;

  public MinusOperation(NumericExpression lhs, NumericExpression rhs) {
    super(lhs, rhs);
  }

  /**
   * Use an operation set to perform a subtraction
   * @param ops The operations to use for the current parameters
   * @param left The minuend
   * @param right The subtrahend
   * @return The difference between left and right
   */
  <L extends Number, R extends Number> Number doOperation(NumberOps<L,R> ops, L left, R right) {
    return ops.subtract(left, right);
  }

  /**
   * A constructor to handle subtracting lists of values.
   * @param operands The list of numbers to subtract.
   * @return A new MinusOperation which is subtracting numbers in order.
   */
  public static MinusOperation newMinusOperation(List<NumericExpression> operands) {
    if (operands.size() < 2) throw new IllegalArgumentException("Require at least 2 addends for subtraction. Got " + operands.size());
    return (MinusOperation)createNestedSubtraction(operands);
  }

  /**
   * A recursive method to build a NumericExpression that represents the subtraction of all values in the list.
   * @param ops The list of values to subtract.
   * @return A NumericExpression which represents the ordered subtraction of everything in the ops list.
   */
  private static NumericExpression createNestedSubtraction(List<NumericExpression> ops) {
    int listSize = ops.size();
    // error on singleton lists
    if (listSize == 1) throw new IllegalStateException("Should not be creating subtractions with single elements");
    // terminate on 2 element lists
    if (listSize == 2) return new MinusOperation(ops.get(0), ops.get(1));
    // general case
    return new MinusOperation(createNestedSubtraction(ops.subList(0, listSize - 1)), ops.get(listSize - 1));
  }

}
