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
 * Describes an expression that represents a binary relation.
 *
 * @created Feb 13, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public abstract class RelationalExpression implements LogicExpression {
  /** The left hand side of this binary expression */
  private Expression lhs;

  /** The right hand side of this binary expression */
  private Expression rhs;

  /**
   * Creates the binary relation between two numberic expressions
   * @param lhs The left hand side of the relation
   * @param rhs The right hand side of the relation
   */
  public RelationalExpression(NumericExpression lhs, NumericExpression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  /**
   * Creates the binary relation between arbitrary expressions
   * @param lhs The left hand side of the relation
   * @param rhs The right hand side of the relation
   */
  public RelationalExpression(Expression lhs, Expression rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    return lhs.getImage() + getOperator() + rhs.getImage();
  }

  /**
   * @return the lhs
   */
  public Expression getLhs() {
    return lhs;
  }

  
  /**
   * @return the rhs
   */
  public Expression getRhs() {
    return rhs;
  }

  /**
   * A string representation of the operator for this relation
   * @return The operator as a string, including spaces if desired
   */
  protected abstract String getOperator();

}
