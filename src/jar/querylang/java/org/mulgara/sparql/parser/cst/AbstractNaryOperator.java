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

import java.util.List;


/**
 * Absract class to provide basic functionality of an N-ary, associative operation.
 *
 * @created Feb 13, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public abstract class AbstractNaryOperator<T extends Expression> implements Expression {

  /** The list of expression operands */
  protected List<T> operands;

  /**
   * @return the list of operands for this operation
   */
  public List<T> getOperands() {
    return operands;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    StringBuffer result = new StringBuffer();
    boolean first = true;
    for (Expression e: operands) {
      if (!first) result.append(" ").append(getOperatorString()).append(" ");
      else first = false;
      
      boolean bracket = e instanceof PrimaryExpression;
      if (bracket) result.append("(");
      result.append(e);
      if (bracket) result.append(")");
    }
    return result.toString();
  }

  /**
   * A string representation of the operator for this relation
   * @return The operator as a string
   */
  protected abstract String getOperatorString();

  /**
   * @see java.lang.Object#toString()
   * @return By default this will return the image, since that's often right.
   */
  public String toString() {
    return getImage();
  }
}
