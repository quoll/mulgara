/*
 * Copyright 2008 Fedora Commons
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mulgara.sparql.parser.cst;


/**
 * Absract class to provide basic functionality of an N-ary, associative operation.
 *
 * @created Feb 13, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public abstract class AbstractBinaryOperator implements Expression {

  /** The first operand */
  protected Expression op1;

  /** The second operand */
  protected Expression op2;
  
  /**
   * Creates the operation.
   */
  public AbstractBinaryOperator(Expression op1, Expression op2) {
    this.op1 = op1;
    this.op2 = op2;
  }

  /**
   * @return the first operand for this operation
   */
  public Expression getFirstOperand() {
    return op1;
  }

  /**
   * @return the second operand for this operation
   */
  public Expression getSecondOperand() {
    return op2;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   * Used for Prefix notation. Override for infix notation.
   */
  public String getImage() {
    StringBuffer result = new StringBuffer(getOperatorString());
    result.append(op1.getImage());
    result.append(", ");
    result.append(op2.getImage());
    result.append(")");
    return result.toString();
  }

  /**
   * A string representation of the operator for this operator
   * @return The operator as a string
   */
  protected abstract String getOperatorString();

}
