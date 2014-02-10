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
 * Wraps ordering information.
 *
 * @created Feb 18, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
 * @licence <a href="{@docRoot}/../LICENCE.txt">Apache License, Version 2.0</a>
 */
public class Ordering implements Node {

  /** The expression that describes the ordering */
  private Expression expr;

  /** The direction of the data with respect to the expression. <code>true</code> for ascending. */
  private boolean ascending;

  /**
   * Create an ordering on a given expression, in a specific direction.
   * @param expr The expression to order with respect to.
   * @param ascending <code>true</code> if ascending, <code>false</code> if descending.
   */
  public Ordering(Expression expr, boolean ascending) {
    this.expr = expr;
    this.ascending = ascending;
  }
  
  /**
   * Create an ordering, ascending on a given expression.
   * @param expr The expression to order with respect to.
   */
  public Ordering(Expression expr) {
    this(expr, true);
  }

  /**
   * @return the expr
   */
  public Expression getExpr() {
    return expr;
  }

  /**
   * @return <code>true</code> if ascending, <code>false</code> if descending
   */
  public boolean isAscending() {
    return ascending;
  }

  /**
   * @see org.mulgara.sparql.parser.cst.Node#getImage()
   */
  public String getImage() {
    return direction() + expr.getImage();
  }
  
  /**
   * Gets a string representing the direction wrt the expression.
   * @return The label for the direction.
   */
  private String direction() {
    return ascending ? "ASC " : "DESC ";
  }

}
