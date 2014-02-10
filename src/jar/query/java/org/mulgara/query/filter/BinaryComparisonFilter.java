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
package org.mulgara.query.filter;

import org.mulgara.query.QueryException;
import org.mulgara.query.filter.value.Bool;
import org.mulgara.query.filter.value.ComparableExpression;


/**
 * A comparison operation.
 *
 * @created Mar 7, 2008
 * @author Paul Gearon
 * @copyright &copy; 2008 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
 */
public abstract class BinaryComparisonFilter extends AbstractFilterValue implements Filter {

  /** Serialization ID */
  private static final long serialVersionUID = -5041035997419603862L;

  /** The first operand */
  protected ComparableExpression lhs;

  /** The second operand */
  protected ComparableExpression rhs;

  /**
   * Creates a binary comparison, and registers this filter as the context owner
   * @param lhs The left comparison expression
   * @param rhs The right comparison expression
   */
  BinaryComparisonFilter(ComparableExpression lhs, ComparableExpression rhs) {
    super(lhs, rhs);
    this.lhs = lhs;
    this.rhs = rhs;
  }

  /**
   * @see org.mulgara.query.filter.Filter#test(Context)
   */
  public boolean test(Context context) throws QueryException {
    setCurrentContext(context);
    return testCmp();
  }

  /** @see org.mulgara.query.filter.AbstractFilterValue#resolve() */
  protected RDFTerm resolve() throws QueryException {
    return testCmp() ? Bool.TRUE : Bool.FALSE;
  }

  /**
   * A comparison specific function.
   * @return <code>true</code> iff the comparison passes.
   * @throws QueryException If there was an error resolving the parameters of the comparison.
   */
  abstract boolean testCmp() throws QueryException;

}
