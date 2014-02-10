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
 * An invocation to the SAME_TERM function
 *
 * @created Feb 13, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class BicSameTerm extends AbstractBinaryOperator implements BuiltInCall, LogicExpression {

  /**
   * Construct an invocation to the STR function
   * @param op1 The first operand for the function
   * @param op2 The second operand for the function
   */
  public BicSameTerm(Expression op1, Expression op2) {
    super(op1, op2);
  }

  @Override
  protected String getOperatorString() {
    return "SAME_TERM";
  }

}
