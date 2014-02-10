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
 * An inequality relation
 *
 * @created Feb 13, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class NotEquals extends RelationalExpression {

  /**
   * Create an inequality relation
   * @param lhs The LHS of the relation
   * @param rhs The LHS of the relation
   */
  public NotEquals(Expression lhs, Expression rhs) {
    super((NumericExpression)lhs, (NumericExpression)rhs);
  }

  /**
   * Create an inequality relation
   * @param lhs The LHS of the relation
   * @param rhs The LHS of the relation
   */
  public NotEquals(NumericExpression lhs, NumericExpression rhs) {
    super(lhs, rhs);
  }

  /**
   * @see org.mulgara.sparql.parser.cst.RelationalExpression#getOperator()
   */
  public String getOperator() {
    return " != ";
  }

}
