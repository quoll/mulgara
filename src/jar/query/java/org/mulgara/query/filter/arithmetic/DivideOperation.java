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
 * Represents a division operation.
 *
 * @created Mar 14, 2008
 * @author Paula Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paula Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public class DivideOperation extends BinaryOperation {

  /** Generated Serialization ID for RMI */
  private static final long serialVersionUID = 5863800753778864432L;

  public DivideOperation(NumericExpression lhs, NumericExpression rhs) {
    super(lhs, rhs);
  }

  /**
   * Use an operation set to perform a division
   * @param ops The operations to use for the current parameters
   * @param left The dividend
   * @param right The divisor
   * @return The quotient of dividing right into left
   */
  <L extends Number, R extends Number> Number doOperation(NumberOps<L,R> ops, L left, R right) {
    return ops.divide(left, right);
  }

  /**
   * A constructor to handle dividnd lists of values.
   * @param operands The list of numbers to divide.
   * @return A new DivideOperation which is dividing numbers in order.
   */
  public static DivideOperation newDivideOperation(List<NumericExpression> operands) {
    if (operands.size() < 2) throw new IllegalArgumentException("Require at least 2 addends for subtraction. Got " + operands.size());
    return (DivideOperation)createNestedDivision(operands);
  }

  /**
   * A recursive method to build a NumericExpression that represents the division of all values in the list.
   * This constructs a linked list of divisions.
   * @param ops The list of values to divide.
   * @return A NumericExpression which represents the ordered division of everything in the ops list.
   */
  private static NumericExpression createNestedDivision(List<NumericExpression> ops) {
    int listSize = ops.size();
    // error on singleton lists
    if (listSize == 1) throw new IllegalStateException("Should not be creating divisions with single elements");
    // terminate on 2 element lists
    if (listSize == 2) return new DivideOperation(ops.get(0), ops.get(1));
    // general case
    return new DivideOperation(createNestedDivision(ops.subList(0, listSize - 1)), ops.get(listSize - 1));
  }

}
