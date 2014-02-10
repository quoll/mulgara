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

import java.util.ArrayList;


/**
 * Represents an n-ary OR expression.
 *
 * @created Feb 13, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class OrExpression extends AbstractNaryOperator<LogicExpression> implements Node, LogicExpression {
  
  /**
   *  Creates an OR expression from a pair of expressions
   *  @param e1 The LHS expression
   *  @param e2 The RHS expression
   */
  public OrExpression(Expression e1, Expression e2) {
    this((LogicExpression)e1, (LogicExpression)e2);
  }

  /**
   *  Creates an OR expression from a pair of expressions
   *  @param e1 The LHS expression
   *  @param e2 The RHS expression
   */
  public OrExpression(LogicExpression e1, LogicExpression e2) {
    if (e1 instanceof OrExpression) {
      operands = new ArrayList<LogicExpression>(((OrExpression)e1).operands);
    } else {
      operands = new ArrayList<LogicExpression>();
      operands.add(e1);
    }
    operands.add(e2);
  }

  /**
   * A string representation of the operator for this operator
   * @return The operator as a string
   */
  protected String getOperatorString() {
    return "OR";
  }

}
