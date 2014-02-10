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
 * Represents a multiplication operation.
 *
 * @created Mar 13, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class MultiplyOperation extends BinaryOperation {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = -2996017159385273832L;

  public MultiplyOperation(NumericExpression lhs, NumericExpression rhs) {
    super(lhs, rhs);
  }

  /**
   * Use an operation set to perform a multiplication
   * @param ops The operations to use for the current parameters
   * @param left The first operand
   * @param right The second operand
   * @return The product of left and right
   */
  <L extends Number, R extends Number> Number doOperation(NumberOps<L,R> ops, L left, R right) {
    return ops.product(left, right);
  }

  /**
   * A constructor to handle adding lists of multipliers.
   * @param operands The list of multipliers to multiply.
   * @return A new MultiplyOperation which is multiplying all the elements.
   */
  public static MultiplyOperation newMultiplyOperation(List<NumericExpression> operands) {
    if (operands.size() < 2) throw new IllegalArgumentException("Require at least 2 addends for multiplication. Got " + operands.size());
    return (MultiplyOperation)createNestedMultiply(operands);
  }

  /**
   * A recursive method to build a NumericExpression that represents the product of all multipliers in the list.
   * @param ops The list of multipliers to multiply.
   * @return A NumericExpression which represents the product of everything in the ops list.
   */
  private static NumericExpression createNestedMultiply(List<NumericExpression> ops) {
    int listSize = ops.size();
    // terminate on singleton lists
    if (listSize == 1) return ops.get(0);
    // short circuit for 2 element lists - optimization
    if (listSize == 2) return new MultiplyOperation(ops.get(0), ops.get(1));
    // general case
    return new MultiplyOperation(createNestedMultiply(ops.subList(0, listSize / 2)), createNestedMultiply(ops.subList(listSize / 2, listSize)));
  }

}
